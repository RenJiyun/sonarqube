package com.wlzq.activity.quant.service.app;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.quant.biz.ActQuantBiz;
import com.wlzq.activity.quant.biz.impl.ActQuantBizImpl;
import com.wlzq.activity.quant.model.ActQuantTeam;
import com.wlzq.activity.quant.model.ActQuantTeamStratDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.Page;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

/**
 * CommonService服务类
 * @author 
 * @version 1.0
 */
@Service("activity.quant")
public class ActQuantService extends BaseService {
	
	@Autowired
	private ActQuantBiz actQuantBiz;
	
	@Signature(true)
	public ResultDto strategyevaluation(RequestParams params) {
		String busiparams =params.getString("busiparams");
		StatusObjDto<Integer> dto = actQuantBiz.strategyEvaluation(busiparams);
		if (!StatusDto.SUCCESS.equals(dto.getCode())) {
			return new ResultDto(dto.getCode(), dto.getMsg());
		}
		Integer count = dto.getObj();
		Map<String, Object> data = Maps.newHashMap();
		data.put("count", count);
    	return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	public ResultDto findteams(RequestParams params, AccTokenUser user) {
		String teamId = params.getString("teamId");
		String leader = params.getString("leader");
		Integer accountType = params.getInt("accountType");
		Integer orderType = params.getInt("orderType");
		Page page = buildPageNew(params);
		StatusObjDto<List<ActQuantTeamStratDto>> dto = actQuantBiz.findteams(teamId, leader, orderType, accountType, page);
		Map<String, Object> data = Maps.newHashMap();
		data.put("total", dto.getObj().size());
		data.put("info", dto.getObj());
		return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	public ResultDto hotteams(RequestParams params)  {
		String teamIdOrLeader = params.getString("teamIdOrLeader");
		Page page = buildPageNew(params);
		StatusObjDto<List<ActQuantTeam>> dto = actQuantBiz.hotteams(teamIdOrLeader, page);
		Map<String, Object> data = Maps.newHashMap();
		data.put("total", dto.getObj().size());
		data.put("info", dto.getObj());
		
		String voteDateStart = AppConfigUtils.get(ActQuantBizImpl.ACT_QUANT_VOTE_DATESTART);
		String voteDateEnd = AppConfigUtils.get(ActQuantBizImpl.ACT_QUANT_VOTE_DATEEND);
		if (ObjectUtils.isEmptyOrNull(voteDateStart)) {
			voteDateStart = "2020-11-13";
		}
		if (ObjectUtils.isEmptyOrNull(voteDateEnd)) {
			voteDateEnd = "2020-12-13";
		}
		
		
		data.put("voteDateStart", DateUtils.getDayStart(DateUtils.parseDate(voteDateStart, "yyyy-MM-dd")).getTime());
		data.put("voteDateEnd", DateUtils.getDayEnd(DateUtils.parseDate(voteDateEnd, "yyyy-MM-dd")).getTime());
		return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	public ResultDto votestatus(RequestParams params, AccTokenUser user) {
		String mobile = user == null ? null : user.getMobile();
		StatusObjDto<Integer> dto = actQuantBiz.voteStatus(mobile);
		Map<String, Object> data = Maps.newHashMap();
		data.put("count", dto.getObj());
		return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	@MustLogin
	public ResultDto vote(RequestParams params, AccTokenUser user) {
		String mobile = user == null ? null : user.getMobile();
		String openId = user == null ? null : user.getOpenid();
		String userId = user == null ? null : user.getUserId();
		String teamId = params.getString("teamId");
		StatusObjDto<Integer> dto = actQuantBiz.vote(mobile, openId, userId, teamId);
		Map<String, Object> data = Maps.newHashMap();
		data.put("count", dto.getObj());
		return new ResultDto(0, data, "");
	}
}
