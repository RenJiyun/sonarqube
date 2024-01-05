package com.wlzq.activity.festival38.service.app;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.festival38.biz.Festival38Biz;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 2022三八女神节
 * @author jjw
 */
@Service("activity.festival38")
public class Festival38Service extends BaseService{
	
	@Autowired
    private Festival38Biz festival38Biz;
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto recieve(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String openId = user == null?null:user.getOpenid();
		String recommendCode = (String) params.get("recommendCode");
		
		StatusObjDto<CouponRecieveStatusDto> result = festival38Biz.recieve(activityCode, user, openId, customer,recommendCode);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	
    	ResultDto back = new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
    	return back;
	}
	
	@Signature(true)
	public ResultDto status(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String customerId = customer == null?null:customer.getCustomerId();
		String recommendCode = (String) params.get("recommendCode");
    	StatusObjDto<List<CouponRecieveStatusDto>> result = festival38Biz.status(activityCode, user, customerId, recommendCode);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("total",result.getObj().size());
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}
	
	@Signature(false)
	public ResultDto checkmobile(RequestParams params) {
		String mobile = (String) params.get("mobile");
		StatusObjDto<String> result = festival38Biz.checkmobile(mobile);
		if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		Map<String,Object> data = Maps.newHashMap();
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}
}
