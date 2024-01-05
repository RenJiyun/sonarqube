package com.wlzq.activity.quant.biz.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.quant.biz.ActQuantBiz;
import com.wlzq.activity.quant.dao.ActQuantStratAvalDao;
import com.wlzq.activity.quant.dao.ActQuantTeamDao;
import com.wlzq.activity.quant.dao.ActQuantTeamLikeDao;
import com.wlzq.activity.quant.dao.ActQuantTeammateDao;
import com.wlzq.activity.quant.model.ActQuantStratAval;
import com.wlzq.activity.quant.model.ActQuantTeam;
import com.wlzq.activity.quant.model.ActQuantTeamLike;
import com.wlzq.activity.quant.model.ActQuantTeamStratDto;
import com.wlzq.activity.quant.model.ActQuantTeammate;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.Page;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

@Service
public class ActQuantBizImpl implements ActQuantBiz {

	@Autowired
	private ActQuantStratAvalDao stratAvalDao;
	@Autowired
	private ActQuantTeamDao teamDao;
	@Autowired
	private ActQuantTeamLikeDao teamLikeDao;
	@Autowired
	private ActQuantTeammateDao teammateDao;
	
	private static final String ACT_QUANT_VOTE_PERSON_LIMIT = "act.quant.vote.person.limit";
	private static final String ACT_QUANT_VOTE_TEAM_LIMIT = "act.quant.vote.team.limit";
	private static final String ACT_QUANT_BACKTRACE_DATESTART = "act.quant.backtrace.datestart";
	private static final String ACT_QUANT_BACKTRACE_DATEEND = "act.quant.backtrace.dateend";
	public static final String ACT_QUANT_VOTE_DATESTART = "act.quant.vote.datestart";
	public static final String ACT_QUANT_VOTE_DATEEND = "act.quant.vote.dateend";
	
	@Override
	@Transactional
	public StatusObjDto<Integer> strategyEvaluation(String busiparams) {
		if (ObjectUtils.isEmptyOrNull(busiparams)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("busiparams");
		}
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = JsonUtils.jsonToList(busiparams);
		Date now = new Date();
		int count = 0;
		for (Map<String, Object> each : list) {
			ActQuantStratAval actQuantStratAval = BeanUtils.mapToBean(each, ActQuantStratAval.class);
			actQuantStratAval.setCreateTime(now);
			@SuppressWarnings("unchecked")
			Map<String, String> map = JsonUtils.json2Map(actQuantStratAval.getEvaluation());
			String rate = map.get("rate");
			String withdraw = map.get("withdraw");
			String sharp = map.get("sharp");
			String runTime = actQuantStratAval.getRunTime();
			if (ObjectUtils.isNotEmptyOrNull(runTime)) {
				actQuantStratAval.setRunTimeD(new Date(Long.valueOf(runTime)));
			}
			actQuantStratAval.setRate(rate);
			actQuantStratAval.setWithdraw(withdraw);
			actQuantStratAval.setSharp(sharp);
			actQuantStratAval.setScore(rate);
			actQuantStratAval.setIsDeleted(CodeConstant.CODE_NO);
			stratAvalDao.insert(actQuantStratAval);
			++count;
		}
		return new StatusObjDto<>(true, count, StatusDto.SUCCESS, "");
	}

