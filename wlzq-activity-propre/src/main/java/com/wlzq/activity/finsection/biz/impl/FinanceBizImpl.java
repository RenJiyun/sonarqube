package com.wlzq.activity.finsection.biz.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.ActivityConstant;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActAppointmentDao;
import com.wlzq.activity.base.dao.ActHotLineDao;
import com.wlzq.activity.base.dao.ActTeamDao;
import com.wlzq.activity.base.dao.ActTeamMemberDao;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.model.ActActivityStepEnum;
import com.wlzq.activity.base.model.ActAppointment;
import com.wlzq.activity.base.model.ActHotLine;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActTeam;
import com.wlzq.activity.base.model.ActTeamMember;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.finsection.biz.FinanceBiz;
import com.wlzq.activity.finsection.dto.ActTeamDto;
import com.wlzq.activity.finsection.dto.FinSectionScheduleDto;
import com.wlzq.activity.finsection.dto.StepActivtyOverview;
import com.wlzq.activity.finsection.service.app.FinanceService;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;

/**
 * 
 * @author zhaozx
 * @version 2019-07-19
 */
@Service
public class FinanceBizImpl extends ActivityBaseBiz implements FinanceBiz {

	@Autowired
	private ActivityDao activityDao;
	
	@Autowired
	private ActAppointmentDao appointmentDao;
	
	@Autowired
	private ActTeamDao actTeamDao;
	
	@Autowired
	private ActTeamMemberDao teamMemberDao;
	
	@Autowired
	private ActHotLineDao hotLineDao;
	
	@Value("${userinfo.default.headimg}")
	private String defaultHeadimg;
	
	@Autowired
	private ActPrizeBiz actPrizeBiz;
	
	//818理财节活动组队编码
	public final static String FINSECTION_TEAM_TEAMLATE = new String("ACTIVITY.818.2019.TEAM");
	//剩余5张券则不能再获取
	public final static Integer FINSECTION_PRIZE_LEFT = new Integer(0);
	
	//818理财节活动组队编码
	public final static Integer FINSECTION_CARD_TEAM_MEMBER = new Integer(4);
	//818理财节活动组队编码
	public final static String FINSECTION_CARD_TEAM_TEAMLATE = new String("ACTIVITY.818.2019.CARD.TEAM");
	
