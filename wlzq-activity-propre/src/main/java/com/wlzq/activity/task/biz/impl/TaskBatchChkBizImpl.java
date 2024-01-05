package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.task.biz.TaskBatchChkBiz;
import com.wlzq.activity.task.biz.UserTaskBiz;
import com.wlzq.activity.task.dao.ActBjsOpenRecordDao;
import com.wlzq.activity.task.dao.ActNoActBankTransferDao;
import com.wlzq.activity.task.dao.DataDateDao;
import com.wlzq.activity.task.model.ActBjsOpenRecord;
import com.wlzq.activity.task.model.ActNoActBankTransfer;
import com.wlzq.activity.task.model.DataDate;
import com.wlzq.activity.task.redis.BatchChkRedis;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author renjiyun
 */
@Service
@Slf4j
public class TaskBatchChkBizImpl implements TaskBatchChkBiz {

    @Autowired
    private DataDateDao dataDateDao;
    @Autowired
    private ActBjsOpenRecordDao actBjsOpenRecordDao;
    @Autowired
    private ActNoActBankTransferDao actNoActBankTransferDao;
    @Autowired
    private UserTaskBiz userTaskBiz;

    @Override
    public void checkBjsPerm(String checkDate) {

        String lastHandleDateStr = (String) BatchChkRedis.ACT_2023DOUBLE11_BJS.get("LAST_HANDLE_DATE");
        if (lastHandleDateStr == null) {
            // 该日期只要早于活动开始日期即可
            lastHandleDateStr = "20231031";
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DataDate dataDate = getLastDataDate(2);
        Date lastSyncDate = dataDate.getDataDate();

        // 需要处理开通记录应该在: (上次处理日期, 当前同步日期] 之间
        String lastSyncDateStr = dateFormat.format(lastSyncDate);
        List<ActBjsOpenRecord> actBjsOpenRecords = actBjsOpenRecordDao.queryByDate(lastHandleDateStr, lastSyncDateStr);
        if (CollectionUtil.isNotEmpty(actBjsOpenRecords)) {
            log.info("本次共处理北交所开通流水数据 {} 条", actBjsOpenRecords.size());

            for (ActBjsOpenRecord record : actBjsOpenRecords) {
                // 触发任务完成
                String userIdAndMobile = (String) BatchChkRedis.ACT_2023DOUBLE11_BJS.get(record.getClientId());
                if (ObjectUtils.isEmptyOrNull(userIdAndMobile)) {
                    continue;
                }

                String userId = userIdAndMobile.split(",")[0];
                String mobile = userIdAndMobile.split(",")[1];
                AccTokenUser user = new AccTokenUser();
                user.setMobile(mobile);
                user.setUserId(userId);

                Customer customer = new Customer();
                customer.setCustomerId(record.getClientId());

                try {
                    userTaskBiz.doUserTask("ACTIVITY.2023DOUBLE11.ZJF", "TASK.2023DOUBLE11.33",
                            "", "", mobile, user, customer, true);
                } catch (Throwable t) {
                    if (t instanceof ActivityBizException) {
                        ActivityBizException e = (ActivityBizException) t;
                        log.info("批量检查北交所权限, 客户号: {}, 业务异常: {}", record.getClientId(), e.getMsg());
                    } else {
                        log.error("批量检查北交所权限任务异常, 客户号: {}, 异常信息: {}", record.getClientId(), t.getMessage());
                    }
                }
            }
        } else {
            log.info("本次共处理北交所开通流水数据 0 条");
        }

        // 更新上次处理日期
        BatchChkRedis.ACT_2023DOUBLE11_BJS.set("LAST_HANDLE_DATE", lastSyncDateStr);
    }

    private DataDate getLastDataDate(int type) {
        return dataDateDao.getLastDataDate(type);
    }

    @Override
    public void checkRjFlow(String checkDate) {
        String lastHandleDateStr = (String) BatchChkRedis.ACT_2023DOUBLE11_RJ.get("LAST_HANDLE_DATE");
        if (lastHandleDateStr == null) {
            // 该日期只要早于活动开始日期即可
            lastHandleDateStr = "20231031";
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DataDate dataDate = getLastDataDate(3);
        Date lastSyncDate = dataDate.getDataDate();

        // 需要处理开通记录应该在: (上次处理日期, 当前同步日期] 之间
        String lastSyncDateStr = dateFormat.format(lastSyncDate);
        List<ActNoActBankTransfer> actNoActBankTransfers = actNoActBankTransferDao.queryByDate(lastHandleDateStr, lastSyncDateStr);
        if (CollectionUtil.isNotEmpty(actNoActBankTransfers)) {
            log.info("本次共处理入金流水数据 {} 条", actNoActBankTransfers.size());

            for (ActNoActBankTransfer transfer : actNoActBankTransfers) {

                String userIdAndMobile = (String) BatchChkRedis.ACT_2023DOUBLE11_RJ.get(transfer.getClientId());
                if (ObjectUtils.isEmptyOrNull(userIdAndMobile)) {
                    continue;
                }
                String userId = userIdAndMobile.split(",")[0];
                String mobile = userIdAndMobile.split(",")[1];
                AccTokenUser user = new AccTokenUser();
                user.setMobile(mobile);
                user.setUserId(userId);

                Customer customer = new Customer();
                customer.setCustomerId(transfer.getClientId());

                try {
                    userTaskBiz.doUserTask("ACTIVITY.2023DOUBLE11.ZJF", "TASK.2023DOUBLE11.34",
                            "", "", mobile, user, customer, true);
                } catch (Throwable t) {
                    if (t instanceof ActivityBizException) {
                        ActivityBizException e = (ActivityBizException) t;
                        log.info("批量检查入金流水, 客户号: {}, 业务异常: {}", transfer.getClientId(), e.getMsg());
                    } else {
                        log.error("批量检查入金流水任务异常, 客户号: {}, 异常信息: {}", transfer.getClientId(), t.getMessage());
                    }
                }
            }
        } else {
            log.info("本次共处理入金流水数据 0 条");
        }

        // 更新上次处理日期
        BatchChkRedis.ACT_2023DOUBLE11_RJ.set("LAST_HANDLE_DATE", lastSyncDateStr);
    }

}
