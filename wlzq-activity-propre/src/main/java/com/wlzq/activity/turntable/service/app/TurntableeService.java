
package com.wlzq.activity.turntable.service.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.turntable.biz.BaseTurntableeBiz;
import com.wlzq.activity.turntable.biz.TurntableeBiz;
import com.wlzq.activity.turntable.dto.TurntableeHitDto;
import com.wlzq.activity.turntable.dto.TurntableePrizeDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
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
 * 大转盘服务类
 * @author 
 * @version 1.0
 */
@Service("activity.turntable")
public class TurntableeService extends BaseService{
	
    @Autowired
    private TurntableeBiz turntableBiz;
    @Autowired
    private BaseTurntableeBiz baseTurntableBiz;
	
    @Signature(true)
    @MustLogin(true)
	public ResultDto turn(RequestParams params,AccTokenUser user) {
    	if(user == null) {
    		throw BizException.NOT_LOGIN_ERROR;
    	}
    	
    	Long timestamp = params.getLong("timestamp");
		StatusObjDto<TurntableeHitDto> result = turntableBiz.turn(user.getUserId(),timestamp);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}

    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
    
    @Signature(true)
    @MustLogin(true)
	public ResultDto baseturn(RequestParams params,AccTokenUser user, Customer customer) {
    	if(user == null) {
    		throw BizException.NOT_LOGIN_ERROR;
    	}
    	String custoomerId = customer == null ? null : customer.getCustomerId();
    	Long timestamp = params.getLong("timestamp");
    	String activity = params.getString("activityCode");
		StatusObjDto<TurntableeHitDto> result = baseTurntableBiz.turn(user.getUserId(),  custoomerId, user.getMobile(), activity, timestamp);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}

    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
   	public ResultDto prizes(RequestParams params,AccTokenUser user) {
    	
    	int[] page = buildPage(params);
    	String activity = params.getString("activityCode");
   		StatusObjDto<List<TurntableePrizeDto>> result = turntableBiz.prizes(activity, page[0], page[1]);

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
    	String activity = params.getString("activityCode");
   		StatusObjDto<List<UserPrizeDto>> result = turntableBiz.userPrizes(activity,user.getUserId(), page[0], page[1]);

		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

    @Signature(true)
	public ResultDto hasnotuseprize(RequestParams params,AccTokenUser user) {
    	if(user == null) {
    		throw BizException.NOT_LOGIN_ERROR;
    	}
    	String activity = params.getString("activityCode");
		StatusObjDto<Integer> result = turntableBiz.findNotUsePrizeCount(user.getUserId(), activity);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Integer count = result.getObj();
		Integer status = count > 0?1:0;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("status", status);
		
    	return new ResultDto(0,data,"");
	}

    @Signature(true)
	public ResultDto share(RequestParams params,AccTokenUser user) {
    	if(user == null) {
    		throw BizException.NOT_LOGIN_ERROR;
    	}
    	String activity = params.getString("activityCode");
		StatusObjDto<Integer> result = turntableBiz.share(user.getUserId());
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("freeCount", result.getObj());
		
    	return new ResultDto(0,data,"");
	}
}
