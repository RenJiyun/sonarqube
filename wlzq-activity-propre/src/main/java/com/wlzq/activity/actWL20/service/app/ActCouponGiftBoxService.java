package com.wlzq.activity.actWL20.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.actWL20.biz.ActCouponGiftBoxBiz;
import com.wlzq.activity.actWL20.model.ActGiftBox;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 万联20周年活动-【拆神秘礼盒】
 * @author jjw
 */
@Service("activity.actcoupongiftbox")
public class ActCouponGiftBoxService extends BaseService{
	
	@Autowired
    private ActCouponGiftBoxBiz actCouponGiftBoxBiz;
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto recieve(RequestParams params, AccTokenUser user,Customer customer) {
		String recommendCode = (String) params.get("recommendCode");
		String shareCode = (String) params.get("shareCode");
		
		StatusObjDto<CouponRecieveStatusDto> result = actCouponGiftBoxBiz.recieve(user, customer, recommendCode, shareCode);
		
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"领取成功");
    	return back;
	}
	
	@Signature(true)
	public ResultDto findrecieves(RequestParams params, AccTokenUser user,Customer customer) {	
		String shareCode = (String) params.get("shareCode");
    	StatusObjDto<List<CouponRecieveStatusDto>> result = actCouponGiftBoxBiz.findRecieves(user, customer,shareCode);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("total", result.getObj().size());
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}
	
	@Signature(true)
	public ResultDto mobilehascustomer(RequestParams params, AccTokenUser user,Customer customer) {	
		String mobile = (String) params.get("mobile");
		ResultDto result = actCouponGiftBoxBiz.mobilehascustomer(user, customer,mobile);
    	
    	return result;
	}

	@Signature(true)
	public ResultDto findgiftboxes(RequestParams params){
		String customerId = (String) params.get("customerId");
		String activityCode = (String) params.get("activityCode");
		ActGiftBox actGiftBox = new ActGiftBox().setActivityCode(activityCode).setMobile(customerId);

		List<ActGiftBox> result = actCouponGiftBoxBiz.findGiftBoxes(actGiftBox);
		Map<String,Object> data = Maps.newHashMap();
		if (result.size() > 0) {
			data.put("popupStatus", CodeConstant.CODE_YES);
		}
		return new ResultDto(0,data,"");
	}


}
