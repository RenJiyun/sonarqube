package com.wlzq.activity.base.biz;

import com.wlzq.activity.base.model.ActActivityVoucher;

/**
 * 活动兑换码凭证业务类
 * @author Administrator
 *
 */
public interface ActActivityVoucherBiz {

	/**
	 * 生成新的兑换码凭证
	 * @param userId
	 * @param activityCode
	 * @return
	 */
	ActActivityVoucher generateVoucher(String userId, String activityCode);
}
