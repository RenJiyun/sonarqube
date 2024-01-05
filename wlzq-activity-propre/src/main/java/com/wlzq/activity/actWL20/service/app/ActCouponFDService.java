package com.wlzq.activity.actWL20.service.app;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.actWL20.biz.ActCouponFDBiz;
import com.wlzq.activity.actWL20.dto.ActFDRecieveDto;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 万联20周年活动-【福袋领取】
 * @author jjw
 */
@Service("activity.actcouponfd")
public class ActCouponFDService extends BaseService{
	
	@Autowired
    private ActCouponFDBiz actCouponFDBiz;
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto recieve(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String userId = user == null?null:user.getUserId();
		String openId = user == null?null:user.getOpenid();
		String recommendCode = (String) params.get("recommendCode");
		
		StatusObjDto<CouponRecieveStatusDto> result = actCouponFDBiz.recieve(activityCode, userId, openId, customer, recommendCode);
		
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	ResultDto back = new ResultDto(0,null,"领取成功");
    	return back;
	}
	
	@Signature(true)
	public ResultDto findrecieves(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		
    	StatusObjDto<List<ActFDRecieveDto>> result = actCouponFDBiz.findRecieves(activityCode,customer.getCustomerId());
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("total", result.getObj().size());
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}
}
