package com.wlzq.activity.brokeragemeeting.service.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.brokeragemeeting.biz.BrokerageMeetingBiz;
import com.wlzq.activity.brokeragemeeting.dto.BrokerageMeetingVoteResultDto;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingPersonnel;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingVote;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingWord;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

@Service("activity.brokeragemeeting")
public class BrokerageMeetingService extends BaseService {
	
	/** 默认返回的消息条数 */
	private static Integer MSG_LENGTH = 5;
	
	/** 默认返回的消息条数 */
	private static Integer RESULT_LEN = 10;

	@Autowired
	private BrokerageMeetingBiz brokerageMeetingBiz;
	
	@Signature(true)
	public ResultDto signin(RequestParams params, AccTokenUser user) {
		StatusObjDto<Map<String, Object> > result = brokerageMeetingBiz.signIn(user);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto unveiling(RequestParams params, AccTokenUser user) {
		String name = params.getString("name");
		StatusObjDto<Map<String, Object>> result = brokerageMeetingBiz.unveiling(user, name);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto isunveildone(RequestParams params) {
		StatusObjDto<Map<String, Object>> result = brokerageMeetingBiz.isUnveilDone();
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto signinlist(RequestParams params) {
		Integer maxOrder = params.getInt("maxOrder");
		Integer maxLength = params.getInt("length");
		if (maxLength == null) {
			maxLength = MSG_LENGTH;
		}
		StatusObjDto<List<ActBrokeragemeetingPersonnel>> result = brokerageMeetingBiz.signinList(maxOrder, maxLength);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", result.getObj() == null? 0 : result.getObj().size());
		map.put("info", result.getObj());
		
		if (result.getObj() == null) {
			map.put("maxSigninCode", "0");
		} else {
			if (result.getObj().size() == 0) {
				map.put("maxSigninCode", String.valueOf(maxOrder));
			} else {
				map.put("maxSigninCode", result.getObj().get(result.getObj().size() - 1).getSignOrder());
			}
		}
		
		
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(map), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto gameswitch(RequestParams params) {
		Integer switchType = params.getInt("switchType");
		StatusObjDto<Map<String, Object>> result = brokerageMeetingBiz.gameSwitch(switchType);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto gamevote(RequestParams params, AccTokenUser user) {
		String votewords = params.getString("votewords");
		String voteCounts = params.getString("voteCounts");
		StatusObjDto<Map<String, Object>> result = brokerageMeetingBiz.gameVote(user, votewords, voteCounts);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto voteresult(RequestParams params) {
		Integer maxLength = params.getInt("length");
		if (maxLength == null) {
			maxLength = RESULT_LEN;
		}
		StatusObjDto<BrokerageMeetingVoteResultDto> result = brokerageMeetingBiz.voteResult(maxLength);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("personNum", 0);
		map.put("total", 0);
		map.put("info", null);
		if (result != null) {
			BrokerageMeetingVoteResultDto dto = result.getObj();
			if (dto != null) {
				List<ActBrokeragemeetingVote> list = dto.getList();
				map.put("personNum", dto.getPersonNum());
				map.put("total", list == null ? 0 : list.size());
				map.put("info", list);
			}
		}
		
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(map), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto votespeed(RequestParams params) {
		Integer maxLength = params.getInt("length");
		if (maxLength == null) {
			maxLength = RESULT_LEN;
		}
		StatusObjDto<List<Map<String, Object>>> result = brokerageMeetingBiz.voteSpeed(maxLength);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", result.getObj() == null ? 0 : result.getObj().size());
		map.put("info", result.getObj());
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(map), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto wordlist(RequestParams params) {
		StatusObjDto<List<ActBrokeragemeetingWord>> result = brokerageMeetingBiz.wordList();
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", result.getObj() == null ? 0 : result.getObj().size());
		map.put("info", result.getObj());
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(map), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto personspeed(RequestParams params, AccTokenUser user) {
		StatusObjDto<Map<String, Object>> result = brokerageMeetingBiz.personSpeed(user);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
}
