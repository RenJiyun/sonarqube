package com.wlzq.activity.l2recieve.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.l2recieve.biz.L2Recieve2024Biz;
import com.wlzq.activity.l2recieve.dao.Level2ReceiveUser2024Dao;
import com.wlzq.activity.l2recieve.dao.Level2RecieveUserDao;
import com.wlzq.activity.l2recieve.model.Level2ReceiveUser2024;
import com.wlzq.activity.l2recieve.model.Level2RecieveUser;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author renjiyun
 */
@Service
@Slf4j
public class L2Recieve2024BizImpl implements L2Recieve2024Biz {

    public static final String ACTIVITY_CODE = "ACTIVITY.L2RECEIVE.2024";
    public static final String[] PRIZE_TYPES = {
            "PRIZE.L2RECEIVE.2024.01", // 新开户 - 一个月体验券
            "PRIZE.L2RECEIVE.2024.02", // 开通预约打新 - 一个月体验券
            "PRIZE.L2RECEIVE.2024.03", // 开通北交所权限 - 一个月体验券
            "PRIZE.L2RECEIVE.2024.04", // 开通科创板权限 - 三个月体验券
            "PRIZE.L2RECEIVE.2024.05", // 首次入金达标 - 三个月体验券
            "PRIZE.L2RECEIVE.2024.06"  // 新开两融账户 - 六个月体验券
    };


    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActPrizeDao actPrizeDao;
    @Autowired
    private CouponCommonReceiveBiz couponCommonReceiveBiz;
    @Autowired
    private Level2ReceiveUser2024Dao level2ReceiveUser2024Dao;
    @Autowired
    private Level2RecieveUserDao level2RecieveUserDao;

    @Override
    public StatusObjDto<List<CouponReceiveStatusDto>> receive(String prizeType, AccTokenUser user, Customer customer) {
        Activity activity = activityBaseBiz.findActivity(ACTIVITY_CODE);
        StatusDto actValidResult = activityBaseBiz.isValid(activity);
        if (!actValidResult.isOk()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(actValidResult.getMsg());
        }

        boolean prizeTypePresent = Arrays.asList(PRIZE_TYPES).contains(prizeType);
        if (!prizeTypePresent) {
            throw ActivityBizException.ACT_PRIZE_NOT_EXIST;
        }

        // 校验客户是否已经领取过
        ActPrize actPrizeQry = new ActPrize()
                .setActivityCode(ACTIVITY_CODE)
                .setCode(prizeType)
                .setCustomerId(customer.getCustomerId());
        List<ActPrize> customerPrizes = actPrizeDao.findList(actPrizeQry);
        if (CollectionUtil.isNotEmpty(customerPrizes)) {
            ActPrize customerPrize = customerPrizes.get(0);
            throw ActivityBizException.CUSTOMER_HAVE_RECEIVED.format(desensitizeMobile(customerPrize.getMobile()));
        }

        // 校验是否符合领取条件, 若不符合则抛出对应的业务异常
        checkRecieveCondition(activity, prizeType, user, customer);
        List<CouponReceiveStatusDto> couponReceiveStatusDtos =
                couponCommonReceiveBiz.receiveCoupon(activity, new String[]{prizeType}, null, user, customer);

        return new StatusObjDto<>(true, couponReceiveStatusDtos);
    }

