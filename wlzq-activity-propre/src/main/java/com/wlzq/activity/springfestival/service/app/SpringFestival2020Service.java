package com.wlzq.activity.springfestival.service.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.springfestival.biz.SpringFestival2020Biz;
import com.wlzq.activity.springfestival.dto.LotteryPreviewDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 
 * @author zhaozx
 * @version 2020-01-02
 */
@Service("activity.springfestival2020")
public class SpringFestival2020Service {

	@Autowired
	private SpringFestival2020Biz springFestival2020Biz;
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto lotteypreview (RequestParams params, AccTokenUser user,Customer customer) {
		String userId = user.getUserId();
		String openId = user.getOpenid();
		String customerId = customer == null ? null : customer.getCustomerId();
		StatusObjDto<LotteryPreviewDto> result = springFestival2020Biz.lotteryPreview(userId, openId, customerId);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto lottery (RequestParams params, AccTokenUser user,Customer customer) {
		String userId = user.getUserId();
		String openId = user.getOpenid();
		String customerId = customer == null ? null : customer.getCustomerId();
		String mobile = user.getMobile();
		StatusObjDto<LotteryDto> result = springFestival2020Biz.lottery(userId, openId, customerId, mobile);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
}
