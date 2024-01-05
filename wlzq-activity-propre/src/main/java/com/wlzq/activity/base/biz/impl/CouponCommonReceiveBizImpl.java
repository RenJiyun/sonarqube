package com.wlzq.activity.base.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.cons.PriceTimesTypeEnum;
import com.wlzq.activity.base.cons.PrizeReceiveStatusEnum;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dao.ActPrizeTypeDao;
import com.wlzq.activity.base.dto.*;
import com.wlzq.activity.base.model.*;
import com.wlzq.activity.task.biz.impl.FreeCourseBizImpl;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.AccUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.model.account.Staff;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.common.utils.StarReplaceUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.AccountUserBiz;
import com.wlzq.remote.service.common.account.StaffBiz;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.remote.service.common.base.PushBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.wlzq.activity.double11.biz.Double11Biz.ACTIVITY_2023DOUBLE11_XTJJDS;

/**
 * @author louie
 */
@Service
@Slf4j
public class CouponCommonReceiveBizImpl extends ActivityBaseBiz implements CouponCommonReceiveBiz {

    //领券活动编码配置
    private static final String ACTIVITIES_CONFIG = "activity.couponrecieve.activities";
    private static final String ACTIVITY_SPRING2021_COUPON = "ACTIVITY.SPRING2021.COUPON";
    private static final String ACTIVITY_PRIZE_SMS_TEMPLATE = "ACTIVITY.PRIZE.SMS.TEMPLATE";
    private static final String ACT_NEWCUST_2O2O = "ACTIVITY.2020.NEWCUST";
    private static final String ACT_NEWCUST_2O2O_PRIZE = "PRIZE.COUPON.INVEST.B1A002.4.RIGHTS.2020-09-05";
    private static final Map<String, String> PRIZE_COUPON_TEMPLATES;
    private static final Map<String, Integer> COUPON_DAILY_LIMIT;
    private static final String ACT_DELAYOREXPIREREPICK_PRIZES = "act.delayorexpirerepick.prizes";
    private static final String ACT_RECOMMENDERMULTI_PRIZES = "act.recommendermulti.prizes";
    private static final String ACT_USEDMULTI_PRIZES = "act.usedmulti.prizes";
    private static final String GLOBAL_UNIQUE_PRIZES = "act.global.unique.prizes";
    private static final String NO_NEED_CUSTOMER_LOGIN_PRIZE_TYPE = "act.noneedcustomerlogin.prizes";

    public static final String NEW_CUSTOMER_FINANCE_PRIZE_TYPE = "PRIZE.COUPON.INVEST.B1A002.4.RIGHTS.2020-09-05";

    // 新客理财券二期券码
    public static final String NEW_CUSTOMER_FINANCE_PRIZE_TYPE_2023_09 = "COUPON.INVEST.NEW.CUSTOMER.RIGHTS.2023-09";
    public static final String NEW_CUSTOMER_VAS_PRIZE_TYPE = "PRIZE.COUPON.FKYB.FREE.7.RECEIVE";
    public static final String NEW_CUSTOMER_LEVEL2_PRIZE_TYPE = "PRIZE.NEW.CUSTOMER.GIFTBAG.LEVEL2.1MONTH";
    public static final String NEW_CUSTOMER_INVESTMENT_PRIZE_TYPE = "PRIZE.NEW.CUSTOMER.GIFTBAG.INVESTMENT.14DAYS";

    static {
        PRIZE_COUPON_TEMPLATES = new HashMap<String, String>();
        PRIZE_COUPON_TEMPLATES.put("PRIZE.DOUBLE11_2019.COUPON.DTCP003", "COUPON.INVEST.DOUBLE11FORSALE.DTCP003");
        PRIZE_COUPON_TEMPLATES.put("PRIZE.DOUBLE11_2019.COUPON.DTCP004", "COUPON.INVEST.DOUBLE11FORSALE.DTCP004");
        PRIZE_COUPON_TEMPLATES.put("PRIZE.DOUBLE11_2019.COUPON.DTCP005", "COUPON.INVEST.DOUBLE11FORSALE.DTCP005");
        PRIZE_COUPON_TEMPLATES.put("PRIZE.DOUBLE11_2019.COUPON.DTCP007", "COUPON.INVEST.DOUBLE11FORSALE.DTCP007");
        PRIZE_COUPON_TEMPLATES.put("PRIZE.DOUBLE11_2019.COUPON.DTCP008", "COUPON.INVEST.DOUBLE11FORSALE.DTCP008");
        PRIZE_COUPON_TEMPLATES.put("PRIZE.DOUBLE11_2019.COUPON.DTCP010", "COUPON.INVEST.DOUBLE11FORSALE.DTCP010");

        COUPON_DAILY_LIMIT = new HashMap<String, Integer>();
        COUPON_DAILY_LIMIT.put("PRIZE.COUPON.INVEST.COMMON.2..2021-01-26", 15);
        COUPON_DAILY_LIMIT.put("PRIZE.COUPON.INVEST.COMMON.1.8.8.2021-01-26", 2000);
        COUPON_DAILY_LIMIT.put("PRIZE.COUPON.INVEST.COMMON.1.7.2021-01-26", 100);
        COUPON_DAILY_LIMIT.put("PRIZE.COUPON.INVEST.COMMON.1.6.8.2021-01-26", 50);
        COUPON_DAILY_LIMIT.put("PRIZE.COUPON.INVEST.B1A002.4.RIGHTS.2021-01-26", 100);
    }

    @Autowired
    private AccountUserBiz userBiz;
    @Autowired
    private PushBiz pushBiz;
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private CouponBiz couponBiz;
    @Autowired
    private ActPrizeDao prizeDao;
    @Autowired
    private ActPrizeTypeDao actPrizeTypeDao;
    @Autowired
    private StaffBiz staffBiz;
    @Autowired
    private FreeCourseBizImpl freeCourseBiz;
    @Autowired
    private ActPrizeTypeBiz actPrizeTypeBiz;


    @Override
    public StatusObjDto<List<CouponRecieveActivityDto>> findActivities(Integer plate, String productCode) {
        List<String> activityCodes = AppConfigUtils.getList(ACTIVITIES_CONFIG, ",");
        if (activityCodes == null || activityCodes.size() == 0) {
            return new StatusObjDto<List<CouponRecieveActivityDto>>(true, Lists.newArrayList(), StatusDto.SUCCESS, "");
        }
        List<CouponRecieveActivityDto> validActivities = Lists.newArrayList();
        for (String activityCode : activityCodes) {
            StatusDto isValidAct = isValid(activityCode);
            if (!isValidAct.isOk()) continue;
            List<String> prizeTypes = Lists.newArrayList();
            List<ActPrize> prizes = actPrizeBiz.findDistinctPrizes(activityCode, ActPrize.TYPE_COUPON);
            for (ActPrize prize : prizes) {
                if (!prize.getType().equals(ActPrize.TYPE_COUPON)) continue;
                if (ObjectUtils.isNotEmptyOrNull(plate)) {//查询该优惠券是否适用于该平台及产品
                    StatusObjDto<CouponInfo> applicableStatus = couponBiz.checkIsApplicable(null, prize.getRedeemCode(), plate, productCode, null);
                    if (applicableStatus.isOk()) {
                        prizeTypes.add(prize.getCode());
                    }
                } else {
                    prizeTypes.add(prize.getCode());
                }
            }
            if (prizeTypes.size() == 0) continue;
            CouponRecieveActivityDto couponActivity = new CouponRecieveActivityDto();
            couponActivity.setActivityCode(activityCode);
            couponActivity.setPrizeTypes(prizeTypes);
            validActivities.add(couponActivity);
        }

        return new StatusObjDto<List<CouponRecieveActivityDto>>(true, validActivities, StatusDto.SUCCESS, "");
    }

