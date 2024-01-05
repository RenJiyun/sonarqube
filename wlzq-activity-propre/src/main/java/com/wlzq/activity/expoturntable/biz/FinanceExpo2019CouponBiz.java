package com.wlzq.activity.expoturntable.biz;

import java.util.Map;

import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

public interface FinanceExpo2019CouponBiz {

	public StatusObjDto<Map<String, Object>> myCoupon(AccTokenUser user);

	public StatusObjDto<Map<String, Object>> getCoupon(AccTokenUser user, Customer customer);

	public StatusObjDto<Map<String, Object>> useCoupon(AccTokenUser user, Customer customer);

}
