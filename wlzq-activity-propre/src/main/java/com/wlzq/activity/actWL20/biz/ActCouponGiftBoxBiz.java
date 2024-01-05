package com.wlzq.activity.actWL20.biz;

import com.wlzq.activity.actWL20.model.ActGiftBox;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

public interface ActCouponGiftBoxBiz {
	
	/**
	 * 拆礼盒
	 * @param token
	 * @param userId
	 * @param openId
	 * @param customer
	 * @param recommendCode
	 * @return
	 */
	public StatusObjDto<CouponRecieveStatusDto> recieve(AccTokenUser user,Customer customer,String recommendCode,String shareCode);

	List<ActGiftBox> findGiftBoxes(ActGiftBox actGiftBox);

	/**
	 * 领取状态
	 * @param token
	 * @param customerId
	 * @return
	 */
	public StatusObjDto<List<CouponRecieveStatusDto>> findRecieves(AccTokenUser user,Customer customer,String shareCode);

	public ResultDto mobilehascustomer(AccTokenUser user, Customer customer, String mobile);
	
}
