package com.wlzq.activity.actWL20.service.app;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.actWL20.biz.ActCouponZBBiz;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 万联20周年活动-直播领券
 * @author jjw
 */
@Service("activity.actcouponzb")
public class ActCouponZBService extends BaseService{
	
	@Autowired
    private ActCouponZBBiz actCouponZBBiz;
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto recieve(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String userId = user == null?null:user.getUserId();
		String openId = user == null?null:user.getOpenid();
		String recommendCode = (String) params.get("recommendCode");
		
		StatusObjDto<CouponRecieveStatusDto> result = actCouponZBBiz.recieve(activityCode, userId, openId, customer,recommendCode);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	
    	ResultDto back = new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
    	return back;
	}
	
	@Signature(true)
	public ResultDto status(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String customerId = customer == null?"0":customer.getCustomerId();
		String recommendCode = (String) params.get("recommendCode");
    	StatusObjDto<List<CouponRecieveStatusDto>> result = actCouponZBBiz.status(activityCode, user.getUserId(), customerId, recommendCode);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("total",result.getObj().size());
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}
}