    private void checkRecieveCondition(Activity activity, String prizeType, AccTokenUser user, Customer customer) {
        switch (prizeType) {
            case "PRIZE.L2RECEIVE.2024.01":
                // 新开户 - 一个月体验券
                checkNewCustomer(activity, user, customer);
                break;
            case "PRIZE.L2RECEIVE.2024.02":
                // 开通预约打新 - 一个月体验券
                checkYydx(activity, user, customer);
                break;
            case "PRIZE.L2RECEIVE.2024.03":
                // 开通北交所权限 - 一个月体验券
                checkBjs(activity, user, customer);
                break;
            case "PRIZE.L2RECEIVE.2024.04":
                // 开通科创板权限 - 三个月体验券
                checkKcb(activity, user, customer);
                break;
            case "PRIZE.L2RECEIVE.2024.05":
                // 首次入金达标 - 三个月体验券
                checkRj(activity, user, customer);
                break;
            case "PRIZE.L2RECEIVE.2024.06":
                // 新开两融账户 - 六个月体验券
                checkLr(activity, user, customer);
                break;
            default:
                throw ActivityBizException.ACT_PRIZE_NOT_EXIST;
        }
    }

    /**
     * 校验是否满足新开两融账户条件
     *
     * @param activity
     * @param user
     * @param customer
     */
    private void checkLr(Activity activity, AccTokenUser user, Customer customer) {
        Level2RecieveUser qry = new Level2RecieveUser();
        qry.setCustmerId(customer.getCustomerId());
        qry.setType(3);
        List<Level2RecieveUser> qryResult = level2RecieveUserDao.findList(qry);
        if (CollectionUtil.isEmpty(qryResult)) {
            throw ActivityBizException.L2_RECEIVE_T1.format("开通两融账户后T+1日即可领取~");
        }

        // sort by effectiveDate desc
        qryResult.sort((o1, o2) -> o2.getEffectiveDate().compareTo(o1.getEffectiveDate()));
        Level2RecieveUser latestRecord = qryResult.get(0);
        Date effectiveDate = latestRecord.getEffectiveDate();
        if (effectiveDate.before(activity.getDateFrom()) || effectiveDate.after(activity.getDateTo())) {
            throw ActivityBizException.L2_RECEIVE_NOT_ACTIVITY_TIME.format("仅限活动期内开通的客户领取哦~");
        }
    }

    /**
     * 校验是否满足入金达标条件
     *
     * @param activity
     * @param user
     * @param customer
     */
    private void checkRj(Activity activity, AccTokenUser user, Customer customer) {
        Level2RecieveUser qry = new Level2RecieveUser();
        qry.setCustmerId(customer.getCustomerId());
        qry.setType(2);
        List<Level2RecieveUser> qryResult = level2RecieveUserDao.findList(qry);
        if (CollectionUtil.isEmpty(qryResult)) {
            throw ActivityBizException.L2_RECEIVE_RJ_CONDITION;
        }

        // 过滤掉当月的数据, 因为当月的记录还处于变动之中, 不足以判断是否达标
        Date startOfMonth = DateUtils.getFirstDayOfMonth(new Date());
        qryResult.removeIf(e -> {
            Date effectiveDate = e.getEffectiveDate();
            if (effectiveDate == null) {
                return true;
            }
            return effectiveDate.after(startOfMonth);
        });

        boolean isPresent = qryResult.stream().anyMatch(e -> {
            Date effectiveDate = e.getEffectiveDate();
            // 判断是否在活动时间内, 但是该项比较特殊, 有效期的开始时间是活动开始时间的前一个月
            Date startDate = new Date(activity.getDateFrom().getTime() - 30 * 24 * 60 * 60 * 1000L);
            return effectiveDate.after(startDate) && effectiveDate.before(activity.getDateTo());
        });

        if (!isPresent) {
            throw ActivityBizException.L2_RECEIVE_NOT_ACTIVITY_TIME.format("仅限活动期内满足条件的客户领取哦~");
        }
    }

