
package com.wlzq.activity.guess.service.app;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.wlzq.activity.guess.dto.*;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.guess.biz.GuesssBiz;
import com.wlzq.activity.guess.model.Guesss;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
/**
 * GuessService服务类
 * @author
 * @version 1.0
 */
@Service("activity.guess")
public class GuesssService extends BaseService{
    @Autowired
    private GuesssBiz guessBiz;

    @Signature(true)
    @MustLogin(true)
	public ResultDto overview(RequestParams params,AccTokenUser user) {
    	if(ObjectUtils.isEmptyOrNull(user.getMobile())) {
    		throw BizException.USER_NOT_BIND_MOBILE;
    	}
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
		StatusObjDto<GuesssInfoDto>  result = guessBiz.overview(user, activityCode);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
	public ResultDto betstatus(RequestParams params) {
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
		StatusObjDto<GuesssStatusDto>  result = guessBiz.betStatus(activityCode);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto guess(RequestParams params,AccTokenUser user) {
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
    	Integer direction = params.getInt("direction");
    	Integer point = params.getInt("point");
   		StatusDto result = guessBiz.guess(user.getUserId(), activityCode, direction, point);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
       	return new ResultDto(0,"");
   	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto guessrecord(RequestParams params,AccTokenUser user) {
    	int[] page = buildPage(params);
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
   		StatusObjDto<List<Guesss>> result = guessBiz.guessRecord(user.getUserId(),activityCode,page[0], page[1]);
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
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
    	Date rankDate = type != null && type.equals(2)?null:new Date();
    	int[] page = buildPage(params);
   		StatusObjDto<List<WinRanksDto>> result = guessBiz.winRanking(rankDate,activityCode,page[0], page[1]);
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
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
   		AchievementsDto achievement = guessBiz.achievement(user, activityCode);

    	return new ResultDto(0,BeanUtils.beanToMap(achievement),"");
   	}

    @Signature(true)
   	public ResultDto prizes(RequestParams params,AccTokenUser user) {

    	int[] page = buildPage(params);
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
   		StatusObjDto<List<GuesssPrizeDto>> result = guessBiz.prizes(activityCode, page[0], page[1]);

		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto userprizes(RequestParams params,AccTokenUser user) {
    	if(ObjectUtils.isEmptyOrNull(user.getMobile())) {
    		throw BizException.USER_NOT_BIND_MOBILE;
    	}

    	int[] page = buildPage(params);
    	String activityCode = ObjectUtils.isEmptyOrNull(params.getString("activityCode")) ? "ACTIVITY.GUESS" : params.getString("activityCode");
   		StatusObjDto<List<UserPrizeDto>> result = guessBiz.userPrizes(user.getUserId(),activityCode, page[0], page[1]);

		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

	/**
	 * 积分兑换奖品
	 */
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto pointprize(RequestParams params, AccTokenUser user, Customer customer) {
		/*活动编码*/
		String activityCode = params.getString("activityCode");
		/*奖品编码*/
		String prizeType = params.getString("prizeType");
		String userId = user.getUserId();
		String mobile = user.getMobile();
		String customerId = customer.getCustomerId();

		StatusObjDto<Object> result = guessBiz.pointPrize(activityCode, prizeType, userId, customerId, mobile);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), BeanUtils.beanToMap(result.getObj()), result.getMsg());
		}

		Map<String, Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		return new ResultDto(0, data, "");
	}

}
