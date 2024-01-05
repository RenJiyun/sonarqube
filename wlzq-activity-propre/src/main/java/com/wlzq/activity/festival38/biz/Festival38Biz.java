package com.wlzq.activity.festival38.biz;

import java.util.List;

import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

public interface Festival38Biz {

	/**
	 * 领取
	 * 
	 * @param activityCode
	 * @param prizeType
	 * @param user
	 * @param openId
	 * @param customer
	 * @param recommendCode
	 * @return
	 */
	public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, AccTokenUser user, String openId, Customer customer, String recommendCode);

	/**
	 * 领取状态
	 * @param activityCode
	 * @param user
	 * @param customerId
	 * @param recommendCode
	 * @return
	 */
	public StatusObjDto<List<CouponRecieveStatusDto>> status(String activityCode, AccTokenUser user, String customerId, String recommendCode);
	
	/**
	 * 检查手机号是否有客户号
	 * @param mobile
	 * @return
	 */
	public StatusObjDto<String> checkmobile(String mobile);

}