    /**
     * 校验是否满足开通科创板权限条件
     *
     * @param activity
     * @param user
     * @param customer
     */
    private void checkKcb(Activity activity, AccTokenUser user, Customer customer) {
        Level2ReceiveUser2024 qry = new Level2ReceiveUser2024();
        qry.setCustomerId(customer.getCustomerId());
        qry.setType(6);
        List<Level2ReceiveUser2024> qryResult = level2ReceiveUser2024Dao.findList(qry);
        if (CollectionUtil.isEmpty(qryResult)) {
            throw ActivityBizException.L2_RECEIVE_T1.format("开通科创板权限后T+1日可领取~");
        }

        // sort by effectiveDate desc
        qryResult.sort((o1, o2) -> o2.getEffectiveDate().compareTo(o1.getEffectiveDate()));
        Level2ReceiveUser2024 latestRecord = qryResult.get(0);
        Date effectiveDate = latestRecord.getEffectiveDate();
        if (effectiveDate.before(activity.getDateFrom()) || effectiveDate.after(activity.getDateTo())) {
            throw ActivityBizException.L2_RECEIVE_NOT_ACTIVITY_TIME.format("仅限活动期内开通权限的客户领取哦~");
        }
    }

    /**
     * 校验是否满足开通北交所权限条件
     *
     * @param activity
     * @param user
     * @param customer
     */
    private void checkBjs(Activity activity, AccTokenUser user, Customer customer) {
        Level2ReceiveUser2024 qry = new Level2ReceiveUser2024();
        qry.setCustomerId(customer.getCustomerId());
        qry.setType(5);
        List<Level2ReceiveUser2024> qryResult = level2ReceiveUser2024Dao.findList(qry);
        if (CollectionUtil.isEmpty(qryResult)) {
            throw ActivityBizException.L2_RECEIVE_T1.format("开通北交所权限后T+1日可领取~");
        }

        // sort by effectiveDate desc
        qryResult.sort((o1, o2) -> o2.getEffectiveDate().compareTo(o1.getEffectiveDate()));
        Level2ReceiveUser2024 latestRecord = qryResult.get(0);
        Date effectiveDate = latestRecord.getEffectiveDate();
        if (effectiveDate.before(activity.getDateFrom()) || effectiveDate.after(activity.getDateTo())) {
            throw ActivityBizException.L2_RECEIVE_NOT_ACTIVITY_TIME.format("仅限活动期内开通权限的客户领取哦~");
        }
    }

    /**
     * 校验是否满足开通预约打新条件
     *
     * @param activity
     * @param user
     * @param customer
     */
    private void checkYydx(Activity activity, AccTokenUser user, Customer customer) {
        Level2ReceiveUser2024 qry = new Level2ReceiveUser2024();
        qry.setCustomerId(customer.getCustomerId());
        qry.setType(4);
        List<Level2ReceiveUser2024> qryResult = level2ReceiveUser2024Dao.findList(qry);
        if (CollectionUtil.isEmpty(qryResult)) {
            throw ActivityBizException.L2_RECEIVE_T1.format("签约预约打新后T+1日可领取~");
        }

        // sort by effectiveDate desc
        qryResult.sort((o1, o2) -> o2.getEffectiveDate().compareTo(o1.getEffectiveDate()));
        Level2ReceiveUser2024 latestRecord = qryResult.get(0);
        Date effectiveDate = latestRecord.getEffectiveDate();
        if (effectiveDate.before(activity.getDateFrom()) || effectiveDate.after(activity.getDateTo())) {
            throw ActivityBizException.L2_RECEIVE_NOT_ACTIVITY_TIME.format("仅限活动期内新签约客户领取哦~");
        }
    }

    /**
     * 校验开户时间是否在活动时间内
     *
     * @param activity
     * @param user
     * @param customer
     */
    private void checkNewCustomer(Activity activity, AccTokenUser user, Customer customer) {
        Date openDate = customer.getOpenDate();
        if (openDate == null) {
            throw ActivityBizException.ACT_ILL_OPENDATE.format("仅限活动期内新开户用户领取哦~");
        }

        if (openDate.before(activity.getDateFrom()) || openDate.after(activity.getDateTo())) {
            throw ActivityBizException.ACT_ILL_OPENDATE.format("仅限活动期内新开户用户领取哦~");
        }
    }

    private String desensitizeMobile(String mobile) {
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

}
