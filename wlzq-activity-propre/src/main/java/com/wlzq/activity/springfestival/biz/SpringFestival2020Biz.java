package com.wlzq.activity.springfestival.biz;

import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.springfestival.dto.LotteryPreviewDto;
import com.wlzq.core.dto.StatusObjDto;

public interface SpringFestival2020Biz {

	/**
	 * 抽奖预览
	 * @param userId 用户号
	 * @param openId 微信号
	 * @param customerId 客户号
	 * @return
	 */
	StatusObjDto<LotteryPreviewDto> lotteryPreview(String userId, String openId, String customerId);

	/**
	 * 抽奖
	 * @param userId
	 * @param openId
	 * @param customerId
	 * @param mobile TODO
	 * @return
	 */
	StatusObjDto<LotteryDto> lottery(String userId, String openId, String customerId, String mobile);

}
