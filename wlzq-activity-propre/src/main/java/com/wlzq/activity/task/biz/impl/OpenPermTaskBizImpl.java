package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.activity.double11.dao.ActCustomerUnionIdDao;
import com.wlzq.activity.double11.dto.QrcodeDto;
import com.wlzq.activity.double11.model.ActCustomerUnionId;
import com.wlzq.activity.task.biz.HengShengBiz;
import com.wlzq.activity.task.biz.SpecificTaskBiz;
import com.wlzq.activity.task.redis.BatchChkRedis;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 开通权限类任务校验
 *
 * @author renjiyun
 */
@Service
@Slf4j
public class OpenPermTaskBizImpl implements SpecificTaskBiz {
    @Autowired
    private Double11Biz double11Biz;
    @Autowired
    private ActCustomerUnionIdDao actCustomerUnionIdDao;
    @Autowired
    private HengShengBiz hengShengBiz;
    @Autowired
    private ActPrizeDao actPrizeDao;

    /** 添加专属经理企微 */
    private static final String TASK_2023DOUBLE11_31 = "TASK.2023DOUBLE11.31";
    /** 免费领港股L2三个月 */
    private static final String TASK_2023DOUBLE11_32 = "TASK.2023DOUBLE11.32";
    /** 开通北交所权限 */
    private static final String TASK_2023DOUBLE11_33 = "TASK.2023DOUBLE11.33";

    private final String[] supportTaskCodes = {
            TASK_2023DOUBLE11_31,
            TASK_2023DOUBLE11_32,
            TASK_2023DOUBLE11_33
    };

    @Override
    public String[] supportTaskCodes() {
        return supportTaskCodes;
    }

    @Override
    public Tuple checkTask(Activity activity, ActTask actTask, List<ActGoodsFlow> actGoodsFlowList, String bizCode,
                           String mobile, AccTokenUser user, Customer customer, String recommendMobile, boolean isBatch) {
        String taskCode = actTask.getCode();
        if (TASK_2023DOUBLE11_31.equals(taskCode)) {
            // 若未登录客户号, 则返回校验失败
            if (ObjectUtils.isEmptyOrNull(mobile)) {
                return new Tuple(false, "");
            }
            ActCustomerUnionId latestUnionIdRecord = getLatestUnionIdRecord(mobile);
            if (latestUnionIdRecord == null) {
                return new Tuple(false, "");
            }
            // 校验是否成功添加了专属经理企微
            Tuple checkResult = double11Biz.checkQiWeiAddResult(latestUnionIdRecord.getCustomerId(), latestUnionIdRecord.getUnionId());
            boolean addResult = checkResult.get(0);
            if (!addResult) {
                return new Tuple(false, "");
            }


            QrcodeDto qrcodeDto = double11Biz.getQrcode(latestUnionIdRecord.getCustomerId());
            String qiweiMobile = "";
            if (qrcodeDto != null) {
                qiweiMobile = qrcodeDto.getMobile();
            }

            // 第二个参数为推荐人手机号, 第三个参数则是渠道, 由财人汇返回
            if (user.getMobile().equals(recommendMobile)) {
                return new Tuple(true, "", checkResult.get(1), qiweiMobile);
            }
            return new Tuple(true, recommendMobile, checkResult.get(1), qiweiMobile);
        } else if (TASK_2023DOUBLE11_32.equals(taskCode)) {
            // 检查是否已经领取港股L2三个月免费券, 该活动为分会场活动
            return checkGgL2FreeCoupon(activity, user, recommendMobile);
        } else if (TASK_2023DOUBLE11_33.equals(taskCode)) {
            return checkBjsPerm(activity, user, customer, isBatch);
        }
        return new Tuple(false, "");
    }

    private Tuple checkGgL2FreeCoupon(Activity activity, AccTokenUser user, String recommendMobile) {
        ActPrize qryActPrize = new ActPrize()
                // 分会场活动编码
                .setActivityCode("ACTIVITY.2023DOUBLE11.GGT")
                .setUserId(user.getUserId())
                .setCode("PRIZE.COUPON.GL2.3.2023DOUBLE11");
        List<ActPrize> actPrizeList = actPrizeDao.findList(qryActPrize);
        if (CollectionUtil.isEmpty(actPrizeList)) {
            return new Tuple(false, "");
        }

        if (user.getMobile().equals(recommendMobile)) {
            return new Tuple(true, "");
        }
        return new Tuple(true, recommendMobile);
    }

    private Tuple checkBjsPerm(Activity activity, AccTokenUser user, Customer customer, boolean isBatch) {
        if (ObjectUtils.isEmptyOrNull(user) || ObjectUtils.isEmptyOrNull(customer)) {
            return new Tuple(false, "");
        }

        // 批量检查时, 需要该信息
        BatchChkRedis.ACT_2023DOUBLE11_BJS.set(customer.getCustomerId(), user.getUserId() + "," + user.getMobile());

        // 校验该客户是否已开通北交所权限
        if (isBatch || hengShengBiz.checkBjsPerm(user, customer)) {
            QrcodeDto qrcodeDto = double11Biz.getQrcode(customer.getCustomerId());
            String recommendMobile = "";
            if (qrcodeDto == null) {
                log.warn("财人汇获取客户服务关系失败, customerId: {}", customer.getCustomerId());
            } else {
                recommendMobile = qrcodeDto.getMobile();
            }

            return new Tuple(true, recommendMobile);
        }
        return new Tuple(false, "");
    }


    private ActCustomerUnionId getLatestUnionIdRecord(String mobile) {
        ActCustomerUnionId qryActCustomerUnionId = new ActCustomerUnionId()
                .setMobile(mobile);
        List<ActCustomerUnionId> actCustomerUnionIdList = actCustomerUnionIdDao.findList(qryActCustomerUnionId);
        if (CollectionUtil.isEmpty(actCustomerUnionIdList)) {
            return null;
        }
        return actCustomerUnionIdList.get(0);
    }

    @Override
    public void excludeTaskCodes(Set<String> candidateTaskCodes, AccTokenUser user, Customer customer) {
        if (CollectionUtil.isEmpty(candidateTaskCodes)) {
            return;
        }

        // 若没有登录客户号, 开通北交所权限任务无法判断是否可做, 因此排除该任务
        if (customer == null) {
            candidateTaskCodes.remove(TASK_2023DOUBLE11_33);
        }

        if (candidateTaskCodes.contains(TASK_2023DOUBLE11_33)) {
            // 校验该客户是否已开通科创板权限, 但未开通北交所权限
            if (!hengShengBiz.checkKcbNotBjsPerm(user, customer)) {
                candidateTaskCodes.remove(TASK_2023DOUBLE11_33);
            }
        }
    }
}
