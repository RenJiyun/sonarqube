package com.wlzq.activity.virtualfin.biz;

import com.wlzq.activity.virtualfin.dto.ExpGoldOverviewDto;
import com.wlzq.activity.virtualfin.model.ActFirstLogin;
import com.wlzq.core.dto.StatusObjDto;

public interface ExpGoldBiz {

	/**
	 * 体验金活动预览
	 * @param activityCode
	 * @param mobile
	 * @param goodsCode TODO
	 * @param taskCodes TODO
	 * @param productCodes TODO
	 * @return
	 */
	StatusObjDto<ExpGoldOverviewDto> overview(String activityCode, String mobile, String goodsCode, String taskCodes, String productCodes);

	/**
	 * 登录状态
	 * @param activityCode
	 * @param mobile
	 * @return
	 */
	StatusObjDto<ActFirstLogin> loginStatus(String activityCode, String mobile);
}
