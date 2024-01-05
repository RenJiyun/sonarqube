package com.wlzq.activity.actWL20.biz;

import com.wlzq.activity.actWL20.dto.ActSubscribeDto;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

public interface ActCoupon818Biz {
	
	/**
	 * 领取818理财券
	 * @param activityCode
	 * @param prizeType
	 * @param userId
	 * @param openId
	 * @param customer
	 * @param recommendCode
	 * @return
	 */
	public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, String prizeType, String userId, String openId, Customer customer,String recommendCode);
	
	/**
	 * 入金登记
	 * @param customer
	 * @return
	 */
	int addFundinGo(Customer customer);
	
	/**
	 * 活动订阅
	 * @param activityCode
	 * @param mobile
	 * @param customerId
	 * @return
	 */
	int subscribe(String activityCode,String mobile,String customerId);
	
	/**
	 * 活动订阅状态查询
	 * @param activityCode
	 * @param mobile
	 * @param customerId
	 * @return
	 */
	StatusObjDto<ActSubscribeDto> subscribeCheck(String activityCode,String mobile,String customerId);
	
}
