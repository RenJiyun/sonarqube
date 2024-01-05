package com.wlzq.activity.virtualfin.biz.impl;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.ActivityConstant;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.task.biz.TaskBiz;
import com.wlzq.activity.virtualfin.biz.ActTaskExpGoldBiz;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.dao.ActRedEnvelopeDao;
import com.wlzq.activity.virtualfin.dao.ActTaskDao;
import com.wlzq.activity.virtualfin.dto.Level2OrderQueryResDto;
import com.wlzq.activity.virtualfin.dto.TjdOpenResDto;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActRedEnvelope;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActTaskExpGoldBizImpl extends ActivityBaseBiz implements ActTaskExpGoldBiz {

	@Autowired
	private ActTaskDao actTaskDao;
	@Autowired
	private ActGoodsFlowDao actGoodsFlowDao;
	@Autowired
	private ActRedEnvelopeDao redEnvelopeDao;
	@Autowired
	private TaskBiz taskBiz;
	@Autowired
	private ActivityDao activityDao;
	
	@Override
	public List<ActTask> getTaskStatus(String activityCode, String mobile, String taskCodes) {
		ActTask entity = new ActTask();
		entity.setActivityCode(activityCode);
		List<String> codeList = Lists.newArrayList();
		if (ObjectUtils.isNotEmptyOrNull(taskCodes)) {
			codeList = Lists.newArrayList(taskCodes.split(",")); 
			entity.setCodeList(codeList);
		}
		List<ActTask> list = actTaskDao.findList(entity);
		/**如果手机号为空，设置任务默认转态**/
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			list = setInitTaskList(list);
		} else {
			ActGoodsFlow goodsFlow = new ActGoodsFlow();
			goodsFlow.setMobile(mobile);
			goodsFlow.setActivityCode(activityCode);
			/**若任务代码为空，默认该活动下所有任务**/
			if (ObjectUtils.isEmptyOrNull(taskCodes)) {
				codeList = list.stream().map(ActTask :: getCode).collect(Collectors.toList());
			}
			goodsFlow.setTaskCodeList(codeList);
			List<ActGoodsFlow> flowList = actGoodsFlowDao.findList(goodsFlow);
			list = setCurrentTaskList(list, flowList);
		}
		return list;
	}
	
	private List<ActTask> setInitTaskList(List<ActTask> list) {
		if (list != null) {
			for (ActTask actTask : list) {
				actTask.setCompleteNum(0);
			}
		}
		return list;
	}
	
	private List<ActTask> setCurrentTaskList(List<ActTask> list, List<ActGoodsFlow> flowList) {
		/**按任务分组计次**/
		Map<String, Long> taskFlowMap =  flowList.stream().collect(Collectors.groupingBy(ActGoodsFlow :: getTaskCode, Collectors.counting()));	
		/**筛选今天完成的次数**/
		Date todayStart = DateUtils.getDayStart(new Date());
		Date todayEnd = DateUtils.getDayEnd(new Date());
		Map<String, Long> todayTaskFlowMap =  flowList.stream().filter(e -> todayStart.before(e.getCreateTime()) && todayEnd.after(e.getCreateTime())).collect(Collectors.groupingBy(ActGoodsFlow :: getTaskCode, Collectors.counting()));
		for (ActTask task : list) {
			String taskCode = task.getCode();
			Map<String, Long> map = Maps.newHashMap();
			if (ActTask.DAILY_TASK.equals(task.getTaskType())) {
				map = todayTaskFlowMap;
			}
			if (ActTask.ONCE_TASK.equals(task.getTaskType())) {
				map = taskFlowMap;
			}
			Integer completeNum = map.get(taskCode) == null ? 0 : map.get(taskCode).intValue();
			task.setCompleteNum(completeNum);
		}
		return list;
	}

	@Override
	public StatusObjDto<List<ActTask>> taskStatus(String activityCode, String mobile, String taskCodes) {
		List<ActTask> list = getTaskStatus(activityCode, mobile, taskCodes);
		return new StatusObjDto<>(true, list, CodeConstant.SUCCESS, "");
	}


	@Override
	public StatusObjDto<ActTask> doTask(String activityCode, String mobile, String taskCode,
										String userId, String openId, String customerId,
										String actGoodsFlowOrderId,Date compTaskTime) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
		if (ObjectUtils.isEmptyOrNull(taskCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("taskCode");
		}
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<>(true, new ActTask(), StatusDto.SUCCESS, "");
		}
		StatusDto isValid = isValid(activityCode);

		//点赞的要去查一下
		if (ActivityConstant.TASK_ACT_21818_ARTICLE_LIKE.equals(taskCode)) {
			LocalDate localDate = LocalDate.now();
			String format = localDate.format(DatePattern.NORM_DATE_FORMATTER);
			String startTimeStr = format + "00:00:00";
			String endTimeStr = format + "23:59:59";
 			Map<String,Object> bizMap = new HashMap<>();
			bizMap.put("userId",userId);
			bizMap.put("status",1);
			bizMap.put("startTimeStr",startTimeStr);
			bizMap.put("endTimeStr",endTimeStr);
			ResultDto resultDto = RemoteUtils.call("service.articlecooperation.findlike", ApiServiceTypeEnum.COOPERATION, bizMap, true);
			if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
				Integer count = (Integer)resultDto.getData().get("count");
				if (count == null || count <= 0) {
					Map<String,Object> bizMap1 = new HashMap<>();
					bizMap1.put("userId",userId);
					bizMap1.put("mobile",mobile);
					bizMap1.put("source",10001);
					bizMap1.put("remark","点赞异常");
					RemoteUtils.call("account.blacklistcooperation.create", ApiServiceTypeEnum.COOPERATION, bizMap1, true);
				}
			}
		}

		ActTask task = getTask(activityCode, taskCode);
		if (!StatusDto.SUCCESS.equals(isValid.getCode())) {
			task.setGoodsQuantity(0.0);
			task.setRedEnvelope(0.0);
			return new StatusObjDto<>(true, task, StatusDto.SUCCESS, "");
		}


		ActGoodsFlow flow = new ActGoodsFlow();
		flow.setActivityCode(activityCode);
		flow.setTaskCode(taskCode);
		/**客户任务**/
		if (CodeConstant.CODE_YES.equals(task.getCustomerTask())) {
			/**客户号为空，不做任务**/
			if (ObjectUtils.isEmptyOrNull(customerId)) {
				task.setGoodsQuantity(0.0);
				task.setRedEnvelope(0.0);
				return new StatusObjDto<>(true, task, StatusDto.SUCCESS, "");
			}
			flow.setCustomerId(customerId);
		} else {
			/**非客户任务**/
			flow.setMobile(mobile);
		}
		List<ActGoodsFlow> actGoodsFlows = actGoodsFlowDao.findList(flow);

		Date taskTime = compTaskTime!=null ? compTaskTime : new Date();
		Date todayStart = DateUtils.getDayStart(taskTime);
		Date todayEnd = DateUtils.getDayEnd(taskTime);
		int todayCompletedCount = (int)actGoodsFlows.stream().filter(e -> todayStart.before(e.getCreateTime()) && todayEnd.after(e.getCreateTime())).count();

		/**今日还可以做任务**/
		boolean flag = ActTask.ONCE_TASK.equals(task.getTaskType()) && task.getTotalNum().compareTo(actGoodsFlows.size()) > 0
				|| ActTask.DAILY_TASK.equals(task.getTaskType()) && task.getTotalNum().compareTo(todayCompletedCount) > 0;

		if (flag) {
			ActGoodsFlow newFlow = BeanUtils.copyToNewBean(task, ActGoodsFlow.class);
			newFlow.setFlag(ActGoodsFlow.FLOW_FLAG_GET);
			newFlow.setTaskCode(taskCode);
			newFlow.setCreateTime(taskTime);
			newFlow.setUserId(userId);
			newFlow.setOpenId(openId);
			newFlow.setMobile(mobile);
			newFlow.setActivityCode(activityCode);
			newFlow.setCustomerId(customerId);
			if (StringUtils.isNotBlank(actGoodsFlowOrderId)) {
				newFlow.setOrderId(actGoodsFlowOrderId);
			}
			actGoodsFlowDao.insert(newFlow);
			task.setCompleteNum(todayCompletedCount + 1);
			/**任务设置红包**/
			if (ObjectUtils.isNotEmptyOrNull(task.getRedEnvelope()) && task.getRedEnvelope().compareTo(0.0) > 0) {
				ActRedEnvelope actRedEnvelope = new ActRedEnvelope();
				actRedEnvelope.setActivityCode(activityCode);
				actRedEnvelope.setMobile(mobile);
				actRedEnvelope.setUserId(userId);
				actRedEnvelope.setOpenId(openId);
				actRedEnvelope.setCustomerId(customerId);
				actRedEnvelope.setFlag(ActRedEnvelope.FLOW_FLAG_GET);
				actRedEnvelope.setBusinessCode(taskCode);
				actRedEnvelope.setBusinessName(task.getName() + "红包");
				actRedEnvelope.setQuantity(task.getRedEnvelope());
				actRedEnvelope.setStatus(CodeConstant.CODE_YES);
				actRedEnvelope.setCreateTime(taskTime);
				redEnvelopeDao.insert(actRedEnvelope);
			}
		} else {
			/**任务已做完，体验金金额和红包返回0**/
			task.setGoodsQuantity(0.0);
			task.setRedEnvelope(0.0);
			task.setCompleteNum(task.getTotalNum());
		}
		return new StatusObjDto<ActTask>(true, task, StatusDto.SUCCESS, "");
	}


	private ActTask getTask(String activityCode, String taskCode) {
		ActTask task = actTaskDao.findByCode(activityCode, taskCode);
		if (task == null || CodeConstant.CODE_NO.equals(task.getStatus())) {
			throw ActivityBizException.ACT_TASK_NOT_EXIST;
		}
		return task;
	}
	
	@Override
	public Boolean checkTask(String activityCode, String mobile, String taskCode) {
		if (ObjectUtils.isEmptyOrNull(activityCode) || ObjectUtils.isEmptyOrNull(mobile)) {
			return true;
		}
		ActTask entity = new ActTask();
		entity.setActivityCode(activityCode);
		entity.setCode(taskCode);
		List<ActTask> tasks = actTaskDao.findList(entity);
		for (ActTask task : tasks) {
			Integer count = actGoodsFlowDao.getDoTaskCount(activityCode, task.getCode(), mobile, task.getTaskType());
			if (ObjectUtils.isNotEmptyOrNull(count) && count > task.getTotalNum()) {
				return false;
			}
		}
		return true;
	}


	/**
	 * 刷新查询任务状态，并更新任务的
	 * @return
	 */
	@Override
	public List<ActTask> flushActTaskStatus(String activityCode, String mobile, List<String> taskCodes, AccTokenUser user, Customer customer) {
		List<ActTask> actTasks = new ArrayList<>();
		Activity activity = activityDao.findActivityByCode(activityCode);
		long actStartTime = activity.getDateFrom().getTime();
		long actEndTime = activity.getDateTo().getTime();


		//region 刷新条件单
		if (customer!=null) {
			flushTjd(activityCode, mobile, user, customer, actTasks, activity);
		}
		//endregion

		//region 刷新level2
		Map<String, Object> pMap = new HashMap<>();
		pMap.put("mobile", mobile);
		ResultDto resultDto = RemoteUtils.call("base.level2.listorders", ApiServiceTypeEnum.COOPERATION, pMap, true);
		if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
			String jsonStr = (String) resultDto.getData().get("list");
			List<Level2OrderQueryResDto> level2OrderQueryResDtos = JSON.parseArray(jsonStr, Level2OrderQueryResDto.class);
			if (!CollectionUtils.isEmpty(level2OrderQueryResDtos)) {
				ActTask actTask = new ActTask()
					.setCode(ActivityConstant.TASK_ACT_21818_LEVEL2_BUY)
					.setCompleteNum(level2OrderQueryResDtos.size());

				Double totalGoodsQuantity = 0d;
				//要支持，一次刷新几天的level2任务。而且要在活动期间。
				for (Level2OrderQueryResDto resDto : level2OrderQueryResDtos) {
					String orderNo = resDto.getOrderNo();
					Date orderTime = resDto.getCreateTime();
					if (orderTime.getTime() < actStartTime || orderTime.getTime() > actEndTime) {
						continue;
					}

					//完成任务
					String customerId = customer!=null ? customer.getCustomerId():"";
					StatusObjDto<ActTask> statusObjDto = checkBeforeDoTask(activityCode, ActivityConstant.TASK_ACT_21818_LEVEL2_BUY, orderNo, mobile,
							user.getUserId(), user.getOpenid(), customerId,orderTime);
					if (statusObjDto!=null && statusObjDto.isOk()) {
						ActTask dbActTask = statusObjDto.getObj();
						Double goodsQuantity = dbActTask.getGoodsQuantity();
						totalGoodsQuantity += goodsQuantity;
						actTask.setTaskType(dbActTask.getTaskType());
						actTask.setTotalNum(dbActTask.getTotalNum());
					}
				}
				if (totalGoodsQuantity>0) {
					actTasks.add(actTask);
					actTask.setGoodsQuantity(totalGoodsQuantity);
				}
			}
		}
		//endregion
		return actTasks;
	}

	private ActTask flushTjd(String activityCode, String mobile, AccTokenUser user, Customer customer, List<ActTask> actTasks, Activity activity) {
		ActTask actTask = null;

		//如果开通条件配置了只能一次，同时任务也完成了。就直接返回空
		ActTask task = getTask(activityCode, ActivityConstant.TASK_ACT_21818_TJD);
		ActGoodsFlow flow = new ActGoodsFlow();
		flow.setActivityCode(activityCode);
		flow.setTaskCode(ActivityConstant.TASK_ACT_21818_TJD);
		flow.setUserId(user.getUserId());
		flow.setCustomerId(customer.getCustomerId());
		List<ActGoodsFlow> actGoodsFlows = actGoodsFlowDao.findList(flow);
		Date now = new Date();
		Date todayStart = DateUtils.getDayStart(now);
		Date todayEnd = DateUtils.getDayEnd(now);
		int todayCompletedCount = (int)actGoodsFlows.stream().filter(e -> todayStart.before(e.getCreateTime()) && todayEnd.after(e.getCreateTime())).count();
		boolean flag = ActTask.ONCE_TASK.equals(task.getTaskType()) && task.getTotalNum().compareTo(actGoodsFlows.size()) > 0
				|| ActTask.DAILY_TASK.equals(task.getTaskType()) && task.getTotalNum().compareTo(todayCompletedCount) > 0;
		if (!flag) {
			return null;
		}


		Date dateTo = activity.getDateTo();
		String endDateStr = DateUtils.formate(dateTo, "yyyy-MM-dd HH:mm:ss");

		Map<String, Object> bizMap = new HashMap<>();
		bizMap.put("customerId", customer.getCustomerId());
		bizMap.put("endDateStr", endDateStr);
		ResultDto resDto = RemoteUtils.call("account.tjd.querytjdopen", ApiServiceTypeEnum.COOPERATION, bizMap, true);
		if (ResultDto.SUCCESS.equals(resDto.getCode())) {
			Integer times = (Integer)resDto.getData().get("times");

			String data = (String) resDto.getData().get("data");
			List<TjdOpenResDto> tjdOpenResDtos = JSON.parseArray(data, TjdOpenResDto.class);
			if (!CollectionUtils.isEmpty(tjdOpenResDtos)) {
				actTask = new ActTask();
				actTask.setCode(ActivityConstant.TASK_ACT_21818_TJD);
//				for (TjdOpenResDto tjdOpenResDto : tjdOpenResDtos) {
				TjdOpenResDto tjdOpenResDto = tjdOpenResDtos.get(0);// 先只支持一次算了。
				String customerId = tjdOpenResDto.getCustomerId();
				Date openTime1 = tjdOpenResDto.getOpenTime();
				String openTime = DateUtils.formate(openTime1,"yyyyMMddHHmmss");

				String unitKey = customerId+openTime;
				StatusObjDto<ActTask> statusObjDto = checkBeforeDoTask(activityCode, ActivityConstant.TASK_ACT_21818_TJD, unitKey, mobile,
						user.getUserId(), user.getOpenid(), customer.getCustomerId(),null);
				if (statusObjDto!=null && statusObjDto.isOk()) {
					ActTask actTask1 = statusObjDto.getObj();
					if (actTask1.getGoodsQuantity()>0) {
						actTask.setGoodsQuantity(actTask1.getGoodsQuantity());
						actTask.setCompleteNum(tjdOpenResDtos.size());
						actTask.setTotalNum(actTask1.getTotalNum());
						actTask.setTaskType(actTask1.getTaskType());
						actTasks.add(actTask);
					}
				}
			}
		}
		return actTask;
	}

	/**
	 * DoTask
	 * (检查是否已完成 ActGoodsFlow)
	 * @return
	 */
	private StatusObjDto<ActTask> checkBeforeDoTask(String activityCode, String taskCode, String bizCode, String mobile,
													String userId, String openId, String customerId,Date compTaskTime) {
		if (StringUtils.isNotBlank(bizCode)) {
			ActGoodsFlow qty = new ActGoodsFlow();
			qty.setOrderId(bizCode).setActivityCode(activityCode).setTaskCode(taskCode).setMobile(mobile);
			List<ActGoodsFlow> list = actGoodsFlowDao.findList(qty);
			int listSize = list.size();
			if (listSize <=0) {
				return doTask(activityCode,mobile,taskCode,userId,openId,customerId ,bizCode,compTaskTime);
			}
		}
		return null;
	}

}
