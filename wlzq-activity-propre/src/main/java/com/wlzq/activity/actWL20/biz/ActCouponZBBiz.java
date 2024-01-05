package com.wlzq.activity.actWL20.biz;

import java.util.List;

import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

public interface ActCouponZBBiz {

	/**
	 * 直播领取
	 * 
	 * @param activityCode
	 * @param prizeType
	 * @param userId
	 * @param openId
	 * @param customer
	 * @param recommendCode
	 * @return
	 */
	public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, String userId, String openId, Customer customer, String recommendCode);

	/**
	 * 领取状态
	 * 
	 * @param activityCode
	 * @param prizeTypes
	 * @param customerId
	 * @param recommendCode
	 * @return
	 */
	public StatusObjDto<List<CouponRecieveStatusDto>> status(String activityCode, String userId, String customerId, String recommendCode);

}
