package com.wlzq.activity.etf.biz.impl;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.etf.biz.EtfBiz;
import com.wlzq.activity.etf.dto.PaidOrdersDto;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class EtfBizImpl implements EtfBiz {
    private static final String PRODUCT_CODE = "KCCP005";
    /*ETF课程及投顾产品联合推广活动*/
    public static final String ACTIVITY_CODE = "ACTIVITY.ETF.INVEST.RECEIVE";

    @Autowired
    private CouponCommonReceiveBiz couponRecieveBiz;

    @Override
    public StatusDto batchReceive(String activityCode, String prizeType) {
        // 调service查订单
        List<PaidOrdersDto> paidOrders = getPaidOrders(null);

        // 发券到客户号
        paidOrders.forEach(order -> {
            StatusDto result = couponRecieveBiz.receiveToCustomer(order.getCustomerId(), activityCode, prizeType);
        });

        return new StatusDto(true);
    }

    private static List<PaidOrdersDto> getPaidOrders(String customerId) {
        Map<String, Object> busparams = Maps.newHashMap();

        /*今天*/
        LocalDate today = LocalDate.now();
        /*00:00:00*/
        long payTimeStart = LocalDateTime.of(today, LocalTime.MIN).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        /*23:59:59 */
        long payTimeEnd = LocalDateTime.of(today, LocalTime.MAX).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        busparams.put("customerId", customerId);
        /*产品编码*/
        busparams.put("productCode", PRODUCT_CODE);
        /*订单支付时间(起始时间)*/
        busparams.put("payTimeStart", payTimeStart);
        /*订单支付时间(结束时间)*/
        busparams.put("payTimeEnd", payTimeEnd);

        ResultDto result = RemoteUtils.call("service.coursecooperation.paidorders",
                ApiServiceTypeEnum.COOPERATION, busparams, true);

        if (!result.getCode().equals(ResultDto.SUCCESS)) {
            throw new ActivityBizException(result.getCode(), result.getMsg());
        }

        List<Map> data = (List<Map>) result.getData().get("info");

        return BeanUtils.toBeanList(data, PaidOrdersDto.class);
    }

    public static boolean isPay(String customerId) {
        return getPaidOrders(customerId).size() > 0;
    }
}
