package com.wlzq.activity.virtualfin.biz;

import com.wlzq.activity.virtualfin.dto.ActGoodsFlowDto;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.core.Page;
import com.wlzq.core.dto.StatusObjDto;

public interface ActGoodsFlowBiz {

	/**
	 * 获取余额
	 * @param activityCode
	 * @param mobile
	 * @param goodsCode
	 * @return
	 */
	Double balance(String activityCode, String mobile, String goodsCode);
	
	/**
	 * 完成任务数
	 * @param activityCode
	 * @param mobile
	 * @return
	 */
	int taskCount(String activityCode, String mobile);
	
	/**
	 * 流水列表
	 * @param activityCode
	 * @param mobile
	 * @param goodsCode
	 * @param page TODO
	 * @return
	 */
	StatusObjDto<ActGoodsFlowDto> goodsFlow(String activityCode, String mobile, String goodsCode, Page page);
	
	/**
	 * 消耗物品
	 * @param activityCode
	 * @param mobile TODO
	 * @param userId TODO
	 * @param openId TODO
	 * @param customerId TODO
	 * @param orderId TODO
	 * @param goodsCode
	 * @param quantity
	 * @return
	 */
	ActGoodsFlow consume(String activityCode, String productCode, String mobile, String userId, String openId, String customerId, String orderId, String goodsCode, Double quantity);

}
