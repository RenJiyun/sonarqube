package com.wlzq.activity.virtualfin.biz;

import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

import java.util.Date;
import java.util.List;

public interface ActTaskExpGoldBiz {

	/**
	 * 任务状态
	 * @param activityCode
	 * @param mobile
	 * @return
	 */
	List<ActTask> getTaskStatus(String activityCode, String mobile, String taskCodes);
	
	/**
	 * 任务状态
	 * @param activityCode
	 * @param mobile
	 * @return
	 */
	StatusObjDto<List<ActTask>> taskStatus(String activityCode, String mobile, String taskCodes);

	/**
	 * 完成任务
	 * @param activityCode
	 * @param mobile
	 * @param taskCode
	 * @param userId TODO
	 * @param openId TODO
	 * @param customerId TODO
	 * @param compTaskTime 完成活动时间
	 * @return
	 */
	StatusObjDto<ActTask> doTask(String activityCode, String mobile, String taskCode,
								 String userId, String openId, String customerId, String actGoodsFlowOrderId, Date compTaskTime);
	
	/**
	 * 检查任务
	 * @param activityCode
	 * @param mobile
	 * @param taskCode
	 * @return
	 */
	Boolean checkTask(String activityCode, String mobile, String taskCode);

	/**
	 * 刷新任务状态
	 */
	List<ActTask> flushActTaskStatus(String activityCode, String mobile, List<String> taskCodes, AccTokenUser user, Customer customer);

}
