
package com.wlzq.activity.checkin.service.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.checkin.biz.CheckInBiz;
import com.wlzq.activity.checkin.dto.CheckInDto;
import com.wlzq.activity.checkin.dto.CheckInPrizeDto;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
/**
 * KLineService服务类
 * @author 
 * @version 1.0
 */
@Service("activity.checkin")
public class CheckInService{
    @Autowired
    private CheckInBiz checkInBiz;

    @Signature(true)
    @MustLogin(true)
   	public ResultDto checkin(RequestParams params,AccTokenUser user) {
   		if(ObjectUtils.isEmptyOrNull(user.getOpenid())) {
   			return new ResultDto(CodeConstant.NOT_LOGIN,"微信未登录");
   		}
   		Integer type = params.getInt("type");
   		String fillDate = params.getString("fillDate");
   		StatusObjDto<CheckInPrizeDto> result = null;
   		result = checkInBiz.checkIn(user.getUserId(),user.getOpenid(),type,fillDate);
   		
   		if(!result.isOk()) {
   			return new ResultDto(result.getCode(),result.getMsg());
   		}
       	
       	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
   	}
	
    @Signature(true)
    @MustLogin(true)
	public ResultDto status(RequestParams params,AccTokenUser user) {
   		if(ObjectUtils.isEmptyOrNull(user.getOpenid())) {
   			return new ResultDto(CodeConstant.NOT_LOGIN,"微信未登录");
   		}
		StatusObjDto<CheckInDto> result = null;
		result = checkInBiz.status(user.getOpenid());
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
    
    @Signature(true)
    @MustLogin(true)
   	public ResultDto getopportunity(RequestParams params,AccTokenUser user) {
   		if(ObjectUtils.isEmptyOrNull(user.getOpenid())) {
   			return new ResultDto(CodeConstant.NOT_LOGIN,"微信未登录");
   		}
   		StatusObjDto<Integer> result = null;
   		result = checkInBiz.getOpportunity(user.getUserId(),user.getOpenid());
   		if(!result.isOk()) {
   			return new ResultDto(result.getCode(),result.getMsg());
   		}
       	Map<String,Object> data = new HashMap<String,Object>();
       	data.put("total", result.getObj());
       	return new ResultDto(0,data,"");
   	}
    
    @Signature(true)
    @MustLogin(true)
   	public ResultDto prizeinfo(RequestParams params,AccTokenUser user) {
   		if(ObjectUtils.isEmptyOrNull(user.getOpenid())) {
   			return new ResultDto(CodeConstant.NOT_LOGIN,"微信未登录");
   		}
   		StatusObjDto<List<CheckInPrizeDto>> result = null;
   		result = checkInBiz.getPrize(user.getOpenid());
   		
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
   	public ResultDto hascheckin(RequestParams params,AccTokenUser user) {
   		if(ObjectUtils.isEmptyOrNull(user.getOpenid())) {
   			return new ResultDto(CodeConstant.NOT_LOGIN,"微信未登录");
   		}
   		StatusObjDto<Integer> result = checkInBiz.hasCheckIn(user.getOpenid(), null);
   		
   		if(!result.isOk()) {
   			return new ResultDto(result.getCode(),result.getMsg());
   		}
       	Map<String,Object> data = new HashMap<String,Object>();
       	data.put("status", result.getObj());
       	return new ResultDto(0,data,"");
   	}
}
