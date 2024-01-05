package com.wlzq.activity.couponreceive.biz;

import java.util.List;
import java.util.Map;

import com.wlzq.activity.couponreceive.dto.CouponsDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 优惠券领取活动业务接口
 * @author louie
 *
 */
public interface CouponRecieveBiz {

	/**
	 * 
	 * @param customerId
	 * @return
	 */
	public StatusObjDto<Map<Integer,CouponsDto>> coupons(String customerId );
	
	/**
	 * 领取优惠券
	 * @param couponId
	 * @param customerId
	 * @param userId
	 * @return
	 */
	public StatusObjDto<Integer> recieve(String couponId,String customerId,String userId);
}