	@Override
	public StatusObjDto<List<ActQuantTeamStratDto>> findteams(String teamId, String leader, Integer orderType, Integer accountType, Page page) {
		ActQuantTeam teamEntity = new ActQuantTeam();
		teamEntity.setTeamId(teamId);
		teamEntity.setLeader(leader);

		String backtraceDateStart = AppConfigUtils.get(ACT_QUANT_BACKTRACE_DATESTART);
		/**默认 2020-11-11**/
		if (ObjectUtils.isEmptyOrNull(backtraceDateStart)) {
			backtraceDateStart = "2020-11-11";
		}
		String backtraceDateEnd = AppConfigUtils.get(ACT_QUANT_BACKTRACE_DATEEND);
		/**默认 2020-12-10**/
		if (ObjectUtils.isEmptyOrNull(backtraceDateEnd)) {
			backtraceDateEnd = "2020-12-10";
		}
		
		Date now = new Date();
		String dateEndStr = backtraceDateEnd;
		/**若当前时间小于2020-12-12，则需对回测区间做处理**/
		if (now.before(DateUtils.addDay(DateUtils.parseDate(backtraceDateEnd, "yyyy-MM-dd"), 2))) {
			int dateGap = 2;
			/**周一或周二，统计为上周五 **/
			int weekOfDate = DateUtils.getIntWeekOfDate(now);
			if (weekOfDate == 1 || weekOfDate == 2) { 
				dateGap += weekOfDate;
			}
			dateEndStr = DateUtils.formate(DateUtils.addDay(now, -dateGap), "yyyy-MM-dd");
		}
		
		/**设置统计时间为回测时间右值当天15点，用于显示**/
		Date statTime = DateUtils.addHour(DateUtils.parseDate(dateEndStr, "yyyy-MM-dd"), 15);
		orderType = ActQuantTeamStratDto.ORDER_BY_SCORE.equals(orderType) || ActQuantTeamStratDto.ORDER_BY_VOTE.equals(orderType) ? orderType : ActQuantTeamStratDto.ORDER_BY_SCORE;
		List<ActQuantTeamStratDto> list = teamDao.findTeamStratDto(teamId, leader, orderType, accountType, backtraceDateStart, dateEndStr, page);
		if (list.isEmpty()) {
			throw ActivityBizException.ACT_TEAM_NOT_FOUND;
		}
		for (ActQuantTeamStratDto each : list) {
			String evaluation = each.getEvaluation();
			each.setStatTime(statTime);
			if (ObjectUtils.isNotEmptyOrNull(evaluation)) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = JsonUtils.json2Map(evaluation);
				each.setRate(map.get("rate"));
				each.setWithdraw(map.get("withdraw"));
				each.setSharp(map.get("sharp"));
			} else {
				each.setStrategyName("-");
				each.setRate("-");
				each.setWithdraw("-");
				each.setSharp("-");
			}
			String leaderName = each.getLeader();
			ActQuantTeammate teammate = new ActQuantTeammate();
			teammate.setTeamId(each.getTeamId());
			List<ActQuantTeammate> teammates = teammateDao.findList(teammate);
			List<String> nameList = teammates.stream().filter(e -> ObjectUtils.isNotEmptyOrNull(e.getName()) && leaderName.equals(e.getName()))
					.map(ActQuantTeammate :: getName).collect(Collectors.toList());
			nameList.add(0, leaderName);
			
			each.setTeammateList(nameList);
			each.setTeammates(StringUtils.join(nameList, ","));
		}
		return new StatusObjDto<>(true, list, StatusDto.SUCCESS, "");
	}

	@Override
	public StatusObjDto<Integer> voteStatus(String mobile) {
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<Integer>(true, 0, StatusDto.SUCCESS, "");
		}
		Date now = new Date();
		Date dateFrom = DateUtils.getDayStart(now);
		Date dateTo = DateUtils.getDayEnd(now);
		ActQuantTeamLike like = new ActQuantTeamLike();
		like.setMobile(mobile);
		like.setDateFrom(dateFrom);
		like.setDateTo(dateTo);
		List<ActQuantTeamLike> list = teamLikeDao.findList(like);
		return new StatusObjDto<Integer>(true, list.size(), StatusDto.SUCCESS, "");
	}

	@Override
	public StatusObjDto<Integer> vote(String mobile, String openId, String userId, String teamId) {
		String voteDateStart = AppConfigUtils.get(ACT_QUANT_VOTE_DATESTART);
		String voteDateEnd = AppConfigUtils.get(ACT_QUANT_VOTE_DATEEND);
		if (ObjectUtils.isEmptyOrNull(voteDateStart)) {
			voteDateStart = "2020-11-13";
		}
		if (ObjectUtils.isEmptyOrNull(voteDateEnd)) {
			voteDateEnd = "2020-12-13";
		}
		Date now = new Date();
		if (now.before(DateUtils.getDayStart(DateUtils.parseDate(voteDateStart, "yyyy-MM-dd")))
				|| now.after(DateUtils.getDayEnd(DateUtils.parseDate(voteDateEnd, "yyyy-MM-dd")))) {
			throw ActivityBizException.ACT_NOT_VOTE_TIME;
		}
		
		
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			throw BizException.USER_NOT_BIND_MOBILE;
		}
		if (ObjectUtils.isEmptyOrNull(teamId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("teamId");
		}
		ActQuantTeam team = new ActQuantTeam();
		team.setTeamId(teamId);
		List<ActQuantTeam> teams = teamDao.findList(team);
		if (teams.isEmpty() || teams.size() > 1) {
			throw BizException.COMMON_PARAMS_IS_ILLICIT.format("teamId");
		}
		
		Integer personLimit = AppConfigUtils.getInt(ACT_QUANT_VOTE_PERSON_LIMIT);
		Integer teamLimit = AppConfigUtils.getInt(ACT_QUANT_VOTE_TEAM_LIMIT);
		personLimit = personLimit == null ? 5 : personLimit;
		teamLimit = teamLimit == null ? 20 : teamLimit;
		Date dateFrom = DateUtils.getDayStart(now);
		Date dateTo = DateUtils.getDayEnd(now);
		ActQuantTeamLike like = new ActQuantTeamLike();
		like.setMobile(mobile);
		like.setDateFrom(dateFrom);
		like.setDateTo(dateTo);
		/**检查个人投票是否超当天上限**/
		List<ActQuantTeamLike> list = teamLikeDao.findList(like);
		if (list.size() >= personLimit) {
			throw ActivityBizException.ACT_DAILY_VOTE_LIMIT;
		}
		like.setTeamId(teamId);
//		like.setMobile(null);
//		/**检查队伍投票是否超当天上限**/
//		List<ActQuantTeamLike> teamDayTotal = teamLikeDao.findList(like);
//		if (teamDayTotal.size() >= teamLimit) {
//			throw ActivityBizException.ACT_DAILY_VOTE_LIMIT;
//		}
//		like.setMobile(mobile);
		like.setUserId(userId);
		like.setOpenId(openId);
		like.setCreateTime(now);
		like.setLikeTime(now);
		like.setIsDeleted(CodeConstant.CODE_NO);
		teamLikeDao.insert(like);
		return new StatusObjDto<Integer>(true, list.size() + 1, StatusDto.SUCCESS, "");
	}

	@Override
	public StatusObjDto<List<ActQuantTeam>> hotteams(String teamIdOrLeader, Page page) {
		List<ActQuantTeam> list = teamDao.findHotteams(teamIdOrLeader, page);
		for (ActQuantTeam each : list) {
			String leaderName = each.getLeader();
			ActQuantTeammate teammate = new ActQuantTeammate();
			teammate.setTeamId(each.getTeamId());
			List<ActQuantTeammate> teammates = teammateDao.findList(teammate);
			List<String> nameList = teammates.stream().filter(e -> ObjectUtils.isNotEmptyOrNull(e.getName()) && !leaderName.equals(e.getName()))
					.map(ActQuantTeammate :: getName).collect(Collectors.toList());
			nameList.add(0, leaderName);
			
			each.setTeammateList(nameList);
			each.setTeammates(StringUtils.join(nameList, ","));
		}
		return new StatusObjDto<List<ActQuantTeam>>(true, list, StatusDto.SUCCESS, "");
	}
	
	

}
