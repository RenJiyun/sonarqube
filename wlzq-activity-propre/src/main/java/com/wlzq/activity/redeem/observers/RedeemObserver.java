package com.wlzq.activity.redeem.observers;

import com.wlzq.core.dto.StatusDto;

/**
 * 兑换码业务领取通知接口
 * @author louie
 *
 */
public interface RedeemObserver {
	
	/**
	 * 兑换码领取通知
	 * @param userId
	 * @param code
	 * @return
	 */
	public StatusDto notify(String userId,String code);
	
}
