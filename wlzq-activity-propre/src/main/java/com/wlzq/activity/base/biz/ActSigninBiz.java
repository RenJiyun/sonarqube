package com.wlzq.activity.base.biz;

import java.util.List;

import com.wlzq.activity.base.model.ActSignin;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 活动签到biz
 * @author cjz
 *
 */
public interface ActSigninBiz {
	
	/**
	 * 签到
	 * @param user
	 * @param activityCode 活动编码
	 * @return
	 */
	public StatusObjDto<ActSignin> signIn(AccTokenUser user, String activityCode);

	/**
	 * 获取签到列表
	 * @param activityCode 活动编码
	 * @param pageIndex 分页页码
	 * @param pageSize 分页大小
	 * @return
	 */
	public StatusObjDto<List<ActSignin>> getSignInList(String activityCode, Integer pageIndex, Integer pageSize);

}
