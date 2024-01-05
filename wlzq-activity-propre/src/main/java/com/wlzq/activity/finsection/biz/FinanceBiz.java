package com.wlzq.activity.finsection.biz;

import java.util.List;

import com.wlzq.activity.base.model.ActHotLine;
import com.wlzq.activity.base.model.ActTeam;
import com.wlzq.activity.finsection.dto.ActTeamDto;
import com.wlzq.activity.finsection.dto.FinSectionScheduleDto;
import com.wlzq.activity.finsection.dto.StepActivtyOverview;
import com.wlzq.core.dto.StatusObjDto;

public interface FinanceBiz {
	
	/**
	 * 查询当前时间理财节所在阶段
	 * @return
	 */
	public StatusObjDto<FinSectionScheduleDto> getSchedule();
	
	/**
	 * 查询用户的预约情况
	 * @param appoinmentCode
	 * @param userId
	 * @param custmerId
	 * @return
	 */
	public StatusObjDto<Integer> appointmentCount(String appoinmentCode, String userId, String customerId);
	
	/**
	 * 用户登记预约
	 * @param appoinmentCode
	 * @param appointmentName TODO
	 * @param userId
	 * @param customerId
	 * @param phone
	 * @param appointmentTime TODO
	 * @return
	 */
	public StatusObjDto<Integer> appointment(String appoinmentCode, String appointmentName, String userId, String customerId, String userName, String phone, String appointmentTime, String template);
	
	/**
	 * 获取营业部热线
	 * @param customerId
	 * @return
	 */
	public StatusObjDto<ActHotLine> getHotLine(String userId, String customerId);
	
	
	/**
	 * 点击营业部热线登记
	 * @param customerId
	 * @return
	 */
	public StatusObjDto<Integer> hotlineClick(String userId, String customerId);
	
	/**
	 * 组队大厅
	 * @param successCount 成功组队数量
	 * @param formingCount 求组队数量
	 * @return
	 */
	public StatusObjDto<ActTeamDto> teamhall(Integer successCount, Integer formingCount);

	/**
	 * 创建队伍
	 * @param userId 用户ID
	 * @param customerId 客户ID
	 * @param template TODO
	 * @param openId TODO
	 * @return
	 */
	public StatusObjDto<ActTeam> createTeamToGetCoupon(String activityCode, String userId, String customerId, String template, String openId);
	

	/**
	 * 查看我的队伍
	 * @param userId
	 * @param customerId
	 * @param template
	 * @return
	 */
	public StatusObjDto<ActTeam> myTeam(String userId, String customerId, String template);

	/**
	 * 解散我的队伍
	 * @param userId
	 * @param customerId
	 * @param teamSerial
	 * @return
	 */
	public StatusObjDto<Integer> dismissTeam(String userId, String customerId, String teamSerial);

	/**
	 * 组队
	 * @param teamSerial
	 * @param userId
	 * @param customerId
	 * @param type
	 * @return
	 */
	public StatusObjDto<ActTeam> formTeam(String teamSerial, String userId, String customerId, Integer type);
	
	/**
	 * 点亮队伍
	 * @param teamSerial
	 * @param userId
	 * @param customerId
	 * @param openId TODO
	 * @return
	 */
	public StatusObjDto<ActTeam> lightTeam(String activityCode, String teamSerial, String userId, String nickName, String portrait, String customerId, String openId);

	/**
	 * 查找队伍
	 * @param teamSerial
	 * @param userId TODO
	 * @param customerId TODO
	 * @param openId TODO
	 * @return
	 */
	public StatusObjDto<ActTeam> findTeamBySerial(String teamSerial, String userId, String customerId, String openId);

	/**
	 * 查找队伍
	 * @param maxCount TODO
	 * @param teamSerial
	 * @return
	 */
	public List<ActTeam> autoFormTeam(Integer maxCount);

	/**
	 * 点亮队伍次数
	 * @param activityCode
	 * @param template
	 * @param userId
	 * @param openId TODO
	 * @return
	 */
	public StatusObjDto<Integer> lightCount(String activityCode, String template, String userId, String openId);

	public StatusObjDto<StepActivtyOverview> stepOverView(String activity, String prizeTypeCode, String customerId);
}