	@Override
	public StatusObjDto<FinSectionScheduleDto> getSchedule() {
		Date now = new Date();
		List<Activity> activityList = activityDao.findList(ActivityConstant.FINSECTION_CODE_PRE);
		  
		FinSectionScheduleDto dto = new FinSectionScheduleDto();
		if (activityList.size() == 0) {
			return new StatusObjDto<FinSectionScheduleDto>(false, dto, 0, "");
		}
		//总活动开始时间
		Long begin = activityList.stream().mapToLong(act -> act.getDateFrom().getTime()).min().getAsLong();
		//总活动结束时间
		Long end = activityList.stream().mapToLong(act -> act.getDateTo().getTime()).max().getAsLong();
		//持续时间
		Long during = end - begin;
		
		dto.setNowPercent(String.format("%.8f", (double) (now.getTime() - begin) / during));
		
		for(int i = 0; i < activityList.size(); i++) {
			Activity act = activityList.get(i);
			Activity nextAct = i == activityList.size() - 1 ? null : activityList.get(i + 1);
			String code = act.getCode();
			String[] steps = null;
			Integer step = null;
			Integer subStep = null;
			String key = null;
			try {
				steps = code.split(ActivityConstant.FINSECTION_CODE_PRE)[1].split("\\.");
				step = new Integer(steps[0]);
				subStep = new Integer(steps[1]);
				key = steps[2];
			} catch (Exception e) {
				e.printStackTrace();
				throw ActivityBizException.INTERNSHIP_PARAMS_FORMAT_ERROR;
			}
			//在活动开始期间内，要求大活动之间不能时间重叠
			//设置当前活动阶段
			if ((now.compareTo(act.getDateFrom()) == 1 && now.compareTo(act.getDateTo()) == -1)) {
				dto.setActivityCode(act.getCode());
				dto.setStep(step);
				dto.getSubSteps().add(subStep);
				//设置倒计时
				if (i < activityList.size() - 1) {
					dto.setCountDown(activityList.get(i + 1).getDateFrom().getTime() - now.getTime());
				}
				//当在子阶段3的时候，不倒计时
				if (dto.getSubSteps().contains(new Integer(3))) {
					dto.setCountDown(0l);
				}
			}
			//存在活動短截情況
			if (nextAct != null && now.compareTo(act.getDateTo()) == 1 && now.compareTo(nextAct.getDateFrom()) == -1) {
				dto.setActivityCode(act.getCode());
				dto.setStep(step);
				dto.getSubSteps().add(subStep + 1);
				//设置下一阶段开始时间
				dto.setCountDown(nextAct.getDateFrom().getTime() - now.getTime());
			}
			Map<String, Object> time = Maps.newHashMap();
			time.put("dateFrom", act.getDateFrom().getTime());
			time.put("dateTo", act.getDateTo().getTime());
			String percent = String.format("%.8f", (double) (act.getDateFrom().getTime() - begin) / during);
			time.put("percent", percent);
			//领券活动，要求领券活动编码包含COUPON关键字, 如ACTIVITY.818.2019.1.2.COUPON
			//设置领券活动的时间
			if ("COUPON".equals(key)) {
				switch (step) {
				case 1:
					dto.setStep1Coupon(time);
					break;
				case 2:
					dto.setStep2Coupon(time);
					break;
				case 3:
					dto.setStep3Coupon(time);
					break;
				default:
					break;
				}
			}
			//设购买时间
			if ("TRADE".equals(key)) {
				switch (step) {
				case 1:
					dto.setStep1Trade(time);
					break;
				case 2:
					dto.setStep2Trade(time);
					break;
				case 3:
					dto.setStep3Trade(time);
					break;
				default:
					break;
				}
			}
		}
		Integer step = dto.getStep();
		
		if (step == null) {
			dto.setStep(3);
			dto.setSubSteps(Arrays.asList(3));
			dto.setNowPercent(new String("1"));
			dto.setCountDown(0l);
			dto.setLeftPrizeCount(0);
			return new StatusObjDto<FinSectionScheduleDto>(true, dto, 0, "");
		}
		
		switch (step) {
		case 1:
			dto.setActivityCode("ACTIVITY.818.2019.1.2.COUPON");
			dto.setPrizeTypeCode("PRIZE.818.2019.1.2");
			break;
		case 2:
			dto.setActivityCode("ACTIVITY.818.2019.2.2.COUPON");
			dto.setPrizeTypeCode("PRIZE.818.2019.2.2");
			break;
		case 3:
			dto.setActivityCode("ACTIVITY.818.2019.3.2.COUPON");
			dto.setPrizeTypeCode("PRIZE.818.2019.3.2");
			break;
		default:
			break;
		}
		return new StatusObjDto<FinSectionScheduleDto>(true, dto, 0, "");
	}

	@Override
	public StatusObjDto<Integer> appointmentCount(String appointmentCode, String userId, String customerId) {
		if (ObjectUtils.isEmptyOrNull(appointmentCode)) {
			throw ActivityBizException.ACTIVITY_PARAMS_NOTNULL; 
		}
		//用户Id和客户Id同时为空
		if (ObjectUtils.isEmptyOrNull(userId) && ObjectUtils.isEmptyOrNull(customerId)) {
			return new StatusObjDto<>(true, 0, 0, "");
		}
		
		ActAppointment appointment = new ActAppointment();
		appointment.setAppointmentCode(appointmentCode);
		appointment.setUserId(userId);
		appointment.setCustomerId(customerId);
		List<ActAppointment> list = appointmentDao.findList(appointment);
		Integer count = list.size();
		return new StatusObjDto<>(true, count, 0, "");
	}

