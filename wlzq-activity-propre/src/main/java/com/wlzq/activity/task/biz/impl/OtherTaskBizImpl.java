package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.activity.double11.dto.QrcodeDto;
import com.wlzq.activity.task.biz.SpecificTaskBiz;
import com.wlzq.activity.task.dao.ActNoActBankTransferDao;
import com.wlzq.activity.task.dao.ActReturnVisitRecordDao;
import com.wlzq.activity.task.model.ActNoActBankTransfer;
import com.wlzq.activity.task.model.ActReturnVisitRecord;
import com.wlzq.activity.task.redis.BatchChkRedis;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 其他类任务校验
 *
 * @author renjiyun
 */
@Service
@Slf4j
public class OtherTaskBizImpl implements SpecificTaskBiz {

    @Autowired
    private Double11Biz double11Biz;
    @Autowired
    private ActPrizeDao actPrizeDao;
    @Autowired
    private ActReturnVisitRecordDao actReturnVisitRecordDao;
    @Autowired
    private ActNoActBankTransferDao actNoActBankTransferDao;

    private final String[] supportTaskCodes = {
            "TASK.2023DOUBLE11.34", // 单笔入金人民币1万元
            "TASK.2023DOUBLE11.35", // 完成问卷回访
            "TASK.2023DOUBLE11.36", // 免费领新客专享大礼包
            "TASK.2023DOUBLE11.37", // 文章留言
    };

    @Override
    public String[] supportTaskCodes() {
        return supportTaskCodes;
    }

    @Override
    public Tuple checkTask(Activity activity, ActTask actTask, List<ActGoodsFlow> actGoodsFlowList, String bizCode,
                           String mobile, AccTokenUser user, Customer customer, String recommendMobile, boolean isBatch) {
        String taskCode = actTask.getCode();
        if (taskCode.equals("TASK.2023DOUBLE11.34")) {
            return checkRj(activity, mobile, user, customer.getCustomerId(), isBatch);
        } else if (taskCode.equals("TASK.2023DOUBLE11.35")) {
            return checkReturnVisit(activity, user, customer.getCustomerId(), recommendMobile);
        } else if (taskCode.equals("TASK.2023DOUBLE11.36")) {
            return checkNewCustomerGift(activity, customer.getCustomerId());
        } else if (taskCode.equals("TASK.2023DOUBLE11.37")) {
            if (user.getMobile().equals(recommendMobile)) {
                return new Tuple(true, "");
            }
            return new Tuple(true, recommendMobile);
        }
        return new Tuple(false, "");
    }

    private Tuple checkNewCustomerGift(Activity activity, String customerId) {
        if (ObjectUtils.isEmptyOrNull(customerId)) {
            return new Tuple(false, "");
        }

        ActPrize qryActPrize = new ActPrize()
                .setCustomerId(customerId)
                .setActivityCode("ACTIVITY.ACCOUNT.NEW.CUSTOMER.GIFTBAG")
                .setCodes(CollectionUtil.newArrayList("PRIZE.NEW.CUSTOMER.GIFTBAG.INVESTMENT.14DAYS"));

        List<ActPrize> actPrizes = actPrizeDao.findCustomerPrizes(qryActPrize);
        if (CollectionUtil.isEmpty(actPrizes)) {
            return new Tuple(false, "");
        }

        ActPrize actPrize = actPrizes.get(0);
        Date updateTime = actPrize.getUpdateTime();
        if (updateTime.after(activity.getDateTo()) || updateTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }
        QrcodeDto qrcodeDto = double11Biz.getQrcode(customerId);
        return new Tuple(true, qrcodeDto.getMobile());
    }

    private Tuple checkReturnVisit(Activity activity, AccTokenUser user, String customerId, String recommendMobile) {
        if (ObjectUtils.isEmptyOrNull(customerId)) {
            return new Tuple(false, "");
        }

        List<ActReturnVisitRecord> actReturnVisitRecordList = getActReturnVisitRecordList(customerId);
        if (CollectionUtil.isEmpty(actReturnVisitRecordList)) {
            return new Tuple(false, "");
        }

        // 校验是否在任务指定的时间范围内完成了问卷回访
        for (ActReturnVisitRecord actReturnVisitRecord : actReturnVisitRecordList) {
            // 校验回访日期
            Date finishTime = actReturnVisitRecord.getFinishTime();
            if (finishTime == null || finishTime.after(activity.getDateTo()) || finishTime.before(activity.getDateFrom())) {
                continue;
            }
            if (user.getMobile().equals(recommendMobile)) {
                return new Tuple(true, "");
            }
            return new Tuple(true, recommendMobile);
        }

        return new Tuple(false, "");
    }

    private List<ActReturnVisitRecord> getActReturnVisitRecordList(String customerId) {
        List<ActReturnVisitRecord> actReturnVisitRecordList =
                actReturnVisitRecordDao.findByCustomerIdAndTaskNo(customerId, "OLDCUS2023");
        return actReturnVisitRecordList;
    }

