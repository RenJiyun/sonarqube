
package com.wlzq.activity.stockcourse.service.app;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dto.AcReceivePriceVO;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.stockcourse.biz.StockCourseBiz;
import com.wlzq.activity.stockcourse.model.StockCourseUser;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;

/**
 * StockCourseService服务类
 * @author zjt
 * @version 1.0
 */
@Service("activity.stockcourse")
public class StockCourseService extends BaseService{
	
    @Autowired
    private StockCourseBiz stockCourseBiz;
    @Autowired
    private CouponCommonReceiveBiz couponRecieveBiz;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;
	
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(false)
	public ResultDto registration(RequestParams params, AccTokenUser user, Customer customer) {
		StatusDto result = activityBaseBiz.isValid(StockCourseUser.ACT_STOCK_COURSE);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		String mobile = null;
		if(ObjectUtils.isNotEmptyOrNull(user)) {
			mobile = user.getMobile();
    	}
		if(StringUtils.isEmpty(mobile)) {
			mobile = params.getString("mobile");
		}
		Integer type = params.getInt("type");
		String openId = user.getThirdUid();
		String wechatName = user.getNickName();
		Integer courseNumber = params.getInt("courseNumber");
		Integer courseNumberStatus = params.getInt("courseNumberStatus");
		Integer learnedCourseNumber = params.getInt("learnedCourseNumber");
		Integer watchedLiveStream = params.getInt("watchedLiveStream");
		String name = StringUtils.isNotBlank((String)params.getString("name")) ? StringEscapeUtils.unescapeHtml4(params.getString("name").trim()) : "";
		String receivingAddress =StringUtils.isNotBlank((String)params.getString("receivingAddress")) ? StringEscapeUtils.unescapeHtml4(params.getString("receivingAddress").trim()) : "";
		String receivingMobile = params.getString("receivingMobile");
		String customerId = null;
		Date openDate = null;
		if(ObjectUtils.isNotEmptyOrNull(customer)) {
			customerId = customer.getCustomerId();
			openDate = customer.getOpenDate();
    	}
		
		StockCourseUser stockCourseUser  = new StockCourseUser();
		stockCourseUser.setMobile(mobile);
		stockCourseUser.setOpenId(openId);
		stockCourseUser.setWechatName(wechatName);
		stockCourseUser.setCourseNumber(courseNumber);
		stockCourseUser.setCourseNumberStatus(courseNumberStatus);
		stockCourseUser.setLearnedCourseNumber(learnedCourseNumber);
		stockCourseUser.setWatchedLiveStream(watchedLiveStream);
		stockCourseUser.setName(name);
		stockCourseUser.setReceivingAddress(receivingAddress);
		stockCourseUser.setReceivingMobile(receivingMobile);
		stockCourseUser.setAccountOpenTime(openDate);
		stockCourseUser.setCustomerId(customerId);
		
		result = stockCourseBiz.registration(stockCourseUser,type);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0,"");
	}
	
	
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(false)
	public ResultDto learningprogress(RequestParams params, AccTokenUser user, Customer customer) {
		String mobile = user == null ? null : user.getMobile();
		StatusObjDto<StockCourseUser> result = stockCourseBiz.learningprogress(mobile);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}
	
	@Signature(true)
	//@MustLogin(true)
	//@CustomerMustLogin(false)
	public ResultDto isregister(RequestParams params, AccTokenUser user, Customer customer) {
		String mobile = null;
		if(ObjectUtils.isNotEmptyOrNull(user)) {
			mobile = user.getMobile();
    	}
		if(StringUtils.isEmpty(mobile)) {
			mobile = params.getString("mobile");
		}
		Map<String,Object> data = Maps.newHashMap();
		StatusObjDto<Boolean> result = stockCourseBiz.isRegister(mobile);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		data.put("isRegister", result.getObj()?1:0);
		return new ResultDto(0, data,"");
	}
	
	
    @Signature(true)
    @MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto couponrecieve(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String prizeType = (String) params.get("prizeType");
		String userId = user == null?null:user.getUserId();
		String openId = user == null?null:user.getOpenid();
		String recommendCode = (String) params.get("recommendCode");
		String mobile = "";
		if (user != null) {
			mobile = user.getMobile();
		}
		if (StringUtils.isBlank(mobile)) {
			mobile = customer.getMobile();
		}

		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
    	if (!StockCourseUser.ACT_STOCK_COURSE.equals(activityCode)) {
    		throw ActivityBizException.ACTIVITY_PARAMS_EXCEPTION.format("activityCode");
    	}
    	
    	//股票课报名用户检查
		StatusDto result = stockCourseBiz.stockCourseUserCheck(mobile,customer);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		
		AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO().setUserId(userId)
				.setCustomerId(customer.getCustomerId()).setActivityCode(activityCode)
				.setPrizeType(prizeType).setOpenId(openId)
				.setRecommendCode(recommendCode).setMobile(mobile);

		List<CouponRecieveStatusDto> recieveStatusDtos = couponRecieveBiz.receivePriceCommon(acReceivePriceVO);
		ResultDto back = null;
		if (recieveStatusDtos!=null){
			if (recieveStatusDtos.size()==1) {
				back = new ResultDto(0, BeanUtils.beanToMap(recieveStatusDtos.get(0)), "");
			}else{
				back = listResult(recieveStatusDtos);
			}
		}
		return back;
	}   
    
	public static <T> ResultDto listResult(List<T> list ) {
		Map<String,Object> data = new HashMap<>();
		if (list!=null && list.size()>0) {
			JSONArray jsonObject = (JSONArray) JSON.toJSON(list);
			data.put("list",jsonObject);
		}
		return new ResultDto(ResultDto.SUCCESS,data,"");
	}


}
