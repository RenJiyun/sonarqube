package com.wlzq.activity.base.biz.impl;

import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.activity.base.biz.OrderCheckBiz;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author renjiyun
 */
@Service
public class OrderCheckBizImpl implements OrderCheckBiz {
    @Autowired
    private ActPrizeTypeBiz actPrizeTypeBiz;
    @Override
    public StatusObjDto<ActPrizeType> checkVasOrder(AccTokenUser user, Activity activity, String outTradeNo) {
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("mobile", user.getMobile())
                .put("outTradeNo", outTradeNo)
                .put("couponCode", "")
                .build();

        ResultDto orderDto = RemoteUtils.call("vas.decisioncooperation.orderdetail",
                ApiServiceTypeEnum.COOPERATION, params, true);

        if (orderDto == null || ResultDto.FAIL_COMMON.equals(orderDto.getCode()) || null == orderDto.getData()) {
            return new StatusObjDto<>(true, null, StatusDto.FAIL_COMMON, "");
        }

        Map<String, Object> orderMap = orderDto.getData();
        Integer status = (Integer) orderMap.get("status");
        Date createTime = new Date(Long.parseLong(String.valueOf(orderMap.get("createTime"))));
        String userId = (String) orderMap.get("userId");

        String goodsCode = (String) orderMap.get("goodsCode");
        Integer specification = (Integer) orderMap.get("specification");
        Integer timeType = (Integer) orderMap.get("timeType");

        if (!"ALv2".equals(goodsCode)) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "活动产品不匹配");
        }

        // 校验用户
        if (!user.getUserId().equals(userId)) {
            return new StatusObjDto<>(true, null, StatusDto.FAIL_COMMON, "用户不匹配");
        }

        // 校验实付金额
        Integer totalFee = (Integer) orderMap.get("totalFee");
        if (ObjectUtils.isEmptyOrNull(totalFee) || totalFee == 0) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "实际支付金额为0");
        }

        // 校验订单是否已经支付
        if (!CodeConstant.CODE_YES.equals(status)) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "订单未支付");
        }

        // 校验订单的创建时间
        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "下单时间不在活动时间范围内");
        }

        ActPrizeType prizeType = getPrizeTypeForDouble11(specification, timeType);
        if (prizeType == null) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "活动产品不匹配");
        }
        return new StatusObjDto<>(true, prizeType, StatusDto.SUCCESS, "");
    }

    private ActPrizeType getPrizeTypeForDouble11(Integer specification, Integer timeType) {
        if (timeType.equals(1) && specification.equals(3)) {
            // 买A股L2三个月, 送金股扫描仪1个月免单券
            return actPrizeTypeBiz.getPrizeType("PRIZE.COUPON.XTJJDS1.2023DOUBLE11");
        } else if (timeType.equals(1) && specification.equals(6)) {
            // 买A股L2六个月, 送金股扫描仪3个月免单券
            return actPrizeTypeBiz.getPrizeType("PRIZE.COUPON.XTJJDS3.2023DOUBLE11");
        }
        return null;
    }

    @Override
    public StatusObjDto<ActPrizeType> checkServiceOrder(AccTokenUser user, Customer customer, Activity activity, String outTradeNo) {
        if (customer == null) {
            return new StatusObjDto<>(true, null, StatusDto.FAIL_COMMON, "客户号未登录");
        }
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("customerId", customer.getCustomerId())
                .put("outTradeNo", outTradeNo)
                .build();

        ResultDto orderDto = RemoteUtils.call("service.productcooperation.getorder",
                ApiServiceTypeEnum.COOPERATION, params, true);

        if (orderDto == null || !ResultDto.SUCCESS.equals(orderDto.getCode()) || null == orderDto.getData()) {
            return new StatusObjDto<>(true, null, StatusDto.FAIL_COMMON, "");
        }

        Map<String, Object> orderMap = orderDto.getData();
        String productCode = (String) orderMap.get("productCode");
        Integer totalFee = (Integer) orderMap.get("totalFee");
        Integer status = (Integer) orderMap.get("status");
        Date createTime = new Date(Long.parseLong(String.valueOf(orderMap.get("createTime"))));
        Integer time = (Integer) orderMap.get("time");
        Integer timeUnit = (Integer) orderMap.get("timeUnit");

        if (ObjectUtils.isEmptyOrNull(totalFee) || totalFee == 0) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "实际支付金额为0");
        }

        // 校验订单是否已经支付
        if (!CodeConstant.CODE_YES.equals(status)) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "订单未支付");
        }

        // 校验订单的创建时间
        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "下单时间不在活动时间范围内");
        }

        // 限制的规格为一个月
        if (!timeUnit.equals(3) || !time.equals(1)) {
            return new StatusObjDto<>(false, null, StatusDto.FAIL_COMMON, "规格不符合");
        }

        ActPrizeType actPrizeType = getPrizeTypeByTgProductCode(productCode);
        return new StatusObjDto<>(true, actPrizeType, StatusDto.SUCCESS, "");
    }

    private ActPrizeType getPrizeTypeByTgProductCode(String productCode) {
        String prizeTypeCode = "PRIZE.COUPON.TG." + productCode + ".2023DOUBLE11";
        return actPrizeTypeBiz.getPrizeType(prizeTypeCode);
    }
}