	@Override
	@Transactional
	public StatusObjDto<Integer> appointment(String appointmentCode, String appointmentName,
			String userId, String customerId, String userName, String phone, String appointmentTime, String template) {
		if (ObjectUtils.isEmptyOrNull(appointmentCode)) {
			throw ActivityBizException.ACTIVITY_PARAMS_NOTNULL; 
		}
		if (ObjectUtils.isEmptyOrNull(userId)) {
			throw ActivityBizException.ACTIVITY_PARAMS_NOTNULL; 
		}
		ActAppointment appointment = new ActAppointment();
		appointment.setAppointmentCode(appointmentCode);
		appointment.setAppointmentName(appointmentName);
		appointment.setUserId(userId);
		appointment.setCustomerId(customerId);
		appointment.setPhone(phone);
		appointment.setCreateDate(new Date());
		appointment.setAppointmentTime(DateUtils.parseDate(appointmentTime, "yyyy-MM-dd HH:mm:ss"));
		int count =  appointmentDao.insert(appointment);
		//若短信模板不为空，则调用发短信接口
		if (!ObjectUtils.isEmptyOrNull(template)) {
			//获取客户所在机构的专员电话号码
			ActHotLine hotLine = hotLineDao.getByCustomerId(customerId);
			String specialistMoblie = hotLine.getMoblie();
			if (ObjectUtils.isEmptyOrNull(specialistMoblie)) {
				return new StatusObjDto<>(true, count, 0, "");
			}
			Map<String, Object> busparams = Maps.newHashMap();
			busparams.put("mobile", specialistMoblie);
			busparams.put("templateCode", template);
			//短信模板参数
			List<Object> params = new ArrayList<>();
			params.add(userName);
			params.add(appointmentTime);
			params.add(phone);
			busparams.put("params", params);
			RemoteUtils.call("base.pushcooperation.sendshortmessage", ApiServiceTypeEnum.COOPERATION, busparams, false);
		}
		return new StatusObjDto<>(true, count, 0, "");
	}

	@Override
	public StatusObjDto<ActTeamDto> teamhall( Integer successCount, Integer formingCount) {
		ActTeam team = new ActTeam();
		//获取已经组队的客户
		team.setStatus(ActTeam.SUCCESS);
		List<ActTeam> successTeams = actTeamDao.findSample(team, successCount);
		for (ActTeam actTeam : successTeams) {
			if (ObjectUtils.isEmptyOrNull(actTeam.getCreatePortrait())) {
				actTeam.setCreatePortrait(defaultHeadimg);
			}
			//查询队伍成员
			List<ActTeamMember> teamMemberList = teamMemberDao.findByTeamSerial(actTeam.getTeamSerial(), actTeam.getCreateCustomerId());
//			TODO替换头像
			for (ActTeamMember member : teamMemberList) {
				if(ObjectUtils.isEmptyOrNull(member.getPortrait())) {
					member.setPortrait(defaultHeadimg);
				}
			}
			actTeam.setMemberNameList(teamMemberList);
		}
		
		//获取组队中的客户
		team.setStatus(ActTeam.FORMING);
		List<ActTeam> formingTeams = actTeamDao.findList(team, formingCount);
		for (ActTeam actTeam : formingTeams) {
			if (ObjectUtils.isEmptyOrNull(actTeam.getCreatePortrait())) {
				actTeam.setCreatePortrait(defaultHeadimg);
			}
			List<ActTeamMember> teamMemberList = teamMemberDao.findByTeamSerial(actTeam.getTeamSerial(), actTeam.getCreateCustomerId());
			actTeam.setMemberNameList(teamMemberList);
		}
		
		ActTeamDto dto = new ActTeamDto();
		dto.setSuccesTeams(successTeams);
		dto.setFormingTeams(formingTeams);
		return new StatusObjDto<ActTeamDto>(true, dto, 0, "");
	}

