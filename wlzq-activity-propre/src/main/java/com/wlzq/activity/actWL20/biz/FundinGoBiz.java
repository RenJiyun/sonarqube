package com.wlzq.activity.actWL20.biz;

import com.wlzq.core.dto.ResultDto;

public interface FundinGoBiz {
	
	/**
	 * 更新入金信息
	 * @return
	 */
	ResultDto updateFundinGo();
	
	/**
	 * 查询客户历史入金信息
	 * @param customerId
	 * @return
	 */
	ResultDto historyRJ(String customerId);
	
	/**
	 * 查询客户当日入金信息
	 * @param customerId 客户号
	 * @return
	 */
	public ResultDto todayRJ(String customerId);
}
