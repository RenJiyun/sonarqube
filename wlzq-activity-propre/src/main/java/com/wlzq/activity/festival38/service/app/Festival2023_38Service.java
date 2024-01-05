package com.wlzq.activity.festival38.service.app;

import java.util.List;
import java.util.Map;

import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.biz.ActLotteryBiz;
import com.wlzq.activity.base.model.ActLotteryEnum;
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
@Service("activity.festival2023_38")
public class Festival2023_38Service extends BaseService{
	
	@Autowired
    private ActLotteryBiz lotteryBiz;
	
	@Signature(true)
	@MustLogin(true)
//	@CustomerMustLogin(true)
	public ResultDto lottery (RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = params.getString("activityCode");
		String userId = user == null?null:user.getUserId();
		String openId = user == null?null:user.getOpenid();
		String customerId = customer == null ? null : customer.getCustomerId();
		String mobile = user == null?null:user.getMobile();
		StatusObjDto<CouponRecieveStatusDto> result = lotteryBiz.lottery38(
				ActLotteryEnum.getActLotteryEnumList(activityCode, userId, customerId), activityCode, userId, openId, customerId, mobile, ActLotteryEnum.FINSECTION_THANKS);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
	
	@Signature(true)
	public ResultDto status(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String userId = user == null?null:user.getUserId();
		String mobile = user == null?null:user.getMobile();
		String openId = user == null?null:user.getOpenid();
		String customerId = customer == null?null:customer.getCustomerId();
    	StatusObjDto<List<CouponRecieveStatusDto>> result = lotteryBiz.getPrizeStatus(activityCode, userId, openId, customerId, mobile);
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