    private Tuple checkRj(Activity activity, String mobile, AccTokenUser user, String customerId, boolean isBatch) {
        if (ObjectUtils.isEmptyOrNull(mobile) || ObjectUtils.isEmptyOrNull(customerId) || user == null) {
            return new Tuple(false, "");
        }

        // 批量处理时, 需要该信息
        BatchChkRedis.ACT_2023DOUBLE11_RJ.set(customerId, user.getUserId() + "," + mobile);

        // 获取该客户的入金流水
        ActNoActBankTransfer qryActNoActBankTransfer = new ActNoActBankTransfer()
                .setClientId(customerId);
        List<ActNoActBankTransfer> actNoActBankTransferList = actNoActBankTransferDao.findList(qryActNoActBankTransfer);
        if (CollectionUtil.isEmpty(actNoActBankTransferList)) {
            return new Tuple(false, "");
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        // 校验是否有单笔入金人民币1万元的流水
        for (ActNoActBankTransfer actNoActBankTransfer : actNoActBankTransferList) {
            try {
                Date createTime = dateFormat.parse(actNoActBankTransfer.getInitDate());
                if (!isBatch && (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom()))) {
                    continue;
                }

                BigDecimal amount = new BigDecimal(actNoActBankTransfer.getOccurBalance());
                if (amount.compareTo(new BigDecimal("10000")) < 0) {
                    continue;
                }

                String recommendMobile = "";
                QrcodeDto qrcodeDto = double11Biz.getQrcode(customerId);
                if (qrcodeDto == null) {
                    log.warn("财人汇获取客户服务关系失败, customerId: {}", customerId);
                } else {
                    recommendMobile = qrcodeDto.getMobile();
                }
                return new Tuple(true, recommendMobile);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return new Tuple(false, "");
    }

    @Override
    public void excludeTaskCodes(Set<String> candidateTaskCodes, AccTokenUser user, Customer customer) {
        if (CollectionUtil.isEmpty(candidateTaskCodes)) {
            return;
        }
        if (customer == null) {
            // 若没有登录客户号, 则需要排除入金, 问卷回访, 新客大礼包领取任务, 因为无法确定这些任务该客户是否可做
            candidateTaskCodes.remove("TASK.2023DOUBLE11.34");
            candidateTaskCodes.remove("TASK.2023DOUBLE11.35");
            candidateTaskCodes.remove("TASK.2023DOUBLE11.36");
        }

        if (candidateTaskCodes.contains("TASK.2023DOUBLE11.34") && !double11Biz.checkNonAccount(customer)) {
            // 有效户不能展示入金任务
            candidateTaskCodes.remove("TASK.2023DOUBLE11.34");
        }

        // 若已经完成2023年问卷回访任务, 则不展示问卷回访任务
        if (candidateTaskCodes.contains("TASK.2023DOUBLE11.35") && haveDoneQuestionnaire(customer)) {
            candidateTaskCodes.remove("TASK.2023DOUBLE11.35");
        }

        if (candidateTaskCodes.contains("TASK.2023DOUBLE11.36") &&
                (haveReceiveNewCustomerGift(customer) || !havePermissionToReceiveNewCustomerGift(customer))) {
            // 若已经领取过新客大礼包或者没有领取新客大礼包的权限, 则不展示新客大礼包任务
            candidateTaskCodes.remove("TASK.2023DOUBLE11.36");
        }
    }

    private boolean haveDoneQuestionnaire(Customer customer) {
        List<ActReturnVisitRecord> actReturnVisitRecordList = getActReturnVisitRecordList(customer.getCustomerId());
        if (CollectionUtil.isEmpty(actReturnVisitRecordList)) {
            return false;
        }

        actReturnVisitRecordList = actReturnVisitRecordList.stream()
                .filter(e -> e.getRecordStatus() != null && e.getRecordStatus().equals(4)
                ).collect(Collectors.toList());
        return CollectionUtil.isNotEmpty(actReturnVisitRecordList);
    }

    private boolean haveReceiveNewCustomerGift(Customer customer) {
        // 是否领取过新客大礼包以 PRIZE.NEW.CUSTOMER.GIFTBAG.INVESTMENT.14DAYS 的领取为准
        // 活动编码为: ACTIVITY.ACCOUNT.NEW.CUSTOMER.GIFTBAG
        ActPrize qryActPrize = new ActPrize()
                .setCustomerId(customer.getCustomerId())
                .setActivityCode("ACTIVITY.ACCOUNT.NEW.CUSTOMER.GIFTBAG")
                .setCodes(CollectionUtil.newArrayList("PRIZE.NEW.CUSTOMER.GIFTBAG.INVESTMENT.14DAYS"));

        List<ActPrize> actPrizes = actPrizeDao.findCustomerPrizes(qryActPrize);
        return CollectionUtil.isNotEmpty(actPrizes);
    }

    private boolean havePermissionToReceiveNewCustomerGift(Customer customer) {
        Date openDate = customer.getOpenDate();
        if (openDate == null) {
            return false;
        }
        Date now = new Date();
        DateTime openDate1 = DateUtil.beginOfDay(openDate);
        DateTime now1 = DateUtil.beginOfDay(now);

        long days = DateUtil.between(openDate1, now1, DateUnit.DAY);
        if (openDate1.getTime() > now1.getTime() || days > 180) {
            return false;
        }

        Integer riskLevel = customer.getRiskLevel();
        if (riskLevel == null || riskLevel.compareTo(11) < 0 || riskLevel.compareTo(15) > 0) {
            return false;
        }

        Date riskEndDate = customer.getRiskEndDate();
        if (riskEndDate == null || now.after(riskEndDate)) {
            return false;
        }

        return true;
    }
}
