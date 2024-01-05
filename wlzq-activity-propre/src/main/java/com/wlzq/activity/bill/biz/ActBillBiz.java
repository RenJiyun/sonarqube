package com.wlzq.activity.bill.biz;

import com.wlzq.activity.bill.dto.ActBillDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

public interface ActBillBiz {
	
	/**
	 * 账单信息
	 * @param user
	 * @param customer
	 * @return
	 */
	StatusObjDto<ActBillDto> view(AccTokenUser user, Customer customer);
	
	/**
	 * 许愿
	 * @param user
	 * @param customer
	 * @param wish
	 * @return
	 */
	ResultDto wish(AccTokenUser user, Customer customer, String wish);
	
	/**
	 * 分享
	 * @param user
	 * @param customer
	 * @return
	 */
	ResultDto share(AccTokenUser user, Customer customer);

}
