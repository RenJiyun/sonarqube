package com.wlzq.activity.base.biz;

import java.util.List;

import com.wlzq.activity.base.model.ActMessage;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 活动留言biz
 * @author cjz
 *
 */
public interface ActMessageBiz {

	/**
	 * 留言
	 * @param user
	 * @param activityCode 活动编码
	 * @param content
	 * @return
	 */
	public StatusObjDto<ActMessage> leavingMessage(AccTokenUser user, String activityCode, String content);

	/**
	 * 获取留言列表
	 * @param lastOrder 上一次最大id
	 * @param length 返回条数
	 * @param activityCode 活动编码
	 * @return
	 */
	public StatusObjDto<List<ActMessage>> getMsgList(Integer lastOrder, Integer length, String activityCode);
	
	/**
	 * 活动中是否已留言
	 * @param activityCode
	 * @param openId
	 */
	public boolean isLeftMessage(String activityCode, String openId);
	
	/**
	 * 活动中是否有有效留言
	 * @param activityCode
	 * @param openId
	 */
	public boolean isLeftValidMessage(String activityCode, String openId);

}
