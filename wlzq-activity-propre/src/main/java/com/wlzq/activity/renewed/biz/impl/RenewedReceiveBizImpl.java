package com.wlzq.activity.renewed.biz.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.renewed.biz.RenewedReceiveBiz;
import com.wlzq.activity.renewed.dto.RenewedOrdersDto;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @Author: 乔峰
 * @Description:
 */
@Slf4j
@Service
public class RenewedReceiveBizImpl implements RenewedReceiveBiz {
    private static final String GOODS_CODE = "ZTZS";
    private static final String ACTIVITY_CODE = "ACTIVITY.ZTZS.RENEWED.RECEIVE";
    private static final String COUPON_INVEST_FREE7 = "PRIZE.COUPON.INVEST.FREE.14.RECEIVE";
    private static final String COUPON_L2_FREE30 = "PRIZE.VAS.ZTZS.L2.FREE.30";
    private static final String SMS_TEMPLATE = "SMS.ACTIVITY_ZTZS_RENEWED";
    private static final String ACTIVITY_RENEWED_RECEIVE_MOBILES = "activity.renewed.receive.mobiles";

    @Autowired
    private ActPrizeDao prizeDao;
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;

    @Override
    public StatusDto batchReceive() {

        // 调vas查连续包月续费成功的订单
        List<RenewedOrdersDto> orders = getRenewedOrders();

        /*发券失败的记录*/
        Map<String, List<String>> failRecords = Maps.newHashMap();
        failRecords.put(COUPON_INVEST_FREE7, Lists.newArrayList());
        failRecords.put(COUPON_L2_FREE30, Lists.newArrayList());

        // 批量发券
        for (RenewedOrdersDto order : orders) {
            /*7天券发至购买工具的客户号*/
            if (!receiveInvestFree7ToCustomerId(order)) {
                failRecords.get(COUPON_INVEST_FREE7).add(order.getCustomerId());
            }
            /*L2发至购买工具的手机号*/
            if (!receiveL2Free30ToMobile(order)) {
                failRecords.get(COUPON_L2_FREE30).add(order.getMobile());
            }
        }

        // 如果发券失败，发短信给运营或开发人员
        if (!failRecords.get(COUPON_INVEST_FREE7).isEmpty() || !failRecords.get(COUPON_L2_FREE30).isEmpty()) {
            sendShortMessage(failRecords);
        }

        return new StatusDto(true);
    }

    /**
     * 发送短信，短信内容为：发券失败的记录
     */
    private void sendShortMessage(Map<String, List<String>> failRecords) {
        log.error("===> 发券失败的记录: {}", failRecords);

        Map<String, Object> busparams = Maps.newHashMap();
        /*2022-05-05 接收发券失败记录的手机号码**/
        String mobiles = AppConfigUtils.get(ACTIVITY_RENEWED_RECEIVE_MOBILES);
        busparams.put("mobile", mobiles);
        busparams.put("templateCode", SMS_TEMPLATE);

        Map<String, String> smsContent = Maps.newHashMap();
        StringBuilder content = new StringBuilder();
        if (!failRecords.get(COUPON_INVEST_FREE7).isEmpty()) {
            content.append(COUPON_INVEST_FREE7).append("发券失败的客户号为：")
                    .append(StringUtils.collectionToDelimitedString(failRecords.get(COUPON_INVEST_FREE7), ","))
                    .append(";");
        }
        if (!failRecords.get(COUPON_L2_FREE30).isEmpty()) {
            content.append(COUPON_L2_FREE30).append("发券失败的手机号为：")
                    .append(StringUtils.collectionToDelimitedString(failRecords.get(COUPON_L2_FREE30), ","))
                    .append(";");
        }
        smsContent.put("content", content.toString());
        busparams.put("jsonParam", JSON.toJSONString(smsContent));

        ResultDto result = RemoteUtils.call("push.pushcooperation.sendshortmessage", ApiServiceTypeEnum.COOPERATION, busparams, true);

        if (!result.getCode().equals(ResultDto.SUCCESS)) {
            throw new ActivityBizException(result.getCode(), result.getMsg());
        }
    }

