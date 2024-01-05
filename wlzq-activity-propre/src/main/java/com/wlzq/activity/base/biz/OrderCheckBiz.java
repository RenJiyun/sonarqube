package com.wlzq.activity.base.biz;

import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

/**
 * @author renjiyun
 */
public interface OrderCheckBiz {

    /**
     * 校验决策商城订单
     *
     * @param user
     * @param activity
     * @param outTradeNo
     * @return
     */
    StatusObjDto<ActPrizeType> checkVasOrder(AccTokenUser user, Activity activity, String outTradeNo);

    /**
     * 校验投顾订单
     *
     * @param user
     * @param customer
     * @param activity
     * @param outTradeNo
     * @return
     */
    StatusObjDto<ActPrizeType> checkServiceOrder(AccTokenUser user, Customer customer, Activity activity, String outTradeNo);
}