    @Override
    public StatusObjDto<CouponRecieveStatusDto> receive(String activityCode, String prizeType,
                                                        String userId, String openId,
                                                        String customerId, String recommendCode, String remark, String mobile) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("acitivityCode");
        }
        if (ObjectUtils.isEmptyOrNull(prizeType)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }
        StatusDto isValidAct = isValid(activityCode);
        if (!isValidAct.isOk()) {
            return new StatusObjDto<CouponRecieveStatusDto>(false, isValidAct.getCode(), isValidAct.getMsg());
        }
        /* 2021年春节领取活动例外,特殊设置leftCount**/
        if (ACTIVITY_SPRING2021_COUPON.equals(activityCode)) {
            Integer leftCount = resetLetfCount(activityCode, prizeType);
            if (leftCount <= 0) {
                throw ActivityBizException.DOUBLE_RECIEVE_COMPLETE;
            }
        }

        //不可重复， 判断有没有获取过奖品
        List<ActPrize> prizes = actPrizeBiz.findPrize(activityCode, customerId, userId, openId, prizeType, null, remark);
        if (prizes.size() > 0) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过该奖品");
        }

        //不在2019.双11活动的奖品
        if (!PRIZE_COUPON_TEMPLATES.containsKey(prizeType)) {
            AcReceivePriceVO acReceivePriceVOItem = new AcReceivePriceVO().setActivityCode(activityCode)
                    .setPrizeType(prizeType).setUserId(userId).setOpenId(openId).setCustomerId(customerId)
                    .setRecommendCode(recommendCode).setRemark(remark);
            ActPrize prize = actPrizeBiz.giveOutPrize(acReceivePriceVOItem, null, mobile);
            if (prize == null) {
                throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
            }
            prize.setUpdateTime(new Date());
            CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
            StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
            MyPrizeDto myPrizeDto = buildMyPrizeDto(prize, coupon.getObj());
            statusDto.setPrize(myPrizeDto);
            statusDto.setPrizeType(prizeType);
            statusDto.setStatus(ActPrize.STATUS_SEND);
            return new StatusObjDto<CouponRecieveStatusDto>(true, statusDto, StatusDto.SUCCESS, "");
        }

        return oldActivityReceive(prizeType, userId, openId, customerId, recommendCode, remark, mobile);
    }

    @Override
    public StatusObjDto<CouponRecieveStatusDto> receive(String activityCode, String prizeType,
                                                        String userId, String openId,
                                                        String customerId, String recommendCode, String remark) {

        return receive(activityCode, prizeType, userId, openId, customerId, recommendCode, remark, "");
    }

    @Override
    public StatusObjDto<CouponRecieveStatusDto> receiveByUserId(String activityCode, String prizeType, String userId, String openId, String customerId, String recommendCode, String remark) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("acitivityCode");
        }
        if (ObjectUtils.isEmptyOrNull(prizeType)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }
        StatusDto isValidAct = isValid(activityCode);
        if (!isValidAct.isOk()) {
            return new StatusObjDto<CouponRecieveStatusDto>(false, isValidAct.getCode(), isValidAct.getMsg());
        }
        AccUser user = userBiz.findByUserId(userId);
        if (user == null) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("用户不存在");
        }

        //不可重复， 判断有没有获取过奖品
        List<ActPrize> prizes = actPrizeBiz.findPrize(activityCode, null, userId, null, prizeType, null, remark);
        if (prizes.size() > 0) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过该奖品");
        }

        /**2021年春节领取活动例外,特殊设置leftCount**/
        if (ACTIVITY_SPRING2021_COUPON.equals(activityCode)) {
            Integer leftCount = resetLetfCount(activityCode, prizeType);
            if (leftCount <= 0) {
                throw ActivityBizException.DOUBLE_RECIEVE_COMPLETE;
            }
        }

        //领取奖品
        AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO().setActivityCode(activityCode)
                .setPrizeType(prizeType).setUserId(userId).setOpenId(openId).setCustomerId(customerId)
                .setRecommendCode(recommendCode).setRemark(remark);
        ActPrize prize = actPrizeBiz.giveOutPrize(acReceivePriceVO, null, user.getMobile());
        if (prize == null) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
        }
        prize.setUpdateTime(new Date());


        //优惠券
        StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
        //转为奖品DTO
        MyPrizeDto myPrizeDto = buildMyPrizeDto(prize, coupon.getObj());

        CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
        statusDto.setPrize(myPrizeDto);
        statusDto.setPrizeType(prizeType);
        statusDto.setStatus(ActPrize.STATUS_SEND);

        StatusDto result = null;
        List<Object> msgParams = new ArrayList<Object>();
        if ("ACTIVITY.2021618.BUYL2.COUPON.FEEDBACK".equals(activityCode)) {
            try {
                msgParams.add(StarReplaceUtils.replaceStarAction(user.getMobile()));
                result = pushBiz.sendSms(ACTIVITY_PRIZE_SMS_TEMPLATE, user.getMobile(), msgParams);
                log.info("优惠券提醒发送成功:mobile:{},msgParams:{};code:{};data:{}", user.getMobile(), msgParams, result.getCode(), result.getMsg());
            } catch (Exception ex) {
                log.info("优惠券提醒发送失败:mobile:{},msgParams:{};code:{};data:{}", user.getMobile(), msgParams, result.getCode(), result.getMsg(), ex);
            }
        }
        return new StatusObjDto<CouponRecieveStatusDto>(true, statusDto, StatusDto.SUCCESS, "");
    }

    /**
     * 领取奖品
     *
     * @return 不返回敏感信息
     */
    @Override
    public StatusObjDto<List<PrizeReceiveSimpDto>> receivePriceCoop(AcReceivePriceVO acReceivePriceVO) {
        AccUser user = userBiz.findByUserId(acReceivePriceVO.getUserId());
        if (user == null) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("用户不存在");
        }

        acReceivePriceVO.setMobile(user.getMobile());
        List<CouponRecieveStatusDto> statusObjDto = doReceivePriceCommon(acReceivePriceVO, true);
        List<PrizeReceiveSimpDto> res = statusObjDto.stream()
                .map(e -> new PrizeReceiveSimpDto().setStatus(e.getStatus())
                        .setPrizeType(e.getPrizeType())
                        .setPrizeName(e.getPrizeName()))
                .collect(Collectors.toList());
        return new StatusObjDto<>(true, res, StatusDto.SUCCESS, "");
    }


    public static boolean checkNewCustomer(Customer customer) {
        boolean re = true;
        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("customerId", customer.getCustomerId());
        ResultDto finOrderDto = RemoteUtils.call("fin.ordercooperation.succount", ApiServiceTypeEnum.COOPERATION, busparams, true);
        if (ResultDto.SUCCESS.equals(finOrderDto.getCode()) && finOrderDto.getData() != null) {
            Integer count = (Integer) finOrderDto.getData().get("count");
            if (count != null && count.compareTo(0) == 1) {
                re = false;
            }
        }
        if (ResultDto.FAIL_COMMON.equals(finOrderDto.getCode())) {
            re = false;
        }
        return re;
    }


    @Override
    public List<CouponRecieveStatusDto> receiveNewCustomerGiftBag(AcReceivePriceVO acReceivePriceVO, Customer customer) {
        if (ACT_NEWCUST_2O2O.equals(acReceivePriceVO.getActivityCode())) {
            boolean b = checkNewCustomer(customer);
            if (!b) {
                return new ArrayList<>();
            }
        } else {
            //新客大礼包领取条件： 领取有效期为开户后31天内，领取奖品如下
            Date openDate = customer.getOpenDate();
            if (openDate == null) {
                log.info("不在新客理财券领取的时间范围。");
                return new ArrayList<>();
            }
            Date now = new Date();
            DateTime openDate1 = DateUtil.beginOfDay(openDate);
            DateTime now1 = DateUtil.beginOfDay(now);

            long days = DateUtil.between(openDate1, now1, DateUnit.DAY);
            if (openDate1.getTime() > now1.getTime() || days > 31) {
                log.info("不在新客理财券领取的时间范围。开户日期：{}", openDate);
                return new ArrayList<>();
            }
        }

        acReceivePriceVO.setContinueFlag(true);
        return doReceivePriceCommon(acReceivePriceVO, true);
    }

    @Override
    public StatusObjDto<List<CouponRecieveStatusDto>> receiveGiftBag(AcReceivePriceVO acReceivePriceVO, Customer customer) {
        String mobile = acReceivePriceVO.getMobile();
        if (StringUtils.isBlank(mobile)) {
            throw BizException.USER_NOT_BIND_MOBILE;
        }
        String customerId = customer.getCustomerId();
        if (StringUtils.isBlank(customerId)) {
            throw BizException.CUSTOMER_NOT_LOGIN_ERROR;
        }

        //新客大礼包领取条件： 领取有效期为开户后180天内
        Date openDate = customer.getOpenDate();
        if (openDate == null) {
            log.info("不在新客理财券领取的时间范围。");
            return new StatusObjDto<>(false, StatusDto.FAIL_COMMON, "不在领取的时间范围");
        }
        Date now = new Date();
        DateTime openDate1 = DateUtil.beginOfDay(openDate);
        DateTime now1 = DateUtil.beginOfDay(now);

        long days = DateUtil.between(openDate1, now1, DateUnit.DAY);
        if (openDate1.getTime() > now1.getTime() || days > 180) {
            log.info("不在新客理财券领取的时间范围。开户日期：{}", openDate);
            return new StatusObjDto<>(false, StatusDto.FAIL_COMMON, "开户日期不在活动范围内");
        }

        Integer riskLevel = customer.getRiskLevel();
        if (riskLevel == null || riskLevel.compareTo(11) < 0 || riskLevel.compareTo(15) > 0) {
            return new StatusObjDto<>(false, StatusDto.FAIL_COMMON, "风险评测不符合");
        }

        Date riskEndDate = customer.getRiskEndDate();
        if (riskEndDate == null || now.after(riskEndDate)) {
            return new StatusObjDto<>(false, StatusDto.FAIL_COMMON, "风险评测已过期");
        }

        List<CouponRecieveStatusDto> re = new ArrayList<>();
        String activityCode = acReceivePriceVO.getActivityCode();
        String userId = acReceivePriceVO.getUserId();

        //投顾服务14天免单券
        AcReceivePriceVO receiveVO4 = new AcReceivePriceVO().setContinueFlag(true)
                .setActivityCode(activityCode)
                .setCustomerId(customerId)
                .setMobile(mobile)
                .setUserId(userId)
                .setPrizeType(NEW_CUSTOMER_INVESTMENT_PRIZE_TYPE)
                .setGlobalUniquePrizeType(false)
                .setUniqueType(AcReceivePriceVO.UNIQUE_CUSTOMER)
                .setCustomerDimension(true)
                .setMobileDimension(false);
        CouponRecieveStatusDto statusDto4 = receiveCoupon(receiveVO4, true);

        // 投顾服务14天免单券的领取结果, 将作为是否已经参加过新客大礼包的标识
        if (StringUtils.equals(statusDto4.getActivityCode(), activityCode)
                && statusDto4.isLastHadReceive() && StringUtils.equals(customerId, statusDto4.getLastCustomerId())) {
            return new StatusObjDto<>(true, re, StatusDto.SUCCESS, "已经领取过新客大礼包");
        }

        //A股level2一个月
        AcReceivePriceVO receiveVO3 = new AcReceivePriceVO().setContinueFlag(true)
                .setActivityCode(activityCode)
                .setCustomerId(customerId)
                .setMobile(mobile)
                .setUserId(userId)
                .setPrizeType(NEW_CUSTOMER_LEVEL2_PRIZE_TYPE)
                .setGlobalUniquePrizeType(false)
                .setUniqueType(AcReceivePriceVO.UNIQUE_MOBILE_OR_CUSTOMER)
                .setCustomerDimension(true)
                .setMobileDimension(false);
        CouponRecieveStatusDto statusDto3 = receiveCoupon(receiveVO3, true);

        // 2023.10.01 起, 将领取更新后的新客理财券
        // 新券的领取条件为: 该客户全渠道未领取过旧的新客理财券
        CouponRecieveStatusDto statusDto1 = null;
        Date secondStartDate = DateUtil.parse(
                AppConfigUtils.get("activity.prize.new.customer.giftbag.second", "2023-10-01"));

        AcReceivePriceVO receiveVO1 = new AcReceivePriceVO().setContinueFlag(true)
                .setActivityCode(activityCode)
                .setCustomerId(customerId)
                .setMobile(mobile)
                .setUserId(userId)
                .setPrizeType(NEW_CUSTOMER_FINANCE_PRIZE_TYPE)
                .setGlobalUniquePrizeType(true)
                .setUniqueType(AcReceivePriceVO.UNIQUE_CUSTOMER)
                .setCustomerDimension(true)
                .setMobileDimension(false);
        if (now.after(secondStartDate)) {
            // 1. 检查旧的新客理财券是否已经领取
            ActPrizeOperVO checkResult = checkMultiReceiveAndGetNewPrizeType(receiveVO1);
            if (ActPrizeOperVO.OPER_HAD_RECEIVE.equals(checkResult.getOperFlag())) {
                statusDto1 = receiveCoupon(receiveVO1, false);
            } else {
                // 2. 若没有领取, 则领取新的新客理财券
                receiveVO1.setPrizeType(NEW_CUSTOMER_FINANCE_PRIZE_TYPE_2023_09);
                statusDto1 = receiveCoupon(receiveVO1, true);
            }
        } else {
            // 若还在一期的领取时间内, 则领取旧的新客理财券
            statusDto1 = receiveCoupon(receiveVO1, true);
        }

        //决策工具券（风口研报7天）
        String changeablePrizeType = AppConfigUtils.get("activity.prize.new.customer.giftbag.changeable", NEW_CUSTOMER_VAS_PRIZE_TYPE);
        AcReceivePriceVO receiveVO2 = new AcReceivePriceVO().setContinueFlag(true)
                .setActivityCode(activityCode)
                .setCustomerId(customerId)
                .setMobile(mobile)
                .setUserId(userId)
                .setPrizeType(changeablePrizeType)
                .setGlobalUniquePrizeType(true)
                .setUniqueType(AcReceivePriceVO.UNIQUE_MOBILE_OR_CUSTOMER)
                .setCustomerDimension(false)
                .setMobileDimension(true);
        CouponRecieveStatusDto statusDto2 = receiveCoupon(receiveVO2, true);

        re.add(statusDto1);
        re.add(statusDto2);
        re.add(statusDto3);
        re.add(statusDto4);
        return new StatusObjDto<>(true, re, StatusDto.SUCCESS, "");
    }


    /**
     * 不用登录客户号就可以领取的奖品编码
     *
     * @param prizeTypes
     */
    @Override
    public boolean isNoNeedCustomerLoginPrizeType(String prizeTypes) {
        if (StringUtils.isBlank(prizeTypes)) {
            return false;
        }
        String[] prizeTypeArr = prizeTypes.split(",");

        //不用登录客户号就可以领取的奖品编码
        List<String> list = AppConfigUtils.getList(NO_NEED_CUSTOMER_LOGIN_PRIZE_TYPE, ",");
        for (String prizeType : prizeTypeArr) {
            if (!list.contains(prizeType)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public List<CouponRecieveStatusDto> receivePriceCommon(AcReceivePriceVO acReceivePriceVO) {
        String activityCode = acReceivePriceVO.getActivityCode();
        if (ACT_NEWCUST_2O2O.equals(activityCode)) {
            String customerId = acReceivePriceVO.getCustomerId();
            if (StringUtils.isBlank(customerId)) {
                throw BizException.SD_NOT_LOGIN_ERROR;
            }

            List<CouponRecieveStatusDto> list = new ArrayList<>();
            AcReceivePriceVO receiveVO1 = new AcReceivePriceVO().setContinueFlag(true)
                    .setActivityCode(activityCode)
                    .setCustomerId(customerId)
                    .setUserId(acReceivePriceVO.getUserId())
                    .setMobile(acReceivePriceVO.getMobile())
                    .setPrizeType(NEW_CUSTOMER_FINANCE_PRIZE_TYPE)
                    .setGlobalUniquePrizeType(true)
                    .setUniqueType(AcReceivePriceVO.UNIQUE_CUSTOMER)
                    .setCustomerDimension(true)
                    .setMobileDimension(false);
            CouponRecieveStatusDto statusDto = receiveCoupon(receiveVO1, false);
            list.add(statusDto);
            return list;
        }

        if (NEW_CUSTOMER_VAS_PRIZE_TYPE.equals(acReceivePriceVO.getPrizeType())) {
            String mobile = acReceivePriceVO.getMobile();
            if (StringUtils.isBlank(mobile)) {
                throw BizException.USER_NOT_BIND_MOBILE;
            }

            List<CouponRecieveStatusDto> list = new ArrayList<>();
            AcReceivePriceVO receiveVO1 = new AcReceivePriceVO().setContinueFlag(true)
                    .setActivityCode(activityCode)
                    .setCustomerId(acReceivePriceVO.getCustomerId())
                    .setUserId(acReceivePriceVO.getUserId())
                    .setMobile(acReceivePriceVO.getMobile())
                    .setPrizeType(NEW_CUSTOMER_VAS_PRIZE_TYPE)
                    .setGlobalUniquePrizeType(true)
                    .setUniqueType(AcReceivePriceVO.UNIQUE_MOBILE)
                    .setCustomerDimension(true)
                    .setMobileDimension(false);
            CouponRecieveStatusDto statusDto = receiveCoupon(receiveVO1, false);
            list.add(statusDto);
            return list;
        }

        return doReceivePriceCommon(acReceivePriceVO, false);
    }


    @Override
    public List<CouponReceiveStatusDto> receiveCoupon(Activity activity, String[] prizeTypeCodes,
                                                      List<ActPrize> receivedPrizeList,
                                                      AccTokenUser user, Customer customer) {

        List<ActPrizeType> prizeTypeList = Arrays.stream(prizeTypeCodes)
                .map(code -> actPrizeTypeBiz.getPrizeType(code))
                .collect(Collectors.toList());
        return receiveCoupon(activity, prizeTypeList, receivedPrizeList, user, customer, "");
    }


    @Override
    public List<CouponReceiveStatusDto> receiveCoupon(Activity activity, List<ActPrizeType> prizeTypeList,
                                                      List<ActPrize> receivedPrizeList,
                                                      AccTokenUser user, Customer customer, String remark) {

        List<CouponReceiveStatusDto> receiveResultList = new ArrayList<>();

        for (ActPrizeType prizeType : prizeTypeList) {
            if (prizeType == null) {
                // 如果奖品类型不存在，则跳过, 不进行报错处理
                continue;
            }

            if (prizeType.getDailyLimit() != null && prizeType.getDailyLimit() > 0
                    && !ACTIVITY_2023DOUBLE11_XTJJDS.equals(activity.getCode())) {
                // 校验是否超出每日限额
                Date now = new Date();
                Integer dailyLimit = prizeType.getDailyLimit();
                Integer dailySendCount = actPrizeBiz.findPrizeCount(activity.getCode(), prizeType.getCode(), ActPrize.STATUS_SEND,
                        DateUtil.beginOfDay(now), DateUtil.endOfDay(now), null, null);
                if (dailySendCount >= dailyLimit) {
                    continue;
                }
            }

            if (actPrizeTypeBiz.canReceive(prizeType, receivedPrizeList, activity, false)) {
                // 发放优惠券
                ActPrize prize = actPrizeBiz.giveOutCoupon(activity, prizeType.getCode(), user, customer, remark);

                // 设置该奖项在活动中的领取状态
                CouponReceiveStatusDto receiveStatusDto = new CouponReceiveStatusDto();
                receiveStatusDto.setPrizeName(prizeType.getName());
                receiveStatusDto.setPrizeType(prizeType.getCode());
                Integer leftCount = actPrizeBiz.findPrizeCount(activity.getCode(), prizeType.getCode(), ActPrize.STATUS_NOT_SEND,
                        null, null, null, null);
                Integer allCount = actPrizeBiz.findPrizeCount(activity.getCode(), prizeType.getCode(), null,
                        null, null, null, null);
                receiveStatusDto.setLeftCount(leftCount);
                receiveStatusDto.setAllCount(allCount);

                // 查询优惠券系统相关信息用于设置我的奖品信息
                StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
                PrizeDto prizeDto = buildPrizeDto(prize, coupon.getObj());
                receiveStatusDto.setPrize(prizeDto);
                receiveStatusDto.setStatus(CouponRecieveStatusDto.STATUS_RECIEVED);
                receiveResultList.add(receiveStatusDto);
            }
        }
        return receiveResultList;
    }

    /**
     * @param acReceivePriceVO
     * @param simpleReturn     是否需要返回奖品的详细信息
     * @return
     */
    private List<CouponRecieveStatusDto> doReceivePriceCommon(AcReceivePriceVO acReceivePriceVO, boolean simpleReturn) {
        String activityCode = acReceivePriceVO.getActivityCode();
        String prizeType = acReceivePriceVO.getPrizeType();

        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(prizeType)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }

        Activity act = findActivity(activityCode);
        StatusDto valid = isValid(act);
        if (!valid.isOk()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(valid.getMsg());
        }

        if (ObjectUtils.isEmptyOrNull(acReceivePriceVO.getUserId()) && CodeConstant.CODE_YES.equals(acReceivePriceVO.getNeedUserId())) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        List<String> userIdNeedPrizes = AppConfigUtils.getList("activity.useridneed.prizes", ",");
        if (userIdNeedPrizes.contains(acReceivePriceVO.getPrizeType()) && ObjectUtils.isEmptyOrNull(acReceivePriceVO.getUserId())) {
            throw BizException.NOT_LOGIN_ERROR;
        }

        if (StringUtils.equals(activityCode, FreeCourseBizImpl.ACTIVITY_CODE_GET_REDPACK)) {
            StatusDto statusDto = freeCourseBiz.checkBeforeReceive(acReceivePriceVO.getUserId(), acReceivePriceVO.getCustomerId(), act);
            if (!statusDto.isOk()) {
                throw ActivityBizException.ACT_TASK_NOT_DONE.format(statusDto.getMsg());
            }
            acReceivePriceVO.setCustomerDimension(true);
            acReceivePriceVO.setMobileDimension(true);
        } else if (StringUtils.equals(activityCode, FreeCourseBizImpl.ACTIVITY_CODE)) {
            acReceivePriceVO.setCustomerDimension(true);
            acReceivePriceVO.setMobileDimension(true);
            acReceivePriceVO.setContinueFlag(true);
        }


        //检测是否重复领取奖品。继续领取的话，如果就领取其余的
        List<ActPrizeOperVO> newPrizeTypes = checkMultiReceiveAndGetNewPrizeTypes(acReceivePriceVO);

        //领取奖品
        List<CouponRecieveStatusDto> re = new ArrayList<>();
        for (ActPrizeOperVO operVO : newPrizeTypes) {
            acReceivePriceVO.setPrizeType(operVO.getCode());
            Integer operFlag = operVO.getOperFlag();
            ActPrize prize = null;
            /**领取操作**/
            if (ActPrizeOperVO.OPER_FLAG_RECEIVE.equals(operFlag)) {
                prize = actPrizeBiz.giveOutPrize(acReceivePriceVO, null, acReceivePriceVO.getMobile());
                if (prize == null) {
                    throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
                }
            }
            /**更新操作**/
            if (ActPrizeOperVO.OPER_FLAG_UPD.equals(operFlag)) {
                prize = actPrizeBiz.delayPrize(operVO.getRedeemCode(), acReceivePriceVO.getRecommendCode());
            }
            if (prize == null) {
                throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
            }
            CouponRecieveStatusDto recieveStatusDto = new CouponRecieveStatusDto();
            recieveStatusDto.setPrizeName(prize.getName());
            recieveStatusDto.setStatus(ActPrize.STATUS_SEND);
            recieveStatusDto.setPrizeType(operVO.getCode());
            recieveStatusDto.setOperFlag(operFlag);
            /**推荐人不为空，设置推荐人**/
            if (ObjectUtils.isNotEmptyOrNull(acReceivePriceVO.getRecommendCode())) {
                StatusObjDto<Staff> staff = staffBiz.infoByMobile(acReceivePriceVO.getRecommendCode());
                if (staff.isOk() && staff.getObj() != null) {
                    recieveStatusDto.setRecommendName(staff.getObj().getName());
                }
            }
            if (!simpleReturn) {
                StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
                MyPrizeDto myPrizeDto = buildMyPrizeDto(prize, coupon.getObj());
                recieveStatusDto.setPrize(myPrizeDto);
            }
            re.add(recieveStatusDto);
        }
        return re;
    }


    /**
     * @param acReceivePriceVO
     * @param simpleReturn     是否需要返回奖品的详细信息
     * @return
     */
    private CouponRecieveStatusDto receiveCoupon(AcReceivePriceVO acReceivePriceVO, boolean simpleReturn) {
        String activityCode = acReceivePriceVO.getActivityCode();
        String prizeType = acReceivePriceVO.getPrizeType();

        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(prizeType)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }

        Activity act = findActivity(activityCode);
        StatusDto valid = isValid(act);
        if (!valid.isOk()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(valid.getMsg());
        }

        if (ObjectUtils.isEmptyOrNull(acReceivePriceVO.getUserId()) && CodeConstant.CODE_YES.equals(acReceivePriceVO.getNeedUserId())) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        List<String> userIdNeedPrizes = AppConfigUtils.getList("activity.useridneed.prizes", ",");
        if (userIdNeedPrizes.contains(acReceivePriceVO.getPrizeType()) && ObjectUtils.isEmptyOrNull(acReceivePriceVO.getUserId())) {
            throw BizException.NOT_LOGIN_ERROR;
        }


        //检测是否重复领取奖品。继续领取的话，如果就领取其余的
        ActPrizeOperVO operVO = checkMultiReceiveAndGetNewPrizeType(acReceivePriceVO);


        acReceivePriceVO.setPrizeType(operVO.getCode());
        Integer operFlag = operVO.getOperFlag();
        ActPrize prize = null;
        CouponRecieveStatusDto recieveStatusDto = new CouponRecieveStatusDto();
        recieveStatusDto.setPrizeType(operVO.getCode());

        /* 领取操作**/
        if (ActPrizeOperVO.OPER_FLAG_RECEIVE.equals(operFlag)) {
            prize = actPrizeBiz.giveOutPrize(acReceivePriceVO, null, acReceivePriceVO.getMobile());
            if (prize == null) {
                throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
            }
        } else if (ActPrizeOperVO.OPER_HAD_RECEIVE.equals(operFlag)) {
            //上次已领取(而且不能再次领取)
            prize = new ActPrize().setRedeemCode(operVO.getRedeemCode());
            recieveStatusDto.setStatus(CouponRecieveStatusDto.STATUS_RECIEVED).setActivityCode(operVO.getLastActivityCode())
                    .setLastHadReceive(true).setLastCustomerId(operVO.getLastCustomerId());
        } else if (ActPrizeOperVO.OPER_FLAG_UPD.equals(operFlag)) {
            /* 更新操作**/
            prize = actPrizeBiz.delayPrize(operVO.getRedeemCode(), acReceivePriceVO.getRecommendCode());
        }
        if (prize == null) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
        }

        recieveStatusDto.setPrizeName(prize.getName());
        recieveStatusDto.setStatus(CouponRecieveStatusDto.STATUS_RECIEVED);
        recieveStatusDto.setOperFlag(operFlag);

        /*推荐人不为空，设置推荐人**/
        if (ObjectUtils.isNotEmptyOrNull(acReceivePriceVO.getRecommendCode())) {
            StatusObjDto<Staff> staff = staffBiz.infoByMobile(acReceivePriceVO.getRecommendCode());
            if (staff.isOk() && staff.getObj() != null) {
                recieveStatusDto.setRecommendName(staff.getObj().getName());
            }
        }
        if (!simpleReturn) {
            StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
            MyPrizeDto myPrizeDto = buildMyPrizeDto(prize, coupon.getObj());
            recieveStatusDto.setPrize(myPrizeDto);
        }

        return recieveStatusDto;
    }


    /**
     * 检测是否重复领取奖品
     */
    private List<ActPrizeOperVO> checkMultiReceiveAndGetNewPrizeTypes(AcReceivePriceVO acReceivePriceVO) {
        String[] prizeTypes = acReceivePriceVO.getPrizeType().split(",");
        List<String> toDeletePrizeTypes = new ArrayList<>(prizeTypes.length);
        /**可更新奖品**/
        Set<ActPrizeOperVO> updOperVO = Sets.newHashSet();

        ActPrize queryPrize = new ActPrize().setActivityCode(acReceivePriceVO.getActivityCode())
                .setCustomerId(acReceivePriceVO.getCustomerId()).setMobile(acReceivePriceVO.getMobile())
                .setPriceTypes(prizeTypes);

        if (!acReceivePriceVO.isCustomerDimension() || !acReceivePriceVO.isMobileDimension()) {
            if (acReceivePriceVO.isCustomerDimension()) {
                queryPrize.setUserId(null);
                queryPrize.setMobile(null);
            }
            if (acReceivePriceVO.isMobileDimension()) {
                queryPrize.setCustomerId(null);
            }
        }


        List<ActPrize> dbActPrizes = prizeDao.findList(queryPrize);
        boolean continueFlag = acReceivePriceVO.isContinueFlag();

        /**2022-01-17 过期可重复领取或可由推荐人续期奖品类型**/
        List<String> canDelayOrExpireRePickPrizes = AppConfigUtils.getList(ACT_DELAYOREXPIREREPICK_PRIZES, ",");
        /**2022-02-08 不同推荐人可重复领取**/
        List<String> recommendMultiPrizes = AppConfigUtils.getList(ACT_RECOMMENDERMULTI_PRIZES, ",");
        /*2022-04-13 使用后可再次领取使用的奖品类型*/
        List<String> usedMultiPrizes = AppConfigUtils.getList(ACT_USEDMULTI_PRIZES, ",");

        if (dbActPrizes.size() > 0) {
            Date today = new Date();
            Date beginOfDay = DateUtil.beginOfDay(today);
            Date endOfDay = DateUtil.endOfDay(today);
            for (ActPrize dbActPrize : dbActPrizes) {
                String prizeType = dbActPrize.getCode();
                Integer limitTimesType = dbActPrize.getLimitTimesType();
                PriceTimesTypeEnum priceTimesTypeEnum = EnumUtil.likeValueOf(PriceTimesTypeEnum.class, limitTimesType);
                priceTimesTypeEnum = priceTimesTypeEnum == null ? PriceTimesTypeEnum.ONCE : priceTimesTypeEnum;
                switch (priceTimesTypeEnum) {
                    case ONCE:
                        if (!continueFlag) {
                            throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过该奖品:" + prizeType);
                        } else {
                            toDeletePrizeTypes.add(prizeType);
                        }
                        break;
                    case DAILY_ONCE:
                        Optional<ActPrize> optional = dbActPrizes.stream().filter(
                                e -> e.getUpdateTime() != null
                                        && e.getUpdateTime().getTime() >= beginOfDay.getTime()
                                        && e.getUpdateTime().getTime() <= endOfDay.getTime()
                        ).findAny();
                        if (optional.isPresent()) {
                            if (!continueFlag) {
                                throw BizException.COMMON_CUSTOMIZE_ERROR.format("今天已领取过该奖品:" + prizeType);
                            } else {
                                toDeletePrizeTypes.add(prizeType);
                            }
                        }
                        if (Arrays.stream(prizeTypes).filter(e -> StringUtils.equals(e, prizeType)).count() > 1) {
                            if (!continueFlag) {
                                throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取奖品超过次数:" + prizeType);
                            } else {
                                toDeletePrizeTypes.add(prizeType);
                            }
                        }
                        break;
                    case MULTI_TIMES:
                        Integer maxTimesDaily = dbActPrize.getMaxTimesDaily();
                        if (maxTimesDaily != null) {
                            if (Arrays.stream(prizeTypes).filter(e -> StringUtils.equals(e, prizeType)).count()
                                    + dbActPrizes.stream().filter(actPrize1 -> StringUtils.equals(actPrize1.getCode(), prizeType)).count()
                                    > maxTimesDaily) {
                                if (!continueFlag) {
                                    throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取奖品超过次数:" + prizeType);
                                } else {
                                    toDeletePrizeTypes.add(prizeType);
                                }
                            }
                        }
                        break;
                    /**奖品对应优惠券过期可重领，或可由推荐人延期**/
                    case MULTI_ON_CONDITION:
                        ActPrizeOperVO operVO = new ActPrizeOperVO();
                        if (canDelayOrExpireRePickPrizes.contains(prizeType)) {
                            //							operVO = checkDelayOrExpireRePick(null, dbActPrize.getRedeemCode(), acReceivePriceVO.getUserId(), acReceivePriceVO.getCustomerId(), acReceivePriceVO.getRecommendCode());
                            /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                            operVO = checkDelayOrExpireRePick(null, dbActPrize.getRedeemCode(), acReceivePriceVO, acReceivePriceVO.getRecommendCode());

                        }
                        if (recommendMultiPrizes.contains(prizeType)) {
                            StatusObjDto<Map<String, Object>> couponDto = couponBiz.couponEntityInfo(dbActPrize.getRedeemCode());
                            if (couponDto.getCode().equals(StatusDto.SUCCESS)) {
                                Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
                                //								operVO = checkRecommendMulti(coupon.getTemplate(), null, null, acReceivePriceVO.getCustomerId(), acReceivePriceVO.getRecommendCode());
                                /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                                /**2022-11-09 增加推荐人为空场景，赋值为0，兼容多推荐人领取**/
                                operVO = checkRecommendMulti(coupon.getTemplate(), null, acReceivePriceVO, ObjectUtils.isEmptyOrNull(acReceivePriceVO.getRecommendCode()) ? "0" : acReceivePriceVO.getRecommendCode());

                            }
                        }
                        if (usedMultiPrizes.contains(prizeType)) {
                            StatusObjDto<Map<String, Object>> couponDto = couponBiz.couponEntityInfo(dbActPrize.getRedeemCode());
                            if (couponDto.getCode().equals(StatusDto.SUCCESS)) {
                                Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
                                //								operVO = checkRecommendMulti(coupon.getTemplate(), null, null, acReceivePriceVO.getCustomerId(), null);
                                /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                                operVO = checkRecommendMulti(coupon.getTemplate(), null, acReceivePriceVO, null);

                            }
                        }
                        Integer openFlag = operVO.getOperFlag();
                        /**可更新**/
                        if (ActPrizeOperVO.OPER_FLAG_UPD.equals(openFlag)) {
                            updOperVO.add(operVO);
                            toDeletePrizeTypes.add(prizeType);
                        }
                        /**不可操作**/
                        if (ActPrizeOperVO.OPER_FLAG_UNABLE.equals(openFlag)) {
                            if (!continueFlag) {
                                throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过该奖品:" + prizeType);
                            } else {
                                toDeletePrizeTypes.add(prizeType);
                            }
                        }
                        break;
                }
            }
        }
        /**可领取奖品列表**/
        List<String> receivePrizes = Arrays.stream(prizeTypes).filter(e -> !toDeletePrizeTypes.contains(e)).collect(Collectors.toList());
        List<ActPrizeOperVO> voList = Lists.newArrayList();
        for (String string : receivePrizes) {
            voList.add(new ActPrizeOperVO().setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE).setCode(string));
        }
        /**增加可更新列表**/
        voList.addAll(updOperVO);
        return voList;
    }


    /**
     * 检测是否重复领取奖品
     */
    private ActPrizeOperVO checkMultiReceiveAndGetNewPrizeType(AcReceivePriceVO receiveVO) {
        String[] prizeTypes = receiveVO.getPrizeType().split(",");
        List<String> toDeletePrizeTypes = new ArrayList<>(prizeTypes.length);

        /* 可更新奖品   */
        Set<ActPrizeOperVO> updOperVO = Sets.newHashSet();
        //默认可领取
        ActPrizeOperVO actPrizeOperVO = new ActPrizeOperVO().setCode(receiveVO.getPrizeType())
                .setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);

        //查询唯一性的字段控制
        ActPrize queryPrize = new ActPrize().setPriceTypes(prizeTypes)
                .setActivityCode(receiveVO.getActivityCode())
                .setCustomerId(receiveVO.getCustomerId())
                .setMobile(receiveVO.getMobile())
                .setUserId(receiveVO.getUserId())
                .setUniqueType(receiveVO.getUniqueType())
                .setGlobalUniquePrizeType(receiveVO.getGlobalUniquePrizeType());


        List<ActPrize> dbActPrizes = prizeDao.findList(queryPrize);
        boolean continueFlag = receiveVO.isContinueFlag();

        /**2022-01-17 过期可重复领取或可由推荐人续期奖品类型**/
        List<String> canDelayOrExpireRePickPrizes = AppConfigUtils.getList(ACT_DELAYOREXPIREREPICK_PRIZES, ",");
        /**2022-02-08 不同推荐人可重复领取**/
        List<String> recommendMultiPrizes = AppConfigUtils.getList(ACT_RECOMMENDERMULTI_PRIZES, ",");
        /*2022-04-13 使用后可再次领取使用的奖品类型*/
        List<String> usedMultiPrizes = AppConfigUtils.getList(ACT_USEDMULTI_PRIZES, ",");

        if (dbActPrizes.size() > 0) {
            Date today = new Date();
            Date beginOfDay = DateUtil.beginOfDay(today);
            Date endOfDay = DateUtil.endOfDay(today);
            for (ActPrize dbActPrize : dbActPrizes) {
                String prizeType = dbActPrize.getCode();
                String redeemCode = dbActPrize.getRedeemCode();
                Integer limitTimesType = dbActPrize.getLimitTimesType();
                PriceTimesTypeEnum priceTimesTypeEnum = EnumUtil.likeValueOf(PriceTimesTypeEnum.class, limitTimesType);
                priceTimesTypeEnum = priceTimesTypeEnum == null ? PriceTimesTypeEnum.ONCE : priceTimesTypeEnum;
                switch (priceTimesTypeEnum) {
                    case ONCE:
                        if (!continueFlag) {
                            throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过该奖品:" + prizeType);
                        } else {
                            actPrizeOperVO.setOperFlag(ActPrizeOperVO.OPER_HAD_RECEIVE).setLastActivityCode(dbActPrize.getActivityCode())
                                    .setLastCustomerId(dbActPrize.getCustomerId()).setRedeemCode(redeemCode);
                            toDeletePrizeTypes.add(prizeType);
                        }
                        break;
                    case DAILY_ONCE:
                        Optional<ActPrize> optional = dbActPrizes.stream().filter(
                                e -> e.getUpdateTime() != null
                                        && e.getUpdateTime().getTime() >= beginOfDay.getTime()
                                        && e.getUpdateTime().getTime() <= endOfDay.getTime()
                        ).findAny();
                        if (optional.isPresent()) {
                            if (!continueFlag) {
                                throw BizException.COMMON_CUSTOMIZE_ERROR.format("今天已领取过该奖品:" + prizeType);
                            } else {
                                actPrizeOperVO.setOperFlag(ActPrizeOperVO.OPER_HAD_RECEIVE).setLastActivityCode(dbActPrize.getActivityCode())
                                        .setLastCustomerId(dbActPrize.getCustomerId()).setRedeemCode(redeemCode);
                                toDeletePrizeTypes.add(prizeType);
                            }
                        }
                        if (Arrays.stream(prizeTypes).filter(e -> StringUtils.equals(e, prizeType)).count() > 1) {
                            if (!continueFlag) {
                                throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取奖品超过次数:" + prizeType);
                            } else {
                                actPrizeOperVO.setOperFlag(ActPrizeOperVO.OPER_HAD_RECEIVE).setLastActivityCode(dbActPrize.getActivityCode())
                                        .setLastCustomerId(dbActPrize.getCustomerId()).setRedeemCode(redeemCode);
                                toDeletePrizeTypes.add(prizeType);
                            }
                        }
                        break;
                    case MULTI_TIMES:
                        Integer maxTimesDaily = dbActPrize.getMaxTimesDaily();
                        if (maxTimesDaily != null) {
                            if (Arrays.stream(prizeTypes).filter(e -> StringUtils.equals(e, prizeType)).count()
                                    + dbActPrizes.stream().filter(actPrize1 -> StringUtils.equals(actPrize1.getCode(), prizeType)).count()
                                    > maxTimesDaily) {
                                if (!continueFlag) {
                                    throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取奖品超过次数:" + prizeType);
                                } else {
                                    actPrizeOperVO.setOperFlag(ActPrizeOperVO.OPER_HAD_RECEIVE).setLastActivityCode(dbActPrize.getActivityCode())
                                            .setLastCustomerId(dbActPrize.getCustomerId()).setRedeemCode(redeemCode);
                                    toDeletePrizeTypes.add(prizeType);
                                }
                            }
                        }
                        break;
                    /**奖品对应优惠券过期可重领，或可由推荐人延期**/
                    case MULTI_ON_CONDITION:
                        ActPrizeOperVO operVO = new ActPrizeOperVO();
                        if (canDelayOrExpireRePickPrizes.contains(prizeType)) {
                            /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                            operVO = checkDelayOrExpireRePick(null, redeemCode, receiveVO, receiveVO.getRecommendCode());

                        }
                        if (recommendMultiPrizes.contains(prizeType)) {
                            StatusObjDto<Map<String, Object>> couponDto = couponBiz.couponEntityInfo(redeemCode);
                            if (couponDto.getCode().equals(StatusDto.SUCCESS)) {
                                Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
                                /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                                /**2022-11-09 增加推荐人为空场景，赋值为0，兼容多推荐人领取**/
                                operVO = checkRecommendMulti(coupon.getTemplate(), null, receiveVO, ObjectUtils.isEmptyOrNull(receiveVO.getRecommendCode()) ? "0" : receiveVO.getRecommendCode());

                            }
                        }
                        if (usedMultiPrizes.contains(prizeType)) {
                            StatusObjDto<Map<String, Object>> couponDto = couponBiz.couponEntityInfo(redeemCode);
                            if (couponDto.getCode().equals(StatusDto.SUCCESS)) {
                                Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
                                operVO = checkRecommendMulti(coupon.getTemplate(), null, receiveVO, null);

                            }
                        }
                        Integer openFlag = operVO.getOperFlag();
                        /**可更新**/
                        if (ActPrizeOperVO.OPER_FLAG_UPD.equals(openFlag)) {
                            updOperVO.add(operVO);
                            toDeletePrizeTypes.add(prizeType);
                        }
                        /**不可操作**/
                        if (ActPrizeOperVO.OPER_FLAG_UNABLE.equals(openFlag)) {
                            if (!continueFlag) {
                                throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过该奖品:" + prizeType);
                            } else {
                                actPrizeOperVO.setOperFlag(ActPrizeOperVO.OPER_HAD_RECEIVE).setLastActivityCode(dbActPrize.getActivityCode())
                                        .setLastCustomerId(dbActPrize.getCustomerId()).setRedeemCode(redeemCode);
                                toDeletePrizeTypes.add(prizeType);
                            }
                        }
                        break;
                }
            }
        }
        return actPrizeOperVO;
    }

    @Override
    public StatusObjDto<List<CouponReceiveStatusDto>> userReceiveCouponStatus(Activity activity, String[] prizeTypeCodes,
                                                                              AccTokenUser user, Customer customer, Integer dimension,
                                                                              String startDateStr, String endDateStr) {

        List<CouponReceiveStatusDto> couponReceiveStatusDtoList = new ArrayList<>();
        for (String prizeTypeCode : prizeTypeCodes) {
            ActPrizeType prizeType = actPrizeTypeBiz.getPrizeType(prizeTypeCode);
            if (prizeType == null) {
                continue;
            }
            CouponReceiveStatusDto statusDto = new CouponReceiveStatusDto();
            statusDto.setStatus(CouponReceiveStatusDto.STATUS_NOT_RECEIVED);
            statusDto.setPoint(prizeType.getPoint());
            statusDto.setPrizeType(prizeTypeCode);
            statusDto.setPrizeName(prizeType.getName());
            statusDto.setDescription(prizeType.getRemark());

            // 获取总剩余数量
            Integer leftCount = actPrizeBiz.findPrizeCount(activity.getCode(), prizeTypeCode, ActPrize.STATUS_NOT_SEND,
                    null, null, null, null);
            Integer allCount = actPrizeBiz.findPrizeCount(activity.getCode(), prizeTypeCode, null,
                    null, null, null, null);

            statusDto.setLeftCount(leftCount);
            statusDto.setAllCount(allCount);
            if (leftCount <= 0) {
                // 该奖品已领完
                statusDto.setStatus(CouponReceiveStatusDto.STATUS_RECEIVE_COMPLETE);
            }

            // 若设置了每日限制, 则设置每日剩余数量
            if (prizeType.getDailyLimit() != null && prizeType.getDailyLimit() > 0) {
                Integer todayCount = actPrizeBiz.findPrizeCount(activity.getCode(), prizeTypeCode, ActPrize.STATUS_SEND,
                        DateUtil.beginOfDay(new Date()), DateUtil.endOfDay(new Date()), null, null);
                statusDto.setDailyLimit(prizeType.getDailyLimit());
                statusDto.setDailyLeftCount(prizeType.getDailyLimit() - todayCount);
                if (statusDto.getDailyLeftCount() <= 0) {
                    // 该奖品今日已领完
                    statusDto.setStatus(CouponReceiveStatusDto.STATUS_TODAY_RECEIVE_COMPLETE);
                }
            }

            // 若用户未登录, 则只能查看活动的基本信息
            if (user == null && customer == null) {
                couponReceiveStatusDtoList.add(statusDto);
                continue;
            }

            // 获取用户/客户领取到的奖品
            ActPrize queryPrize = new ActPrize()
                    // 如果活动设置了活动组, 则按活动组查询, 否则按活动查询
                    .setActivityCode(ObjectUtils.isEmptyOrNull(activity.getGroupCode()) ? activity.getCode() : null)
                    .setActivityGroupCode(activity.getGroupCode())
                    .setCode(prizeTypeCode);

            // 设置查询维度
            if (dimension != null) {
                // 若明确指定了查询维度, 则按指定维度查询
                if (dimension.equals(0) && user != null) {
                    queryPrize.setUserId(user.getUserId());
                } else if (dimension.equals(1) && customer != null) {
                    queryPrize.setCustomerId(customer.getCustomerId());
                } else if (dimension.equals(1) && customer == null) {
                    queryPrize.setCustomerId("-1");
                }
            } else {
                // 默认优先使用客户维度查询
                if (customer != null) {
                    queryPrize.setCustomerId(customer.getCustomerId());
                } else if (user != null) {
                    queryPrize.setUserId(user.getUserId());
                }
            }

            // 获取用户已领取的奖品
            List<ActPrize> receivedPrizeList = prizeDao.getUserPrizeList(queryPrize);

            // 计算用户对于该奖品的领取状态
            PrizeReceiveStatusEnum receiveStatus = actPrizeTypeBiz.getReceiveStatus(prizeType, receivedPrizeList, activity, true);

            // 券的真实状态: 未领取, 已领取, 已领完, 已使用
            if (receiveStatus == PrizeReceiveStatusEnum.RECEIVED) {
                statusDto.setStatus(CouponReceiveStatusDto.STATUS_RECEIVED);
            }

            Date startDate = ObjectUtils.isEmptyOrNull(startDateStr) ? null : DateUtil.parse(startDateStr, "yyyyMMdd");
            Date endDate = ObjectUtils.isEmptyOrNull(endDateStr) ? null : DateUtil.parse(endDateStr, "yyyyMMdd");

            if (CollectionUtil.isNotEmpty(receivedPrizeList)) {
                for (ActPrize prize : receivedPrizeList) {
                    PrizeDto prizeDto = new PrizeDto();
                    prizeDto.setId(prize.getId());
                    prizeDto.setType(prize.getType());
                    prizeDto.setName(prize.getName());
                    prizeDto.setCode(prize.getRedeemCode());
                    prizeDto.setRecieveTime(prize.getReceiveTime());
                    if (startDate != null && prize.getReceiveTime().before(startDate)) {
                        continue;
                    }
                    if (endDate != null && prize.getReceiveTime().after(endDate)) {
                        continue;
                    }
                    prizeDto.setWorth(prize.getWorth());
                    prizeDto.setTime(prize.getTime());
                    prizeDto.setStatus(prize.getStatus());
                    prizeDto.setRemark(prize.getRemark());
                    statusDto.getPrizeList().add(prizeDto);

                    // 从优惠券系统获取更多券的信息
                    StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
                    PrizeDto prizeDtoTmp = buildPrizeDto(prize, coupon.getObj());
                    prizeDto.setOpenUrl(prizeDtoTmp.getOpenUrl());
                    prizeDto.setValidityDateFrom(prizeDtoTmp.getValidityDateFrom());
                    prizeDto.setValidityDateTo(prizeDtoTmp.getValidityDateTo());
                    // 设置已使用状态
                    if (coupon.getObj().getStatus() != null && coupon.getObj().getStatus().equals(ActPrize.STATUS_USED)) {
                        prizeDto.setStatus(ActPrize.STATUS_USED);
                    }

                    // 设置券的使用状态
                    if (receiveStatus == PrizeReceiveStatusEnum.RECEIVED) {
                        if (ActPrize.STATUS_USED.equals(prizeDto.getStatus()) && receivedPrizeList.size() == 1) {
                            statusDto.setStatus(CouponReceiveStatusDto.STATUS_USED);
                        }
                    }

                    // 此处只是为了配合前端
                    if (statusDto.getPrizeList().size() == 1) {
                        statusDto.setPrize(statusDto.getPrizeList().get(0));
                    }
                }
            }

            couponReceiveStatusDtoList.add(statusDto);
        }
        return new StatusObjDto<>(true, couponReceiveStatusDtoList, StatusDto.SUCCESS, "");
    }


    @Override
    public StatusObjDto<List<CouponRecieveStatusDto>> status(AcReceivePriceVO acReceivePriceVO) {
        /*校验活动有效性*/
        // StatusDto isValidAct = isValid(acReceivePriceVO.getActivityCode());
        // if(!isValidAct.isOk()) {
        // 	throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(isValidAct.getMsg());
        // }
        //当前页面查询的活动编码
        String curQueryActivityCode = acReceivePriceVO.getActivityCode();
        if (ObjectUtils.isEmptyOrNull(curQueryActivityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }

        if (ObjectUtils.isEmptyOrNull(acReceivePriceVO.getPrizeType())) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeTypes");
        }

        String[] prizeTypeArr = acReceivePriceVO.getPrizeType().split(",");
        /**2022-01-17 过期可重复领取或可由推荐人续期奖品类型**/
        List<String> canDelayOrExpireRePickPrizes = AppConfigUtils.getList(ACT_DELAYOREXPIREREPICK_PRIZES, ",");
        /**2022-02-08 不同推荐人可重复领取**/
        List<String> recommendMultiPrizes = AppConfigUtils.getList(ACT_RECOMMENDERMULTI_PRIZES, ",");
        /*2022-04-13 使用后可再次领取使用的奖品类型*/
        List<String> usedMultiPrizes = AppConfigUtils.getList(ACT_USEDMULTI_PRIZES, ",");

        //跨活动只能领取一次的奖品类型
        List<String> globalUniquePrizes = AppConfigUtils.getList(GLOBAL_UNIQUE_PRIZES, ",");


        //领取状态
        List<CouponRecieveStatusDto> status = Lists.newArrayList();
        for (String prizeType : prizeTypeArr) {
            CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();

            /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
            boolean globalUniquePrizeType = globalUniquePrizes.contains(prizeType);
            List<ActPrize> prizes = actPrizeBiz.findPrize(acReceivePriceVO, prizeType, null, null, globalUniquePrizeType);

            Integer recieveStatus = prizes.size() > 0 ? CouponRecieveStatusDto.STATUS_RECIEVED : CouponRecieveStatusDto.STATUS_NOT_RECIEVED;

            ActPrizeOperVO operVo = new ActPrizeOperVO();

            /**2021-01-17 查找优惠券信息**/
            if (!prizes.isEmpty()) {
                String redeemCode = prizes.get(0).getRedeemCode();
                StatusObjDto<Map<String, Object>> couponDto = couponBiz.couponEntityInfo(redeemCode);
                if (!couponDto.isOk()) {
                    log.error("查询优惠券信息异常,{},{}", redeemCode, couponDto.getMsg());
                    throw BizException.COMMON_CUSTOMIZE_ERROR.format(couponDto.getMsg());
                }
                Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
                recieveStatus = CouponInfo.STATUS_USERD.equals(coupon.getStatus()) ? CouponRecieveStatusDto.STATUS_USE : recieveStatus;

                PriceTimesTypeEnum priceTimesTypeEnum = EnumUtil.likeValueOf(PriceTimesTypeEnum.class, prizes.get(0).getLimitTimesType());
                priceTimesTypeEnum = priceTimesTypeEnum == null ? PriceTimesTypeEnum.ONCE : priceTimesTypeEnum;

                /**特殊优惠券：过期可重复领取或者未使用可由推荐人续期**/
                if (priceTimesTypeEnum == PriceTimesTypeEnum.MULTI_ON_CONDITION) {
                    if (canDelayOrExpireRePickPrizes.contains(prizeType)) {
                        //						operVo = checkDelayOrExpireRePick(coupon.getTemplate(), redeemCode, null, acReceivePriceVO.getCustomerId(), acReceivePriceVO.getRecommendCode());
                        /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                        operVo = checkDelayOrExpireRePick(coupon.getTemplate(), redeemCode, acReceivePriceVO, acReceivePriceVO.getRecommendCode());

                    }
                    if (recommendMultiPrizes.contains(prizeType)) {
                        //						operVo = checkRecommendMulti(coupon.getTemplate(), redeemCode, null, acReceivePriceVO.getCustomerId(), acReceivePriceVO.getRecommendCode());
                        /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                        operVo = checkRecommendMulti(coupon.getTemplate(), redeemCode, acReceivePriceVO, ObjectUtils.isEmptyOrNull(acReceivePriceVO.getRecommendCode()) ? "0" : acReceivePriceVO.getRecommendCode());

                    }
                    if (usedMultiPrizes.contains(prizeType)) {
                        //						operVo = checkRecommendMulti(coupon.getTemplate(), redeemCode, null, acReceivePriceVO.getCustomerId(), null);
                        /**2022-09-26 增加手机号查询场景，由acReceivePriceVO控制**/
                        operVo = checkRecommendMulti(coupon.getTemplate(), redeemCode, acReceivePriceVO, null);
                    }
                    /**可领取或可更新，重置领取状态**/
                    if (ActPrizeOperVO.OPER_FLAG_RECEIVE.equals(operVo.getOperFlag()) || ActPrizeOperVO.OPER_FLAG_UPD.equals(operVo.getOperFlag())) {
                        recieveStatus = CouponRecieveStatusDto.STATUS_NOT_RECIEVED;
                    }

                    /*每天可领取一次*/
                } else if (priceTimesTypeEnum == PriceTimesTypeEnum.DAILY_ONCE) {
                    Date today = new Date();
                    Date beginOfDay = DateUtil.beginOfDay(today);
                    Date endOfDay = DateUtil.endOfDay(today);

                    Optional<ActPrize> optional = prizes.stream()
                            .filter(e -> e.getUpdateTime().getTime() >= beginOfDay.getTime() && e.getUpdateTime().getTime() <= endOfDay.getTime())
                            .findAny();
                    if (!optional.isPresent()) {
                        /*今日未领取  设置状态为未领取*/
                        recieveStatus = CouponRecieveStatusDto.STATUS_NOT_RECIEVED;
                    }
                }
            }
            ActPrize aPrize = null;
            //未领取时从缓存中获取一条数据返回
            if (CouponRecieveStatusDto.STATUS_NOT_RECIEVED.equals(recieveStatus)) {
                aPrize = actPrizeBiz.findOneAvailablePrize(curQueryActivityCode, prizeType);
                /**若是可替换，则返回已获取奖品信息**/
                if (operVo != null && ActPrizeOperVO.OPER_FLAG_UPD.equals(operVo.getOperFlag())) {
                    String couponCode = operVo.getRedeemCode();
                    aPrize = prizes.stream().filter(e -> e.getRedeemCode().equals(couponCode)).findAny().orElse(null);
                }
                /**未领取且无可用奖品**/
                recieveStatus = aPrize == null ? CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE : recieveStatus;
            }

            //查出剩余奖品数据
            Integer notSend = actPrizeBiz.findPrizeCount(curQueryActivityCode, prizeType, null, null, null, null, ActPrize.STATUS_NOT_SEND, null, null);
            statusDto.setLeftCount(notSend);
            Integer allCount = actPrizeBiz.findPrizeCount(curQueryActivityCode, prizeType, null, null, null, null, null, null, null);
            statusDto.setAllCount(allCount);
            if (ObjectUtils.isNotEmptyOrNull(acReceivePriceVO.getRecommendCode())) {
                //查询营销人员下剩余券数量
                Map<String, Object> busparams = Maps.newHashMap();
                String couponTemplate = PRIZE_COUPON_TEMPLATES.get(prizeType);
                if (couponTemplate != null) {
                    List<String> salesCouponTemplates = Lists.newArrayList();
                    salesCouponTemplates.add(couponTemplate);
                    busparams.put("templates", salesCouponTemplates);
                    busparams.put("recommendCode", acReceivePriceVO.getRecommendCode());
                    busparams.put("status", 1);
                    ResultDto result = RemoteUtils.call("base.couponcooperation.salesleftcount", ApiServiceTypeEnum.COOPERATION, busparams, true);
                    if (!result.getCode().equals(ResultDto.SUCCESS)) {
                        recieveStatus = CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE;
                    } else {
                        Integer leftCount = (Integer) result.getData().get("count");
                        recieveStatus = leftCount.equals(0) ? CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE : recieveStatus;
                        statusDto.setLeftCount(leftCount);
                    }
                }
            }

            /**2021年春节领取活动例外,特殊设置leftCount**/
            if (ACTIVITY_SPRING2021_COUPON.equals(curQueryActivityCode)) {
                Integer resetLeftCount = resetLetfCount(curQueryActivityCode, prizeType);
                statusDto.setLeftCount(resetLeftCount);
            }

            /**逻辑合并至【2021-01-17 查找优惠券信息】**/
            //			if(prizes.size() > 0) {//覆盖状态为已领取
            //				recieveStatus = CouponRecieveStatusDto.STATUS_RECIEVED;
            //
            //				//region 查询是否已使用
            //				Map<String, Object> busparams = Maps.newHashMap();
            //				busparams.put("code", prizes.get(0).getRedeemCode());
            //				ResultDto result =  RemoteUtils.call("base.couponcooperation.findcouponbycode",ApiServiceTypeEnum.COOPERATION,busparams,true);
            //				if (result.getCode() != 0) {
            //					throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
            //				}
            //				Integer couponStatus = (Integer) result.getData().get("status");
            //				recieveStatus = couponStatus.equals(3)?CouponRecieveStatusDto.STATUS_USE:recieveStatus;
            //				//end region
            //			}

            statusDto.setPrizeType(prizeType);
            statusDto.setStatus(recieveStatus);
            if (ObjectUtils.isNotEmptyOrNull(acReceivePriceVO.getRecommendCode()) && recieveStatus > CouponRecieveStatusDto.STATUS_NOT_RECIEVED) {
                /**推荐人不为空，设置推荐人**/
                StatusObjDto<Staff> staff = staffBiz.infoByMobile(acReceivePriceVO.getRecommendCode());
                if (staff.isOk() && staff.getObj() != null) {
                    statusDto.setRecommendName(staff.getObj().getName());
                }
            }
            ActPrize prizeInfo = new ActPrize();
            if (prizes.size() > 0) {
                /**若已领取，且返回redeemCode，取redeemCode对应奖品信息**/
                if (operVo != null && ObjectUtils.isNotEmptyOrNull(operVo.getRedeemCode())) {
                    String operVoRedeemCode = operVo.getRedeemCode();
                    BeanUtils.copyProperties(prizes.stream().filter(e -> e.getRedeemCode().equals(operVoRedeemCode)).findAny().orElse(prizeInfo), prizeInfo);
                } else {
                    BeanUtils.copyProperties(prizes.get(0), prizeInfo);
                }
            } else {
                prizeInfo = aPrize;
            }

            ActPrizeType actPrizeType = findPrizeType(prizeType);
            PriceTimesTypeEnum priceTimesType = EnumUtil.likeValueOf(PriceTimesTypeEnum.class, actPrizeType.getLimitTimesType());
            priceTimesType = priceTimesType == null ? PriceTimesTypeEnum.ONCE : priceTimesType;
            if (priceTimesType == PriceTimesTypeEnum.DAILY_ONCE && recieveStatus.equals(CouponRecieveStatusDto.STATUS_NOT_RECIEVED)) {
                prizeInfo = aPrize;
            }

            if (prizeInfo != null) {
                Integer type = prizeInfo.getType();
                MyPrizeDto prize = new MyPrizeDto();
                prize.setType(type);
                prize.setName(prizeInfo.getName());
                String code = prizes.size() > 0 ? prizeInfo.getRedeemCode() : null;
                prize.setCode(code);
                prize.setRecieveTime(prizeInfo.getUpdateTime());
                //prize.setCardNo(prizeInfo.getCardNo());
                prize.setWorth(prizeInfo.getWorth());
                prize.setTime(prizeInfo.getTime());
                //prize.setCardPassword(prizeInfo.getCardPassword());
                prize.setPoint(prizeInfo.getPoint());

                //查询卡券中心卡券信息
                StatusObjDto<CouponInfo> couponStatus = couponBiz.couponInfo(null, prizeInfo.getRedeemCode());
                if (!couponStatus.getCode().equals(StatusDto.SUCCESS)) continue;
                CouponInfo coupon = couponStatus.getObj();
                if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS) && ObjectUtils.isNotEmptyOrNull(coupon.getSendTime())) {
                    prize.setValidityDateFrom(coupon.getSendTime());
                    prize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(coupon.getSendTime(), coupon.getValidityDays())));
                } else if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_TIME_RANGE)) {
                    prize.setValidityDateFrom(coupon.getValidityDateFrom());
                    prize.setValidityDateTo(coupon.getValidityDateTo());
                }
                // 重设奖品领取状态
                recieveStatus = CouponInfo.STATUS_USERD.equals(coupon.getStatus()) ? CouponRecieveStatusDto.STATUS_USE
                        : CouponInfo.STATUS_SENDED.equals(coupon.getStatus()) ? CouponRecieveStatusDto.STATUS_RECIEVED : recieveStatus;
                statusDto.setStatus(recieveStatus);
                prize.setDiscount(coupon.getDiscount());
                prize.setDescription(coupon.getDescription());
                prize.setRegulation(coupon.getRegulation());
                prize.setOpenUrl(coupon.getOpenUrl());
                prize.setRecieveTime(coupon.getSendTime());
                prize.setStatus(coupon.getStatus());
                prize.setCouponType(coupon.getType());
                //设置当前是否可用
                Integer isCurrentEnable = CodeConstant.CODE_NO;
                if (prize.getStatus() != null && MyPrizeDto.STATUS_NOT_USED.compareTo(prize.getStatus()) >= 0
                        && prize.getValidityDateFrom() != null && prize.getValidityDateTo() != null) {
                    Date now = new Date();
                    isCurrentEnable = now.getTime() > prize.getValidityDateFrom().getTime() && now.getTime() < prize.getValidityDateTo().getTime() ?
                            CodeConstant.CODE_YES : CodeConstant.CODE_NO;
                }
                prize.setIsCurrentEnable(isCurrentEnable);
                statusDto.setPrize(prize);
            } else {
                /**未获得奖品，且已无可用奖品，设置奖品信息**/
                ActPrize ePrize = new ActPrize();
                ePrize.setActivityCode(curQueryActivityCode);
                ePrize.setCode(prizeType);
                List<ActPrize> ePrizeList = prizeDao.findList(ePrize);
                prizeInfo = ePrizeList.stream().findAny().orElse(null);
                MyPrizeDto prize = new MyPrizeDto();
                prize.setType(prizeInfo.getType());
                prize.setName(prizeInfo.getName());
                prize.setWorth(prizeInfo.getWorth());
                prize.setTime(prizeInfo.getTime());
                prize.setIsCurrentEnable(CodeConstant.CODE_NO);
                prize.setPoint(prizeInfo.getPoint());
                statusDto.setPrize(prize);
            }

            /*奖品每日限量配置*/
            if (actPrizeType.getDailyLimit() == null) {
                statusDto.setDailyLimit(notSend);
                statusDto.setDailyLeftCount(notSend);

            } else {
                statusDto.setDailyLimit(actPrizeType.getDailyLimit());

                String redisKey = curQueryActivityCode + ":" + prizeType;
                /*今日剩余数量*/
                Long dailyLeftCount = ActivityRedis.ACT_ACTVITY_PRIZE.scard(redisKey);

                statusDto.setDailyLeftCount(Math.toIntExact(dailyLeftCount));
            }

            status.add(statusDto);
        }
        return new StatusObjDto<List<CouponRecieveStatusDto>>(true, status, StatusDto.SUCCESS, "");
    }

    @Override
    public StatusObjDto<List<CouponRecieveStatusDto>> getNewCustomerCouponStatus(AcReceivePriceVO acReceivePriceVO) {
        //当前页面查询的活动编码
        String curQueryActivityCode = acReceivePriceVO.getActivityCode();
        // 奖品编码
        String prizeType = acReceivePriceVO.getPrizeType();
        if (ObjectUtils.isEmptyOrNull(curQueryActivityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(acReceivePriceVO.getPrizeType())) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeTypes");
        }
        // 活动编码和奖品编码不匹配，直接返回
        if (!ACT_NEWCUST_2O2O.equals(curQueryActivityCode) || !ACT_NEWCUST_2O2O_PRIZE.equals(prizeType)) {
            return new StatusObjDto<List<CouponRecieveStatusDto>>(true, Lists.newArrayList(), StatusDto.SUCCESS, "");
        }

        //领取状态
        List<CouponRecieveStatusDto> status = Lists.newArrayList();
        CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
        //查找已领取奖品
        List<ActPrize> prizes = actPrizeBiz.findPrize(acReceivePriceVO, prizeType, null, null, null);

        Integer recieveStatus = prizes.size() > 0 ? CouponRecieveStatusDto.STATUS_RECIEVED : CouponRecieveStatusDto.STATUS_NOT_RECIEVED;

        /**2021-01-17 查找优惠券信息**/
        if (!prizes.isEmpty()) {
            String redeemCode = prizes.get(0).getRedeemCode();
            StatusObjDto<Map<String, Object>> couponDto = couponBiz.couponEntityInfo(redeemCode);
            if (!couponDto.isOk()) {
                log.error("查询优惠券信息异常,{},{}", redeemCode, couponDto.getMsg());
                throw BizException.COMMON_CUSTOMIZE_ERROR.format(couponDto.getMsg());
            }
            Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
            MyPrizeDto prizeDto = new MyPrizeDto();
            String openUrl = coupon.getOpenUrl();
            prizeDto.setOpenUrl(openUrl);
            statusDto.setPrize(prizeDto);
            statusDto.setOperFlag(ActPrizeOperVO.OPER_HAD_RECEIVE);
            recieveStatus = CouponInfo.STATUS_USERD.equals(coupon.getStatus()) ? CouponRecieveStatusDto.STATUS_USE : recieveStatus;

        }
        ActPrize aPrize = null;
        //未领取时从缓存中获取一条数据返回
        if (CouponRecieveStatusDto.STATUS_NOT_RECIEVED.equals(recieveStatus)) {
            aPrize = actPrizeBiz.findOneAvailablePrize(curQueryActivityCode, prizeType);
            /**未领取且无可用奖品**/
            recieveStatus = aPrize == null ? CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE : recieveStatus;
            if (aPrize == null) {
                statusDto.setOperFlag(ActPrizeOperVO.OPER_FLAG_UNABLE);
            } else {
                statusDto.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
            }
        }

        //查出剩余奖品数据
        Integer notSend = actPrizeBiz.findPrizeCount(curQueryActivityCode, prizeType, null, null, null, null, ActPrize.STATUS_NOT_SEND, null, null);
        statusDto.setLeftCount(notSend);
        Integer allCount = actPrizeBiz.findPrizeCount(curQueryActivityCode, prizeType, null, null, null, null, null, null, null);
        statusDto.setAllCount(allCount);
        statusDto.setPrizeType(prizeType);
        statusDto.setStatus(recieveStatus);
        status.add(statusDto);
        return new StatusObjDto<List<CouponRecieveStatusDto>>(true, status, StatusDto.SUCCESS, "");
    }

    private ActPrizeOperVO checkRecommendMulti(String template, String redeemCode, String userId, String customerId,
                                               String recommendCode) {
        StatusObjDto<Map<String, Object>> couponDto = couponBiz.getLatelyCoupon(template, redeemCode, null, customerId, recommendCode, Coupon.STATUS_SENDED);
        ActPrizeOperVO vo = new ActPrizeOperVO();
        vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_UNABLE);
        if (!couponDto.isOk()) {
            log.error("查询优惠券异常,params,{}|{}|{}|{},msg,{}", template, redeemCode, userId, customerId, couponDto.getMsg());
            return vo;
        }
        /**查无优惠券，可领取**/
        if (couponDto.getObj() == null) {
            vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
        } else {
            Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
            /**场景1：优惠券已过期，可重新领取**/
            if (CouponInfo.STATUS_EXPIRED.equals(coupon.getStatus())) {
                vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
            }
            /**场景2：优惠券未过期**/
            if (CouponInfo.STATUS_SENDED.equals(coupon.getStatus())) {
                vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
                vo.setRedeemCode(coupon.getCode());
            }
        }
        return vo;
    }

    private ActPrizeOperVO checkRecommendMulti(String template, String redeemCode, AcReceivePriceVO acReceivePriceVO,
                                               String recommendCode) {
        String userId = acReceivePriceVO.getUserId();
        String customerId = acReceivePriceVO.getCustomerId();
        if (acReceivePriceVO.isCustomerDimension()) {
            userId = null;
        }
        if (acReceivePriceVO.isMobileDimension()) {
            customerId = null;
        }
        StatusObjDto<Map<String, Object>> couponDto = couponBiz.getLatelyCoupon(template, redeemCode, userId, customerId, recommendCode, Coupon.STATUS_SENDED);
        ActPrizeOperVO vo = new ActPrizeOperVO();
        vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_UNABLE);
        if (!couponDto.isOk()) {
            log.error("查询优惠券异常,params,{}|{}|{}|{},msg,{}", template, redeemCode, userId, customerId, couponDto.getMsg());
            return vo;
        }
        /**查无优惠券，可领取**/
        if (couponDto.getObj() == null) {
            vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
        } else {
            Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
            /**场景1：优惠券已过期，可重新领取**/
            if (CouponInfo.STATUS_EXPIRED.equals(coupon.getStatus())) {
                vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
            }
            /**场景2：优惠券未过期**/
            if (CouponInfo.STATUS_SENDED.equals(coupon.getStatus())) {
                vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_UNABLE);
                vo.setRedeemCode(coupon.getCode());
            }
        }
        log.info("checkRecommendMulti-status：{},", vo.getOperFlag());
        return vo;
    }

    /**
     * @param coupon
     * @param template
     * @param redeemCode
     * @param userId
     * @param customerId
     * @param recommendCode
     * @return opeaFlag:0-不可领取，1-可领取，2-可更新
     */
    private ActPrizeOperVO checkDelayOrExpireRePick(String template, String redeemCode, AcReceivePriceVO acReceivePriceVO, String recommendCode) {
        String userId = acReceivePriceVO.getUserId();
        String customerId = acReceivePriceVO.getCustomerId();
        if (acReceivePriceVO.isCustomerDimension()) {
            userId = null;
        }
        if (acReceivePriceVO.isMobileDimension()) {
            customerId = null;
        }
        StatusObjDto<Map<String, Object>> couponDto = couponBiz.getLatelyCoupon(template, redeemCode, userId, customerId, null, null);
        ActPrizeOperVO vo = new ActPrizeOperVO();
        vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_UNABLE);
        if (!couponDto.isOk()) {
            log.error("查询优惠券异常,params,{}|{}|{}|{},msg,{}", template, redeemCode, userId, customerId, couponDto.getMsg());
            return vo;
        }
        /**查无优惠券，可领取**/
        if (couponDto.getObj() == null) {
            vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
        } else {

            Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);
            /**场景1：优惠券已过期，可重新领取**/
            if (CouponInfo.STATUS_EXPIRED.equals(coupon.getStatus())) {
                vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_RECEIVE);
            }
            /**场景2：优惠券未过期，且优惠券无推荐人信息**/
            if (CouponInfo.STATUS_SENDED.equals(coupon.getStatus()) && ObjectUtils.isEmptyOrNull(coupon.getRecommendCode()) && ObjectUtils.isNotEmptyOrNull(recommendCode)) {
                vo.setOperFlag(ActPrizeOperVO.OPER_FLAG_UPD);
                vo.setRedeemCode(coupon.getCode());
            }
        }
        log.info("checkDelayOrExpireRePick-status：{},", vo.getOperFlag());
        return vo;
    }

    public static MyPrizeDto buildMyPrizeDto(ActPrize prizeInfo, CouponInfo coupon) {
        MyPrizeDto prize = new MyPrizeDto();
        if (prizeInfo != null) {
            prize.setId(prizeInfo.getId());
            prize.setType(prizeInfo.getType());
            prize.setName(prizeInfo.getName());
            prize.setCode(prizeInfo.getRedeemCode());
            prize.setRecieveTime(prizeInfo.getUpdateTime());
            //prize.setCardNo(prizeInfo.getCardNo());
            prize.setWorth(prizeInfo.getWorth());
            prize.setTime(prizeInfo.getTime());
            prize.setRedEnvelopeAmt(prizeInfo.getRedEnvelopeAmt());
        }
        if (coupon != null) {
            if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS) && ObjectUtils.isNotEmptyOrNull(coupon.getSendTime())) {
                prize.setValidityDateFrom(coupon.getSendTime());
                prize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(coupon.getSendTime(), coupon.getValidityDays())));
            } else if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_TIME_RANGE)) {
                prize.setValidityDateFrom(coupon.getValidityDateFrom());
                prize.setValidityDateTo(coupon.getValidityDateTo());
            }
            prize.setDiscount(coupon.getDiscount());
            prize.setDescription(coupon.getDescription());
            prize.setRegulation(coupon.getRegulation());
            prize.setOpenUrl(coupon.getOpenUrl());
            prize.setRecieveTime(coupon.getSendTime());
            prize.setStatus(coupon.getStatus());
            prize.setCouponType(coupon.getType());
            prize.setAmountSatisfy(coupon.getAmountSatisfy());
            prize.setAmountReducation(coupon.getAmountReducation());
            //设置当前是否可用
            Integer isCurrentEnable = CodeConstant.CODE_NO;
            if (prize.getStatus() != null && MyPrizeDto.STATUS_NOT_USED.compareTo(prize.getStatus()) >= 0 && prize.getValidityDateFrom() != null && prize.getValidityDateTo() != null) {
                Date now = new Date();
                isCurrentEnable = now.getTime() > prize.getValidityDateFrom().getTime() && now.getTime() < prize.getValidityDateTo().getTime() ?
                        CodeConstant.CODE_YES : CodeConstant.CODE_NO;
            }
            prize.setIsCurrentEnable(isCurrentEnable);
        }
        return prize;
    }


    private PrizeDto buildPrizeDto(ActPrize prizeInfo, CouponInfo coupon) {
        PrizeDto prize = new PrizeDto();
        if (prizeInfo != null) {
            prize.setId(prizeInfo.getId());
            prize.setType(prizeInfo.getType());
            prize.setName(prizeInfo.getName());
            prize.setCode(prizeInfo.getRedeemCode());
            prize.setRecieveTime(prizeInfo.getUpdateTime());
            prize.setWorth(prizeInfo.getWorth());
            prize.setTime(prizeInfo.getTime());
        }
        if (coupon != null) {
            if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS) && ObjectUtils.isNotEmptyOrNull(coupon.getSendTime())) {
                prize.setValidityDateFrom(coupon.getSendTime());
                prize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(coupon.getSendTime(), coupon.getValidityDays())));
            } else if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_TIME_RANGE)) {
                prize.setValidityDateFrom(coupon.getValidityDateFrom());
                prize.setValidityDateTo(coupon.getValidityDateTo());
            }
            prize.setDiscount(coupon.getDiscount());
            prize.setDescription(coupon.getDescription());
            prize.setRegulation(coupon.getRegulation());
            prize.setOpenUrl(coupon.getOpenUrl());
            prize.setRecieveTime(coupon.getSendTime());
            prize.setStatus(coupon.getStatus());
            prize.setCouponType(coupon.getType());
            //设置当前是否可用
            Integer isCurrentEnable = CodeConstant.CODE_NO;
            if (prize.getStatus() != null && MyPrizeDto.STATUS_NOT_USED.compareTo(prize.getStatus()) >= 0 && prize.getValidityDateFrom() != null && prize.getValidityDateTo() != null) {
                Date now = new Date();
                isCurrentEnable = now.getTime() > prize.getValidityDateFrom().getTime() && now.getTime() < prize.getValidityDateTo().getTime() ?
                        CodeConstant.CODE_YES : CodeConstant.CODE_NO;
            }
            prize.setIsCurrentEnable(isCurrentEnable);
        }
        return prize;
    }

    @Override
    @Transactional
    public StatusObjDto<Integer> sectionReceive(String activityCode, String prizeType, String userId, String openId, String customerId) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("acitivityCode");
        }
        if (ObjectUtils.isEmptyOrNull(prizeType)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }
        StatusDto isValidAct = isValid(activityCode);
        if (!isValidAct.isOk()) {
            return new StatusObjDto<Integer>(false, isValidAct.getCode(), isValidAct.getMsg());
        }
        ActActivityStepEnum stepEnum = ActActivityStepEnum.getStepEnumByCode(activityCode);
        StatusObjDto<Integer> isValidTime = isValidTime(stepEnum);
        if (!isValidTime.isOk()) {
            return new StatusObjDto<Integer>(false, isValidTime.getCode(), isValidTime.getMsg());
        }
        Date dayStart = DateUtils.getDayStart(new Date());
        Date updateTimeFrom = DateUtils.addHour(dayStart, stepEnum.getOPEN_HOUR()[isValidTime.getObj()]);
        Date updateTimeTo = DateUtils.addHour(dayStart, stepEnum.getCLOSE_HOUR()[isValidTime.getObj()]);
        List<ActPrize> outPrizes = actPrizeBiz.findPrize(activityCode, prizeType, customerId, userId, openId, null, ActPrize.STATUS_SEND, updateTimeFrom, updateTimeTo);
        if (stepEnum.getPrizeMaxCount().compareTo(outPrizes.size()) <= 0) {
            throw ActivityBizException.ACT_EMPTY_PRIZE;
        }
        //判断有没有获取过奖品
        List<ActPrize> prizes = actPrizeBiz.findPrize(activityCode, customerId, null, null, prizeType, null, null);
        if (prizes.size() > 0) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过该奖品");
        }
        ActPrize prize = actPrizeBiz.giveOutPrize(activityCode, "", null, prizeType, userId, openId, customerId, null);
        if (prize == null) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
        }
        return new StatusObjDto<Integer>(true, 1, StatusDto.SUCCESS, "");
    }

    private Integer resetLetfCount(String activity, String prizeType) {
        Integer resetLeftCount = 0;
        Date updateTimeFrom = DateUtils.getDayStart(new Date());
        Date updateTimeTo = DateUtils.getDayEnd(new Date());
        List<ActPrize> sendLists = actPrizeBiz.findPrize(activity, prizeType, null, null, null, null, ActPrize.STATUS_SEND, updateTimeFrom, updateTimeTo);
        Integer notSend = actPrizeBiz.findPrizeCount(activity, prizeType, null, null, null, null, ActPrize.STATUS_NOT_SEND, null, null);
        Integer dailyLimit = COUPON_DAILY_LIMIT.get(prizeType);
        resetLeftCount = dailyLimit == null ? notSend :
                dailyLimit - sendLists.size() > notSend ? notSend : dailyLimit - sendLists.size() > 0 ? dailyLimit - sendLists.size() : 0;
        return resetLeftCount;
    }


    //DOUBLE11_2019
    private StatusObjDto<CouponRecieveStatusDto> oldActivityReceive(String prizeTypes, String userId, String openId, String customerId, String recommendCode, String remark, String mobile) {
        //领取带有推荐人的优惠券
        if (ObjectUtils.isEmptyOrNull(recommendCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("recommendCode");
        }

        //		ActPrize prize = actPrizeBiz.findOneAvailablePrize(activityCode, prizeType);
        //		if (prize == null) {
        //			throw ActivityBizException.ACT_EMPTY_PRIZE;
        //		}
        //领取该模板下推荐人的优惠券
        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("customerId", customerId);
        busparams.put("userId", userId);
        busparams.put("template", PRIZE_COUPON_TEMPLATES.get(prizeTypes));
        busparams.put("recommendCode", recommendCode);
        busparams.put("mobile", mobile);
        ResultDto result = RemoteUtils.call("base.couponcooperation.receiveavailablecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
        //调用失败
        if (result.getCode() != 0) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
        }
        //若为兑换码，更新兑换码发出时间
        //		if(prize.getType().equals(ActPrize.TYPE_REDEEM)) {
        //			redeemBiz.out(prize.getRedeemCode());
        //		}
        String couponCode = (String) result.getData().get("code");
        ActPrize prize = actPrizeBiz.findPrizeByRedeem(couponCode);
        if (prize == null) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("奖品不存在");
        }
        prize.setStatus(ActPrize.STATUS_SEND);
        prize.setUpdateTime(new Date());
        actPrizeBiz.updatePrize(userId, openId, customerId, prize.getId(), ActPrize.STATUS_SEND, null, remark);


        CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
        StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
        MyPrizeDto myPrizeDto = buildMyPrizeDto(prize, coupon.getObj());
        statusDto.setPrize(myPrizeDto);
        statusDto.setPrizeType(prizeTypes);
        statusDto.setStatus(ActPrize.STATUS_SEND);
        return new StatusObjDto<CouponRecieveStatusDto>(true, statusDto, StatusDto.SUCCESS, "");
    }

    /**
     * 发券到客户号
     */
    @Override
    public StatusDto receiveToCustomer(String customerId, String activityCode, String prizeType) {
        ActPrize queryPrize = new ActPrize()
                /*7天券 用客户号*/
                .setCustomerId(customerId)
                /*活动编码*/
                .setActivityCode(activityCode)
                /*奖品*/
                .setPriceTypes(new String[]{prizeType});

        List<ActPrize> dbActPrizes = prizeDao.findList(queryPrize);
        if (dbActPrizes.size() > 0) {
            /*如果已经领过奖品*/
            return new StatusDto(true);
        }

        ActPrize prize = actPrizeBiz.getOneAvailablePrize(activityCode, prizeType);
        if (prize == null) {
            throw ActivityBizException.ACT_PRIZE_NOT_VALID;
        }

        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("customerId", customerId);
        busparams.put("code", prize.getRedeemCode());
        /*调基础平台发券*/
        ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
        if (result.getCode() != 0) {
            log.error("receivecoupon error: " + result.getMsg() + "| prizeId:" + prize.getId());
            throw new ActivityBizException(result.getCode(), result.getMsg());
        }

        /*更新活动平台卡券状态*/
        actPrizeBiz.updatePrize(null, null, customerId, prize.getId(), ActPrize.STATUS_SEND, null, null);

        return new StatusDto(true);
    }
}