    /**
     * 7天券发至购买工具的客户号
     */
    private boolean receiveInvestFree7ToCustomerId(RenewedOrdersDto order) {
        ActPrize queryPrize = new ActPrize()
                /*7天券 用客户号*/
                .setCustomerId(order.getCustomerId())
                /*活动编码*/
                .setActivityCode(ACTIVITY_CODE)
                /*奖品*/
                .setPriceTypes(new String[]{COUPON_INVEST_FREE7});

        List<ActPrize> dbActPrizes = prizeDao.findList(queryPrize);
        if (dbActPrizes.size() > 0) {
            /*如果已经领过奖品*/
            return true;
        }

        ActPrize prize = actPrizeBiz.getOneAvailablePrize(ACTIVITY_CODE, COUPON_INVEST_FREE7);
        if (prize == null) {
            return false;
        }

        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("customerId", order.getCustomerId());
        busparams.put("code", prize.getRedeemCode());
        /*调基础平台发券*/
        ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
        if (result.getCode() != 0) {
            log.error("receivecoupon error: " + result.getMsg() + "| prizeId:" + prize.getId());
            return false;
        }

        /*更新活动平台卡券状态*/
        actPrizeBiz.updatePrize(null, null, order.getCustomerId(), prize.getId(), ActPrize.STATUS_SEND, order.getMobile(), null);

        return true;
    }

    /**
     * L2发至购买工具的手机号
     */
    private boolean receiveL2Free30ToMobile(RenewedOrdersDto order) {
        ActPrize queryPrize = new ActPrize()
                /*l2 用手机号*/
                .setUserId(order.getUserId())
                /*活动编码*/
                .setActivityCode(ACTIVITY_CODE)
                /*奖品*/
                .setPriceTypes(new String[]{COUPON_L2_FREE30});

        List<ActPrize> dbActPrizes = prizeDao.findList(queryPrize);
        if (dbActPrizes.size() > 0) {
            /*如果已经领过奖品*/
            return true;
        }

        ActPrize prize = actPrizeBiz.getOneAvailablePrize(ACTIVITY_CODE, COUPON_L2_FREE30);
        if (prize == null) {
            return false;
        }

        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("userId", order.getUserId());
        busparams.put("code", prize.getRedeemCode());
        if (!StringUtils.isEmpty(order.getCustomerId())) {
            busparams.put("customerId", order.getCustomerId());
        }
        /*调基础平台发券*/
        ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
        if (result.getCode() != 0) {
            log.error("receivecoupon error: " + result.getMsg() + "| prizeId:" + prize.getId());
            return false;
        }

        /*更新活动平台卡券状态*/
        actPrizeBiz.updatePrize(order.getUserId(), null, order.getCustomerId(), prize.getId(), ActPrize.STATUS_SEND, order.getMobile(), null);

        return true;
    }

    /**
     * 查连续包月续费成功的订单
     */
    private List<RenewedOrdersDto> getRenewedOrders() {
        Map<String, Object> busparams = Maps.newHashMap();

        /*今天*/
        LocalDate today = LocalDate.now();
        /*00:00:00*/
        long payTimeStart = LocalDateTime.of(today, LocalTime.MIN).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        /*23:59:59 */
        long payTimeEnd = LocalDateTime.of(today, LocalTime.MAX).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        Activity act = activityBaseBiz.findActivity(ACTIVITY_CODE);
        StatusDto valid = activityBaseBiz.isValid(act);
        if(!valid.isOk()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(valid.getMsg());
        }

        /*产品编码*/
        busparams.put("goodsCode", GOODS_CODE);
        /*协议订单支付时间*/
        busparams.put("payTimeStart", payTimeStart);
        /*协议订单支付时间*/
        busparams.put("payTimeEnd", payTimeEnd);
        /*活动开始时间*/
        busparams.put("activityDateFrom", act.getDateFrom().getTime());
        /*活动结束时间*/
        busparams.put("activityDateTo", act.getDateTo().getTime());

        ResultDto result = RemoteUtils.call("vas.decisioncooperation.renewedorders", ApiServiceTypeEnum.COOPERATION, busparams, true);

        if (!result.getCode().equals(ResultDto.SUCCESS)) {
            throw new ActivityBizException(result.getCode(), result.getMsg());
        }

        List<Map> data = (List<Map>) result.getData().get("info");

        return BeanUtils.toBeanList(data, RenewedOrdersDto.class);
    }
}
