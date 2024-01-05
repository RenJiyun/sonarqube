package com.wlzq.activity.task.biz;

import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.remote.service.utils.RemoteUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 特定任务校验
 *
 * @author renjiyun
 */
public interface SpecificTaskBiz {

    /**
     * 获取支持的任务
     *
     * @return
     */
    String[] supportTaskCodes();


    /**
     * 校验任务是否可以完成
     *
     * @param activity
     * @param actTask
     * @param actGoodsFlowList
     * @param bizCode
     * @param mobile
     * @param user
     * @param customer
     * @param recommendMobile
     * @param isBatch
     */
    Tuple checkTask(Activity activity, ActTask actTask, List<ActGoodsFlow> actGoodsFlowList, String bizCode,
                    String mobile, AccTokenUser user, Customer customer, String recommendMobile, boolean isBatch);

    /**
     * 排除用户或客户不满足指定条件的任务
     *
     * @param candidateTaskCodes
     * @param user
     * @param customer
     * @return
     */
    void excludeTaskCodes(Set<String> candidateTaskCodes, AccTokenUser user, Customer customer);

    /**
     * 获取投顾订单
     *
     * @param customerId
     * @param outTradeNo
     * @return
     */
    default Map<String, Object> getServiceOrder(String customerId, String outTradeNo) {
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("customerId", customerId)
                .put("outTradeNo", outTradeNo)
                .build();

        ResultDto orderDto = RemoteUtils.call("service.productcooperation.getorder",
                ApiServiceTypeEnum.COOPERATION, params, true);

        if (orderDto == null || !ResultDto.SUCCESS.equals(orderDto.getCode()) || null == orderDto.getData()) {
            return null;
        }

        return orderDto.getData();
    }

    /**
     * 获取投顾订单列表
     *
     * @param customerId
     * @param productCode
     * @return
     */
    default List<Map<String, Object>> getServiceOrderList(String customerId, String productCode) {
        Date createTimeFrom = DateUtils.parseDate("2000-01-01", "yyyy-MM-dd");
        Date createTimeTo = DateUtils.parseDate("2099-01-01", "yyyy-MM-dd");
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("customerId", customerId)
                .put("productCode", productCode)
                .put("createTimeFrom", createTimeFrom.getTime())
                .put("createTimeTo", createTimeTo.getTime())
                .build();

        ResultDto orderDto = RemoteUtils.call("service.productcooperation.orderrecords",
                ApiServiceTypeEnum.COOPERATION, params, true);

        if (orderDto == null || !ResultDto.SUCCESS.equals(orderDto.getCode()) || null == orderDto.getData()) {
            return null;
        }

        Map<String, Object> data = orderDto.getData();
        return (List<Map<String, Object>>) data.get("info");
    }


    /**
     * 获取决策商城订单列表
     *
     * @param mobile
     * @param goodsCode
     * @return
     */
    default List<Map<String, Object>> getVasOrderList(String mobile, String goodsCode) {
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("mobile", mobile)
                .put("goodsCode", goodsCode)
                .put("status", 1)
                .build();

        ResultDto orderDto = RemoteUtils.call("vas.decisioncooperation.findorders",
                ApiServiceTypeEnum.COOPERATION, params, true);

        if (orderDto == null || ResultDto.FAIL_COMMON.equals(orderDto.getCode()) || null == orderDto.getData()) {
            return null;
        }

        Map<String, Object> data = (Map<String, Object>) orderDto.getData();
        if (data != null) {
            return (List<Map<String, Object>>) data.get("info");
        }
        return null;
    }


    /**
     * 获取决策商城订单
     *
     * @param mobile
     * @param outTradeNo
     * @return
     */
    default Map<String, Object> getVasOrder(String mobile, String outTradeNo) {
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("mobile", mobile)
                .put("outTradeNo", outTradeNo)
                .build();

        ResultDto orderDto = RemoteUtils.call("vas.decisioncooperation.orderdetail",
                ApiServiceTypeEnum.COOPERATION, params, true);

        if (orderDto == null || ResultDto.FAIL_COMMON.equals(orderDto.getCode()) || null == orderDto.getData()) {
            return null;
        }

        return orderDto.getData();
    }

    /**
     * 根据使用的优惠券编码获取决策商城订单
     *
     * @param mobile
     * @param couponCode
     * @return
     */
    default Map<String, Object> getVasOrderByCouponCode(String couponCode) {
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("couponCode", couponCode)
                .build();

        ResultDto orderDto = RemoteUtils.call("vas.decisioncooperation.orderdetail",
                ApiServiceTypeEnum.COOPERATION, params, true);

        if (orderDto == null || ResultDto.FAIL_COMMON.equals(orderDto.getCode()) || null == orderDto.getData()) {
            return null;
        }
        return orderDto.getData();
    }
}