	@Override
	public StatusObjDto<ActHotLine> getHotLine(String userId, String customerId) {
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			throw ActivityBizException.ACTIVITY_PARAMS_NOTNULL; 
		}
		ActHotLine hotLine = hotLineDao.getByCustomerId(customerId);
		return new StatusObjDto<ActHotLine>(true, hotLine, 0, "");
	}

	@Override
	@Transactional
	public StatusObjDto<Integer> hotlineClick(String userId, String customerId) {
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			throw ActivityBizException.ACTIVITY_PARAMS_NOTNULL; 
		}
		//拨打热线登记预约表
		try {
			String appointmentCode = ActivityConstant.FINSECTION_HOTLINE_CALL;
			String appointmentName = ActivityConstant.FINSECTION_HOTLINE_CALL_NAME;
			appointment(appointmentCode, appointmentName, userId, customerId, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new StatusObjDto<Integer>(true, new Integer(1), 0, "");
	}

	/**
	 * 创建队伍以获得优惠券
	 */
	@Override
	@Transactional
	public StatusObjDto<ActTeam> createTeamToGetCoupon(String activityCode, String userId, String customerId, String template, String openId) {
		StatusDto isValidAct = isValid(activityCode);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<ActTeam>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		checkCanCreateTeam(template, null, customerId);
		String teamSerial = UUID.randomUUID().toString().replace("-", "");
		ActTeam newTeam = ActTeam.createNewTeam(teamSerial, new String(), template,  userId, customerId, new Date(), ActTeam.FORMING);
		newTeam.setIsTeamLead(ActTeam.IS_TEAM_LEAD);
		newTeam.setOpenId(openId);
		actTeamDao.insert(newTeam);
		insertTeamMember(newTeam, ActTeamMember.TYPE_MANUAL);
		
		List<ActTeamMember> memberList = new ArrayList<>();
		newTeam.setMemberNameList(memberList);
		return new StatusObjDto<>(true, newTeam, 0, "");
	}
	
	private Integer insertTeamMember(ActTeam actTeam, Integer formType) {
		ActTeamMember teamMember = copyFromTeam(actTeam);
		teamMember.setType(formType);
		teamMember.setCreateDate(new Date());
		return teamMemberDao.insert(teamMember);
	}
	
	private static ActTeamMember copyFromTeam(ActTeam team) {
		ActTeamMember teamMember = new ActTeamMember();
		teamMember.setTeamSerial(team.getTeamSerial());
		teamMember.setUserId(team.getCreateUserId());
		teamMember.setOpenId(team.getOpenId());
		teamMember.setCustomerId(team.getCreateCustomerId());
		return teamMember;
	}

	@Override
	@Transactional
	public StatusObjDto<ActTeam> formTeam(String teamSerial, String userId, String customerId, Integer type) {
		StatusDto isValidAct = isValid(FinanceService.ACTIVITY_CODE_818_2019_1_2);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<ActTeam>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		if (ObjectUtils.isEmptyOrNull(teamSerial)) {
			throw ActivityBizException.ACTIVITY_PARAMS_NOTNULL; 
		}
		List<ActTeam> actTeamList = actTeamDao.findCreateTeam(teamSerial, null, null, null);
		if (actTeamList == null || actTeamList.size() == 0) {
			throw ActivityBizException.FINSECTION_TEAM_FAIL;
		}
		//队伍已组队成功
		ActTeam actTeam  = actTeamList.get(0);
		if (ActTeam.SUCCESS.compareTo(actTeam.getStatus()) == 0) {
			throw ActivityBizException.FINSECTION_TEAM_SUCCES;
		}
		//队伍已失效
		if (ActTeam.FAIL.compareTo(actTeam.getStatus()) == 0) {
			throw ActivityBizException.FINSECTION_TEAM_FAIL;
		}
		if (actTeam.getCreateCustomerId().equals(customerId)) {
			throw ActivityBizException.FINSECTION_HAS_TEAM;
		}
		if (ObjectUtils.isEmptyOrNull(actTeam.getCreatePortrait())) {
			actTeam.setCreatePortrait(defaultHeadimg);
		}
		actTeamDao.update(teamSerial, ActTeam.SUCCESS, actTeam.getCreateCustomerId());
		ActTeamMember member = ActTeamMember.createNewMember(teamSerial, customerId, userId, new Date(), type);
		teamMemberDao.insert(member);
		actTeam.setStatus(ActTeam.SUCCESS);
		if (actTeam.getCreateCustomerId().equals(customerId)) {
			actTeam.setIsTeamLead(ActTeam.IS_TEAM_LEAD);
		}
		return new StatusObjDto<ActTeam>(true, actTeam, 0, "");
	} 
	
	@Override
	public StatusObjDto<ActTeam> myTeam(String userId, String customerId, String template) {
		if (ObjectUtils.isEmptyOrNull(userId) && ObjectUtils.isEmptyOrNull(customerId)) {
			return new StatusObjDto<ActTeam>(true, null, 0, "");
		}
		//获取用户所在的所有队伍
//		List<ActTeam> actTeamList = actTeamDao.findCreateTeam(null, template, userId, customerId);
//		if (actTeamList == null || actTeamList.size() == 0) {
//			return new StatusObjDto<ActTeam>(true, null, 0, "");
//		}
		//获取组队中或者组队成功的队伍
//		for(ActTeam team : actTeamList) {
//			if (team.getStatus().compareTo(ActTeam.FORMING) == 0 || team.getStatus().compareTo(ActTeam.SUCCESS) == 0) {
//				actTeam = team;
//			}
//		}
		ActTeam actTeam  = actTeamDao.findMyCreateTeam(template, customerId);
		if (actTeam == null) {
			return new StatusObjDto<ActTeam>(true, null, 0, "");
		}
		//查询队伍成员
		List<ActTeamMember> teamMemberList = teamMemberDao.findByTeamSerial(actTeam.getTeamSerial(), actTeam.getCreateCustomerId());
//		TODO替换头像
		if (ObjectUtils.isEmptyOrNull(actTeam.getCreatePortrait())) {
			actTeam.setCreatePortrait(defaultHeadimg);
		}
		for (int i = 0; i < teamMemberList.size(); i++) {
			if(ObjectUtils.isEmptyOrNull(teamMemberList.get(i).getPortrait())) {
				teamMemberList.get(i).setPortrait(defaultHeadimg);
			}
			teamMemberList.get(i).setPosition(i);
		}
		actTeam.setMemberNameList(teamMemberList);
		if (actTeam.getCreateCustomerId().equals(customerId)) {
			actTeam.setIsTeamLead(ActTeam.IS_TEAM_LEAD);
		}
		return new StatusObjDto<ActTeam>(true, actTeam, 0, "");
	}
	
	
	
	@Override
	public StatusObjDto<ActTeam> findTeamBySerial(String teamSerial, String userId, String customerId, String openId) {
		if (ObjectUtils.isEmptyOrNull(teamSerial)) {
			throw ActivityBizException.ACTIVITY_PARAMS_NOTNULL; 
		}
		List<ActTeam> actTeamList = actTeamDao.findCreateTeam(teamSerial, null, null, null);
		if (actTeamList == null || actTeamList.size() == 0) {
			return new StatusObjDto<ActTeam>(true, null, 0, "");
		}
		//队伍已组队成功
		ActTeam actTeam  = actTeamList.get(0);
		List<ActTeamMember> teamMemberList = teamMemberDao.findByTeamSerial(actTeam.getTeamSerial(), actTeam.getCreateCustomerId());
//		TODO替换头像
		if (ObjectUtils.isEmptyOrNull(actTeam.getCreatePortrait())) {
			actTeam.setCreatePortrait(defaultHeadimg);
		}
		
		for (int i = 0; i < teamMemberList.size(); i++) {
			if(ObjectUtils.isEmptyOrNull(teamMemberList.get(i).getPortrait())) {
				teamMemberList.get(i).setPortrait(defaultHeadimg);
			}
			teamMemberList.get(i).setPosition(i);
			if (!ObjectUtils.isEmptyOrNull(teamMemberList.get(i).getOpenId()) && teamMemberList.get(i).getOpenId().equals(openId)) {
				actTeam.setHasLight(ActTeam.HAS_HELP);
			}
		}
		actTeam.setMemberNameList(teamMemberList);
		if (!ObjectUtils.isEmptyOrNull(actTeam.getCreateCustomerId()) && actTeam.getCreateCustomerId().equals(customerId)){
			actTeam.setIsTeamLead(ActTeam.IS_TEAM_LEAD);
		} else if (!ObjectUtils.isEmptyOrNull(actTeam.getCreateUserId()) && actTeam.getCreateUserId().equals(userId))  {
			actTeam.setIsTeamLead(ActTeam.IS_TEAM_LEAD);
		} else {
			actTeam.setIsTeamLead(ActTeam.NOT_TEAM_LEAD);
		}
		return new StatusObjDto<>(true, actTeam, 0, "");
	}
	
	@Override
	public StatusObjDto<Integer> dismissTeam(String userId, String customerId, String teamSerial) {
		if (ObjectUtils.isEmptyOrNull(teamSerial)) {
			return new StatusObjDto<Integer>(true, null, 0, "");
		}
		Integer count = actTeamDao.update(teamSerial, ActTeam.FAIL, customerId);
		return new StatusObjDto<Integer>(true, count, 0, "");
	}

	@Override
	public List<ActTeam> autoFormTeam(Integer maxCount) {
		ActTeam actTeam = new ActTeam();
		actTeam.setStatus(ActTeam.FORMING);
		List<ActTeam> teamList = actTeamDao.findList(actTeam, maxCount);
		if (teamList == null || teamList.size() == 0) {
			return teamList;
		}
		List<ActTeamMember> actTeamMembers = teamMemberDao.findAutoList(teamList, teamList.size());
		if (actTeamMembers == null || actTeamMembers.size() == 0) {
			return new ArrayList<>();
		}
		//查找已組隊的成員，進行組隊
		for(int i = 0; i < actTeamMembers.size(); i++) {
			teamList.get(i).setStatus(ActTeam.SUCCESS);
			actTeamMembers.get(i).setTeamSerial(teamList.get(i).getTeamSerial());
			actTeamMembers.get(i).setType(ActTeamMember.TYPE_AUTO);
			actTeamMembers.get(i).setCreateDate(new Date());
			actTeamDao.update(actTeamMembers.get(i).getTeamSerial(), ActTeam.SUCCESS, teamList.get(i).getCreateCustomerId());
			teamMemberDao.insert(actTeamMembers.get(i));
		}
		return teamList.subList(0, actTeamMembers.size());
	}
	

	
	private void checkCanCreateTeam(String template, String userId, String customerId) {
		//获取用户所在的所有队伍
		ActTeam team = actTeamDao.findMyCreateTeam(template, customerId);
		//获取组队中或者组队成功的队伍
		if (team != null) {
			throw BizException.NETWORK_ERROR;
		}
	}

	@Override
	@Transactional
	public StatusObjDto<ActTeam> lightTeam(String activityCode, String teamSerial, String userId,String nickName, String portrait, String customerId, String openId) {
		StatusDto isValidAct = isValid(activityCode);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<ActTeam>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		if (ObjectUtils.isEmptyOrNull(teamSerial)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("teamSerial");
		}
		ActTeam entry = new ActTeam();
		entry.setTeamSerial(teamSerial);
		entry.setStatus(ActTeam.FORMING);
		ActTeam team = actTeamDao.get(entry);
		if (team == null) {
			throw BizException.NETWORK_ERROR;
		}
		//查询队伍成员
		List<ActTeamMember> memberList = teamMemberDao.findByTeamSerial(teamSerial, team.getCreateCustomerId());
//		TODO替换头像n 
		if (ObjectUtils.isEmptyOrNull(team.getCreatePortrait())) {
			team.setCreatePortrait(defaultHeadimg);
		}
		for (int i = 0; i < memberList.size(); i++) {
			if(ObjectUtils.isEmptyOrNull(memberList.get(i).getPortrait())) {
				memberList.get(i).setPortrait(defaultHeadimg);
			}
			memberList.get(i).setPosition(i);
			if (userId.equals(memberList.get(i).getUserId())) {
				team.setHasLight(ActTeam.HAS_HELP);
			}
		}
		team.setMemberNameList(memberList);
		
		checkCanLight(memberList, userId, team.getCreateUserId());
		
		ActTeamMember member = ActTeamMember.createNewMember(teamSerial, customerId, userId, new Date(), ActTeamMember.TYPE_MANUAL);
		member.setOpenId(openId);
		teamMemberDao.insert(member);
		member.setPortrait(portrait);
		member.setNickName(nickName);
		member.setPosition(memberList.size());
		team.getMemberNameList().add(member);
		
		//更新队伍状态
		if (team.getMemberNameList().size() >= FINSECTION_CARD_TEAM_MEMBER) {
			team.setStatus(ActTeam.SUCCESS);
			actTeamDao.update(team);
		}
		if (team.getCreateCustomerId().equals(customerId)) {
			team.setIsTeamLead(ActTeam.IS_TEAM_LEAD);
		}
		return new StatusObjDto<>(true, team, 0, "");
	}
	
	@Override
	public StatusObjDto<Integer> lightCount(String activityCode, String template, String userId, String openId) {
		StatusDto isValidAct = isValid(activityCode);
		if(!isValidAct.isOk()) new StatusObjDto<Integer>(true, 0, 0, "");
		if (ObjectUtils.isEmptyOrNull(activityCode)) return new StatusObjDto<Integer>(true, 0, 0, "");
		if (ObjectUtils.isEmptyOrNull(openId)) return new StatusObjDto<Integer>(true, 0, 0, "");
		List<ActTeamMember> lightList = teamMemberDao.findByTemplateAndOpenId(template, openId);
		Integer lightCount = lightList == null ? 0 : lightList.size();
		return new StatusObjDto<Integer>(true, lightCount, 0, "");
	}
	
	/**
	 * 
	 * @param memberList
	 * @param openId
	 */
	private void checkCanLight(List<ActTeamMember> memberList, String userId, String createUserId) {
		if (!ObjectUtils.isEmptyOrNull(createUserId) && createUserId.equals(userId)) {
			throw ActivityBizException.FINSECTION_LIGHT_SELF;
		}
//		if (memberList.size() >= FINSECTION_CARD_TEAM_MEMBER) {
//			throw ActivityBizException.FINSECTION_CARD_HAS_LIGHT;
//		}
		for (ActTeamMember member : memberList) {
			if (!ObjectUtils.isEmptyOrNull(member.getUserId()) && member.getUserId().equals(userId)) {
				throw ActivityBizException.FINSECTION_DOUBLE_LIGHT;
			}
		}
	}
	
	@Override
	public StatusObjDto<StepActivtyOverview> stepOverView(String activity, String prizeTypeCode, String customerId) {
		if(ObjectUtils.isEmptyOrNull(activity)) {
			return new StatusObjDto<StepActivtyOverview>(false, 201, "activity参数不能为空"); 
		}
		Activity act = findActivity(activity);
		StatusDto isValidAct =isValid(act);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<StepActivtyOverview>(false, isValidAct.getCode(), isValidAct.getMsg()); 
		}
		
		ActActivityStepEnum stepEnum = ActActivityStepEnum.getStepEnumByCode(activity);
		int[] openHour = stepEnum.getOPEN_HOUR();
		int[] closeHour = stepEnum.getCLOSE_HOUR();
		 
		StepActivtyOverview overview = new StepActivtyOverview();
		overview.setIsOpen(ActActivityStepEnum.inOpenTime(stepEnum) ? StepActivtyOverview.OPEN : StepActivtyOverview.CLOSE);
		overview.setNextOpenTime(getNextOpenTime(act.getDateTo(), stepEnum));
		Date now = new Date();
		if (StepActivtyOverview.CLOSE.equals(overview.getIsOpen())) {
			overview.setEnoughCoupon(StepActivtyOverview.NOT_ENOUGH);
			if (overview.getNextOpenTime() != null) {
				overview.setCountToOpen(overview.getNextOpenTime().getTime() - now.getTime());
			}
		} else {
			Integer integer = ActActivityStepEnum.index(stepEnum);
			Date dayStart = DateUtils.getDayStart(new Date());
			Date updateTimeFrom = DateUtils.addHour(dayStart, openHour[integer]);
			Date updateTimeTo = DateUtils.addHour(dayStart, closeHour[integer]);
			overview.setCountToClose(updateTimeTo.getTime() - now.getTime());
			List<ActPrize> prizeList = actPrizeBiz.findPrize(activity, prizeTypeCode, null, null, null, null, ActPrize.STATUS_SEND, updateTimeFrom, updateTimeTo);
			if (stepEnum.getPrizeMaxCount().compareTo(prizeList.size()) > 0) {
				overview.setEnoughCoupon(StepActivtyOverview.ENOUGH);
			}
		}
		overview.setLeftTime(act.getDateTo().getTime() - new Date().getTime());
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			overview.setHasCoupon(StepActivtyOverview.NOT_HAS_COUPON);
		} else {
			List<ActPrize> myPrize = actPrizeBiz.findPrize(activity, customerId, null, null, prizeTypeCode, null, null);
			overview.setHasCoupon(myPrize.size() != 0 ? StepActivtyOverview.HAS_COUPON : StepActivtyOverview.NOT_HAS_COUPON);
		}
		return new StatusObjDto<StepActivtyOverview>(true, overview, 0, "");
	}
	
	private Date getNextOpenTime (Date finalDate, ActActivityStepEnum stepEnum) {
		Date nextOpen = ActActivityStepEnum.getNextOpenDate(stepEnum);
		if (finalDate.before(nextOpen)) {
			return null;
		}
		return nextOpen;
	}
}
