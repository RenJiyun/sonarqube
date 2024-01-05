package com.wlzq.activity.actWL20.biz;

import java.util.List;

import com.wlzq.activity.actWL20.dto.ActFDRecieveDto;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

public interface ActCouponFDBiz {
	
	/**
	 * 818领取福袋
	 * @param activityCode
	 * @param prizeType
	 * @param userId
	 * @param openId
	 * @param customer
	 * @param recommendCode
	 * @return
	 */
	public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, String userId, String openId, Customer customer,String recommendCode);

	/**
	 * 福袋领取查询
	 * @param activityCode
	 * @param customerId
	 * @return
	 */
	public StatusObjDto<List<ActFDRecieveDto>> findRecieves(String activityCode, String customerId);
	
}
