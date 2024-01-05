package com.wlzq.activity.virtualfin.biz;

import com.wlzq.activity.virtualfin.dto.ActRedEnvelopeDto;
import com.wlzq.activity.virtualfin.dto.LastAmountFlowResDto;
import com.wlzq.activity.virtualfin.model.ActFinOrder;
import com.wlzq.activity.virtualfin.model.ActRedEnvelope;
import com.wlzq.core.Page;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

public interface ActRedEnvelopeBiz {

	/**
	 * 获取余额
	 * @param activityCode
	 * @param mobile
	 * @param goodsCode
	 * @return
	 */
	Double balance(String activityCode, String mobile);
	
	/**
	 * 红包流水
	 * @param activityCode
	 * @param mobile
	 * @return
	 */
	StatusObjDto<ActRedEnvelopeDto> redEnvelopeFlow(String activityCode, String mobile, Page page);


	/**
	 * 查询最近的红包/体验金流水列表
	 */
    List<LastAmountFlowResDto> getLastAmountFlow(String activityCode);

    /**
	 * 提现红包
	 * @param activityCode
	 * @param mobile
	 * @param userId TODO
	 * @param openId TODO
	 * @param customerId TODO
	 * @param quantity TODO
	 * @return
	 */
	StatusObjDto<ActRedEnvelope> withdraw(String activityCode, String mobile, String userId, String openId, String customerId, Double quantity);
	
	/**
	 * 新增流水
	 * @param toRedeemList
	 */
	void addRedEnvelope(List<ActFinOrder> toRedeemList);
}
