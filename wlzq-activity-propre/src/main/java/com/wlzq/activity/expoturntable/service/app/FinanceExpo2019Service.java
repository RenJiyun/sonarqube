package com.wlzq.activity.expoturntable.service.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.expoturntable.biz.FinanceExpo2019Biz;
import com.wlzq.activity.expoturntable.biz.FinanceExpo2019CouponBiz;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

@Service("activity.financeexpo2019")
public class FinanceExpo2019Service extends BaseService {
	
	/** 默认返回的消息条数 */
	private static Integer MSG_LENGTH = 10;
	
	@Autowired
	private FinanceExpo2019Biz financeExpo2019Biz;
	
	@Autowired
	private FinanceExpo2019CouponBiz financeExpo2019CouponBiz;

	@Signature(true)
	@MustLogin(true)
	public ResultDto signin(RequestParams params, AccTokenUser user) {
		StatusObjDto<Map<String, Object>> result = financeExpo2019Biz.signIn(user);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto signinlist(RequestParams params) {
		Integer maxOrder = params.getInt("maxOrder");
		Integer maxLength = params.getInt("length");
		if (maxLength == null) {
			maxLength = MSG_LENGTH;
		}
		StatusObjDto<List<Map<String, Object>>> result = financeExpo2019Biz.signinList(maxOrder, maxLength);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", result.getObj() == null? 0 : result.getObj().size());
		map.put("info", result.getObj());
		
		if (result.getObj() == null) {
			map.put("maxSigninCode", "0");
		} else {
			if (result.getObj().size() == 0) {
				map.put("maxSigninCode", String.valueOf(maxOrder));
			} else {
				map.put("maxSigninCode", result.getObj().get(result.getObj().size() - 1).get("signInCode"));
			}
		}
		
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(map), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto gameswitch(RequestParams params) {
		Integer switchType = params.getInt("switchType");
		Integer scene = params.getInt("scene");
		StatusObjDto<Map<String, Object>> result = financeExpo2019Biz.gameSwitch(switchType, scene);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto gameshake(RequestParams params, AccTokenUser user) {
		Integer scene = params.getInt("scene");
		String counts = params.getString("counts");
		Integer maxLength = params.getInt("maxLength");
		StatusObjDto<Map<String, Object>> result = financeExpo2019Biz.gameShake(user, scene, counts, maxLength);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto shaketrend(RequestParams params) {
		Integer scene = params.getInt("scene");
		Integer length = params.getInt("length");
		StatusObjDto<Map<String, Object>> result = financeExpo2019Biz.shakeTrend(scene, length);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto shakerank(RequestParams params) {
		Integer scene = params.getInt("scene");
		StatusObjDto<Map<String, Object>> result = financeExpo2019Biz.shakeRank(scene);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto personelrecord(RequestParams params, AccTokenUser user) {
		Integer scene = params.getInt("scene");
		StatusObjDto<Map<String, Object>> result = financeExpo2019Biz.personelRecord(scene, user);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	public ResultDto playerinfo(RequestParams params) {
		String playerIds = params.getString("playerIds");
		StatusObjDto<Map<String, Object>> result = financeExpo2019Biz.playerInfo(playerIds);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto mycoupon(RequestParams params, AccTokenUser user) {
		StatusObjDto<Map<String, Object>> result = financeExpo2019CouponBiz.myCoupon(user);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(false)
	public ResultDto getcoupon(RequestParams params, AccTokenUser user, Customer customer) {
		StatusObjDto<Map<String, Object>> result = financeExpo2019CouponBiz.getCoupon(user, customer);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
	
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto usecoupon(RequestParams params, AccTokenUser user, Customer customer) {
		StatusObjDto<Map<String, Object>> result = financeExpo2019CouponBiz.useCoupon(user, customer);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
	}
}
