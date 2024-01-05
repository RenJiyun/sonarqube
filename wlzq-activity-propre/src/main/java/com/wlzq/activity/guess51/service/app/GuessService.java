
package com.wlzq.activity.guess51.service.app;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.guess51.biz.GuessBiz;
import com.wlzq.activity.guess51.dto.AchievementDto;
import com.wlzq.activity.guess51.dto.GuessDto;
import com.wlzq.activity.guess51.dto.GuessInfoDto;
import com.wlzq.activity.guess51.dto.GuessPrizeDto;
import com.wlzq.activity.guess51.dto.GuessStatusDto;
import com.wlzq.activity.guess51.dto.WinRankDto;
import com.wlzq.activity.guess51.model.Guess;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
/**
 * GuessService服务类
 * @author 
 * @version 1.0
 */
@Service("activity.guess51")
public class GuessService extends BaseService{
    @Autowired
    private GuessBiz guessBiz;
	
    @Signature(true)
    @MustLogin(true)
	public ResultDto overview(RequestParams params,AccTokenUser user) {
    	if(ObjectUtils.isEmptyOrNull(user.getMobile())) {
    		throw BizException.USER_NOT_BIND_MOBILE;
    	}
		StatusObjDto<GuessInfoDto>  result = guessBiz.overview(user);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
	public ResultDto betstatus(RequestParams params) {
    	
		StatusObjDto<GuessStatusDto>  result = guessBiz.betStatus();
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
    
    @Signature(true)
    @MustLogin(true)
   	public ResultDto guess(RequestParams params,AccTokenUser user) {
    	Integer direction = params.getInt("direction");
    	Integer point = params.getInt("point");
   		StatusObjDto<GuessDto> result = guessBiz.guess(user.getUserId(), direction, point);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
       	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
   	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto guessrecord(RequestParams params,AccTokenUser user) {
    	int[] page = buildPage(params);
   		StatusObjDto<List<Guess>> result = guessBiz.guessRecord(user.getUserId(),page[0],page[1]);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}

		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto winranking(RequestParams params,AccTokenUser user) {
    	Integer type = params.getInt("type");
    	Date rankDate = type != null && type.equals(2)?null:new Date();
    	int[] page = buildPage(params);
   		StatusObjDto<List<WinRankDto>> result = guessBiz.winRanking(rankDate,page[0],page[1]);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}

		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto achievement(RequestParams params,AccTokenUser user) {
    	if(ObjectUtils.isEmptyOrNull(user.getMobile())) {
    		throw BizException.USER_NOT_BIND_MOBILE;
    	}
    	
   		AchievementDto achievement = guessBiz.achievement(user);
   		
    	return new ResultDto(0,BeanUtils.beanToMap(achievement),"");
   	}

    @Signature(true)
   	public ResultDto prizes(RequestParams params,AccTokenUser user) {
    	
    	int[] page = buildPage(params);
   		StatusObjDto<List<GuessPrizeDto>> result = guessBiz.prizes(page[0], page[1]);

		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

}
