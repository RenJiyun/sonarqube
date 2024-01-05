package com.wlzq.activity.virtualfin.biz;

import java.util.List;

import com.wlzq.activity.virtualfin.model.ActFinOrder;
import com.wlzq.core.dto.StatusObjDto;

public interface ActFinOrderBiz {

	/**
	 * 获取订单列表
	 * @param activityCode
	 * @param productCodes
	 * @return
	 */
	StatusObjDto<List<ActFinOrder>> orders(String activityCode, String mobile);

	/**
	 * 购买产品
	 * @param activityCode
	 * @param mobile
	 * @param productCode TODO
	 * @param price TODO
	 * @param userId TODO
	 * @param openId TODO
	 * @param customerId TODO
	 * @return
	 */
	StatusObjDto<ActFinOrder> buy(String activityCode, String mobile, String productCode, Double price, String userId, String openId, String customerId);
	
	/**
	 * 更新订单状态
	 * @return
	 */
	StatusObjDto<List<ActFinOrder>> updStaus();
}
