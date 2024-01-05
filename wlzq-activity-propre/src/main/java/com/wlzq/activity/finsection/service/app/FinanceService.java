package com.wlzq.activity.finsection.service.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.ActivityConstant;
import com.wlzq.activity.base.biz.ActLotteryBiz;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.base.dto.WinDto;
import com.wlzq.activity.base.model.ActHotLine;
import com.wlzq.activity.base.model.ActLotteryEnum;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActTeam;
import com.wlzq.activity.base.model.ActTeamMember;
import com.wlzq.activity.finsection.biz.FinanceBiz;
import com.wlzq.activity.finsection.biz.impl.FinanceBizImpl;
import com.wlzq.activity.finsection.dto.ActTeamDto;
import com.wlzq.activity.finsection.dto.FinSectionScheduleDto;
import com.wlzq.activity.finsection.dto.StepActivtyOverview;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;

/**
 * 
 * @author zhaozx
 * @version 2019-07-19
 */
@Service("activity.finsection")
public class FinanceService extends BaseService {

	@Autowired
	private FinanceBiz financeBiz;
	
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    
    @Autowired 
    private ActLotteryBiz actLotteryBiz;
    
	@Value("${userinfo.default.headimg}")
	private String defaultHeadimg;
    
	//818理财节第一阶段领券活动编码
	public final static String ACTIVITY_CODE_818_2019_1_2 = new String("ACTIVITY.818.2019.1.2.COUPON");
	//818理财节第一阶段领券奖品编码
	public final static String ACT_FINSECTION_STEP_1_PRIZE_CODE = new String("PRIZE.818.2019.1.2");	
	
	//二阶段领券活动编码
	public final static String ACTIVITY_CODE_818_2019_2_2 = new String("ACTIVITY.818.2019.2.2.COUPON");
	public final static String ACT_FINSECTION_STEP_2_PRIZE_CODE = new String("PRIZE.818.2019.2.2");
	
	@Signature(true)
	public ResultDto getschedule(RequestParams params) {
		StatusObjDto<FinSectionScheduleDto> result = financeBiz.getSchedule();
		
		//List<Long> prizes = actPrizeBiz.findAvailablePrizes(result.getObj().getActivityCode(), result.getObj().getPrizeTypeCode());
		//奖品个数小于5提前结束
    	//if (result.getObj() != null) {
    	//	result.getObj().setLeftPrizeCount(prizes.size() < 5 ? 0 : prizes.size());
		//}
		
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	/**
	 * 活动提醒,客户可登陆可不登陆
	 * @param params
	 * @param user
	 * @param customer
	 * @return
	 */
	@Signature(true)
	@MustLogin(true)
	public ResultDto remind(RequestParams params, AccTokenUser user, Customer customer) {
		String appointmentCode = ActivityConstant.FINSECTION_STEP_REMIND;
		String appointmentName = ActivityConstant.FINSECTION_STEP_REMIND_NAME;
		String userId = user.getUserId();
		String phone = user.getMobile();
		//客户可登陆可不登陆
		String customerId = customer == null ? null : customer.getCustomerId();
		StatusObjDto<Integer> result = financeBiz.appointment(appointmentCode, appointmentName, userId, customerId, null, phone, null, null);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		Integer count = result.getObj();
		Integer status = count > 0 ? 1 : 0;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("status", status);
		return new ResultDto(0, data, "");
	}
	
	/**
	 * 预约产品咨询
	 * @param params
	 * @param user
	 * @param customer
	 * @return
	 */
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto appointment(RequestParams params, AccTokenUser user,Customer customer) {
		String appointmentCode = ActivityConstant.FINSECTION_COLLECTION_APPOINTMENT;
		String appointmentName = ActivityConstant.FINSECTION_COLLECTION_APPOINTMENT_NAME;
		String appointmentTime = params.getString("appointmentTime");
		String template = ActivityConstant.FINSECTION_SMS_TEMPLATE;
		String phone = params.getString("phone") == null ? user.getMobile() : params.getString("phone");
		String userId = user.getUserId();
		String customerId = customer.getCustomerId();
		String userName = customer.getUser_name();
		StatusObjDto<Integer> result = financeBiz.appointment(appointmentCode, appointmentName, userId, customerId, userName, phone, appointmentTime, template);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		Integer count = result.getObj();
		Integer status = count > 0 ? 1 : 0;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("status", status);
		return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	@MustLogin(false)
	public ResultDto remindcount(RequestParams params, AccTokenUser user,Customer customer) {
		String appointmentCode = ActivityConstant.FINSECTION_STEP_REMIND;
		String userId = user == null ? null : user.getUserId();
		String customerId = customer == null ? null : customer.getCustomerId();
		
		StatusObjDto<Integer> result = financeBiz.appointmentCount(appointmentCode, userId, customerId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = new HashMap<String,Object>();
		Integer count = result.getObj();
		data.put("count", count);
		return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	@MustLogin(false)
	public ResultDto teamhall(RequestParams params) {
		Integer successCount = params.getInt("successCount");
		Integer formingCount = params.getInt("formingCount");
		StatusObjDto<ActTeamDto> result = financeBiz.teamhall(successCount, formingCount);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto gethotline(RequestParams params, AccTokenUser user,Customer customer) {
		String customerId = customer.getCustomerId();
		String userId = user == null ? customer.getUser_id() : user.getUserId();
		StatusObjDto<ActHotLine> result = financeBiz.getHotLine(userId, customerId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto hotlineclick(RequestParams params, AccTokenUser user,Customer customer) {
		String customerId = customer.getCustomerId();
		String userId = user == null ? customer.getUser_id() : user.getUserId();
		StatusObjDto<Integer> result = financeBiz.hotlineClick(userId, customerId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		Integer count = result.getObj();
		Integer status = count > 0 ? 1 : 0;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("status", status);
    	return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto myteam(RequestParams params, AccTokenUser user,Customer customer) {
		String customerId = customer.getCustomerId();
		
		String teamTemplate = ObjectUtils.isEmptyOrNull(params.getString("template")) ? FinanceBizImpl.FINSECTION_TEAM_TEAMLATE : params.getString("template");
		
		StatusObjDto<ActTeam> result = financeBiz.myTeam(null, customerId, teamTemplate);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), ""); 
	}
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto dismissteam(RequestParams params, AccTokenUser user,Customer customer) {
		String teamSerial = params.getString("teamSerial");
		String customerId = customer.getCustomerId();
		StatusObjDto<Integer> result = financeBiz.dismissTeam(null, customerId, teamSerial);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		Integer count = result.getObj();
		Integer status = count > 0 ? 1 : 0;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("status", status);
    	return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto formteam(RequestParams params, AccTokenUser user,Customer customer) {
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(ACTIVITY_CODE_818_2019_1_2, ACT_FINSECTION_STEP_1_PRIZE_CODE);
		if (prizes != null && prizes.size() < FinanceBizImpl.FINSECTION_PRIZE_LEFT) {
			throw ActivityBizException.ACT_EMPTY_PRIZE; 
		}
		
		String teamSerial = params.getString("teamSerial");
		String userId = user == null ? null : user.getUserId();
		String customerId = customer.getCustomerId();
		StatusObjDto<ActTeam> result = financeBiz.formTeam(teamSerial, userId, customerId, 0);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	
		//发优惠券，则发券
		List<ActPrize> prizeList1 = actPrizeBiz.findPrize(ACTIVITY_CODE_818_2019_1_2, result.getObj().getCreateCustomerId(), null, null, ACT_FINSECTION_STEP_1_PRIZE_CODE, null, null);
		if (prizeList1 == null || prizeList1.size() == 0) {
			actPrizeBiz.giveOutPrize(ACTIVITY_CODE_818_2019_1_2, "", null, ACT_FINSECTION_STEP_1_PRIZE_CODE, userId, null, result.getObj().getCreateCustomerId(), null);
		}
		List<ActPrize> prizeList2 = actPrizeBiz.findPrize(ACTIVITY_CODE_818_2019_1_2, customerId, null, null, ACT_FINSECTION_STEP_1_PRIZE_CODE, null, null);
		if (prizeList2 == null || prizeList2.size() == 0) {
			actPrizeBiz.giveOutPrize(ACTIVITY_CODE_818_2019_1_2, "", null, ACT_FINSECTION_STEP_1_PRIZE_CODE, userId, null, customerId, null);
		}
		
		List<ActTeamMember> teamMemberList = new ArrayList<>();
		ActTeamMember member = new ActTeamMember();
		member.setUserId(userId);
		member.setCustomerId(customerId);
		member.setCreateDate(new Date());
		member.setNickName(user == null ? null : user.getNickName());
		member.setUserName(customer.getUser_name());
		member.setPortrait(user == null ? defaultHeadimg : user.getPortrait());
		teamMemberList.add(member);
		result.getObj().setMemberNameList(teamMemberList);
    	if (result.getObj() != null) {
    		result.getObj().setMemberNameList(teamMemberList);
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	@Signature(true)
	@MustLogin(true)
	public ResultDto lightteam(RequestParams params, AccTokenUser user,Customer customer) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
   		if(thirdType != null && !thirdType.equals(1) || ObjectUtils.isEmptyOrNull(thirdUid)) {
   			throw BizException.COMMON_CUSTOMIZE_ERROR.format("微信未登录");
   		}
   		
		String teamSerial = params.getString("teamSerial");
		String userId = user.getUserId();
		String portrait = user.getPortrait();
		String nickName = user.getNickName();
		String customerId = customer == null ? null : customer.getCustomerId();
		String mobile = user.getMobile() != null ? user.getMobile() : params.getString("mobile") != null ? params.getString("mobile") : null;
		StatusObjDto<ActTeam> result = financeBiz.lightTeam(ACTIVITY_CODE_818_2019_2_2, teamSerial, userId, nickName, portrait, customerId, thirdUid);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	
		//组队成功
    	if (result.getObj().getStatus().compareTo(ActTeam.SUCCESS) == 0) {
    		List<ActPrize> prizeList = actPrizeBiz.findPrize(ACTIVITY_CODE_818_2019_2_2, result.getObj().getCreateCustomerId(), null, null, ACT_FINSECTION_STEP_2_PRIZE_CODE, null, null);
    		if (prizeList == null || prizeList.size() == 0) {
    			actPrizeBiz.giveOutPrize(ACTIVITY_CODE_818_2019_2_2, "", null, ACT_FINSECTION_STEP_2_PRIZE_CODE, result.getObj().getCreateUserId(), thirdUid, result.getObj().getCreateCustomerId(), mobile);
    		}
		}
		
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	@Signature
	@CustomerMustLogin(true)
	public ResultDto createteam(RequestParams params, AccTokenUser user,Customer customer) {
		StatusObjDto<FinSectionScheduleDto> dto = financeBiz.getSchedule();
		StatusDto checkPrize = checkEnoughPrize(dto.getObj().getActivityCode(), dto.getObj().getPrizeTypeCode(), FinanceBizImpl.FINSECTION_PRIZE_LEFT);
		if (!checkPrize.isOk()) {
			return new ResultDto(checkPrize.getCode(), checkPrize.getMsg());
		}
		//已获得奖品，不能再组队
		String userId = user == null ? null : user.getUserId();
		String openId = user == null ? null : user.getThirdUid();
		String customerId = customer.getCustomerId();
		List<ActPrize> prizeList = actPrizeBiz.findPrize(dto.getObj().getActivityCode(), customerId, null, null, dto.getObj().getPrizeTypeCode(), null, null);
		if (prizeList != null && prizeList.size() > 0) {
			throw ActivityBizException.FINSECTION_HAS_COUPON;
		}
		String template = ObjectUtils.isEmptyOrNull(params.getString("template")) ? FinanceBizImpl.FINSECTION_TEAM_TEAMLATE: params.getString("template");
		
		StatusObjDto<ActTeam> result = financeBiz.createTeamToGetCoupon(dto.getObj().getActivityCode(), userId, customerId, template, openId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	if (result.getObj() != null) {
    		result.getObj().setCreateUserName(customer.getUser_name());
    		result.getObj().setCreatePortrait(user == null ? defaultHeadimg : user.getPortrait());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	@Signature
	@MustLogin(true)
	public ResultDto findteam(RequestParams params, AccTokenUser user,Customer customer) {
		String teamSerial = params.getString("teamSerial");
		String userId = user == null ? null : user.getUserId();
		String openId = user == null ? null : user.getThirdUid();
		String customerId = customer == null ? null : customer.getCustomerId();
		StatusObjDto<ActTeam> result = financeBiz.findTeamBySerial(teamSerial, userId, customerId, openId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	@Signature
	@MustLogin(true)
	public ResultDto canlotterycount(RequestParams params, AccTokenUser user,Customer customer) {
		String userId = user == null ? null : user.getUserId();
		String openId = user == null ? null : user.getThirdUid();
		StatusObjDto<Integer> result = financeBiz.lightCount(ACTIVITY_CODE_818_2019_2_2, FinanceBizImpl.FINSECTION_CARD_TEAM_TEAMLATE, userId, openId);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		int lotteryCount = 0;
		if (result.getObj().compareTo(0) == 1) {
			lotteryCount =  actLotteryBiz.lotteryCount(ACTIVITY_CODE_818_2019_2_2, userId, openId, null, null);
		}
		Map<String, Object> data = Maps.newHashMap();
		Integer count = result.getObj() - lotteryCount < 0 ? 0 : result.getObj() - lotteryCount;
		data.put("count", count);
		return new ResultDto(0, data, "");
	}
	
	@Signature
	@MustLogin(true)
	public ResultDto lottery(RequestParams params, AccTokenUser user,Customer customer) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
   		if(thirdType != null && !thirdType.equals(1) || ObjectUtils.isEmptyOrNull(thirdUid)) {
   			throw BizException.COMMON_CUSTOMIZE_ERROR.format("微信未登录");
   		}
		String userId = user.getUserId();
		String customerId = customer == null ? null : customer.getCustomerId();
		
		
		ResultDto countResult = canlotterycount(params, user, customer);
		if (countResult.getCode().compareTo(ResultDto.SUCCESS) != 0) {
			return new ResultDto(countResult.getCode(), countResult.getMsg());
		}
		Integer count = countResult.getData().get("count") == null ? 0 : (Integer)countResult.getData().get("count");
		if (count.compareTo(0) == 0) {
			throw ActivityBizException.FINSECTION_ZERO_LOTTERY_COUNT;
		}
		
		String mobile = user.getMobile() != null ? user.getMobile() : params.getString("mobile") != null ? params.getString("mobile") : null;
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			throw ActivityBizException.FINSECTION_EMPTY_MOBILE;
		}
		
		List<ActLotteryEnum> list = Arrays.asList(ActLotteryEnum.FINSECTION_THANKS, ActLotteryEnum.FINSECTION_VEST_COUPON, ActLotteryEnum.FINSECTION_LEVEL2, ActLotteryEnum.FINSECTION_REDENVELOPE);
		StatusObjDto<LotteryDto> result =  actLotteryBiz.lottery(list, ACTIVITY_CODE_818_2019_2_2, userId, thirdUid, customerId, mobile, ActLotteryEnum.FINSECTION_THANKS);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
    @Signature(true)
   	public ResultDto prizelist(RequestParams params,AccTokenUser user) {
    	int[] page = buildPage(params);
   		StatusObjDto<List<WinDto>> result = actLotteryBiz.prizes(ACTIVITY_CODE_818_2019_2_2, page[0], page[1]);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}
	
	public ResultDto findleftprize(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = params.getString("activityCode");
		String prizeTypeCode = params.getString("prizeTypeCode");
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(activityCode, prizeTypeCode);
		Map<String,Object> data = new HashMap<String,Object>();
		if (prizes != null && prizes.size() < FinanceBizImpl.FINSECTION_PRIZE_LEFT) {
			data.put("count", 0);
			return new ResultDto(0, data, ""); 
		}
		data.put("count", prizes.size());
		return new ResultDto(0, data, "");
	}
	
	private StatusDto checkEnoughPrize(String activityCode, String prizeCode, Integer mixPrizeLeft) {
		//奖品数量不足，不能再组队
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(activityCode, prizeCode);
		if (prizes != null && prizes.size() < FinanceBizImpl.FINSECTION_PRIZE_LEFT) {
			return new StatusDto(false, ActivityBizException.ACT_EMPTY_PRIZE.getCode(), ActivityBizException.ACT_EMPTY_PRIZE.getMsg());
		}
		return new StatusDto(true);
	}
	
	public ResultDto prizes(RequestParams params) {
		String activityCode = params.getString("activityCode");
		Integer start = params.getInt("start");
		Integer end = params.getInt("end");
		StatusObjDto<List<WinDto>> result = actLotteryBiz.prizes(activityCode, start, end);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto unreceivedprize(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = ACTIVITY_CODE_818_2019_2_2;
		String userId = user == null ? null : user.getUserId();
		String customerId = customer == null ? null : customer.getCustomerId();
		String prizeTypeCode = ActLotteryEnum.FINSECTION_VEST_COUPON.getPrizeTypeCode();
		List<String> prizeTypeCodeList = new ArrayList<>();
		prizeTypeCodeList.add(ActLotteryEnum.FINSECTION_VEST_COUPON.getPrizeTypeCode());
		
		Map<String, Object> data = Maps.newHashMap();
		List<ActPrize> prizeListCustomer = actPrizeBiz.findPrize(activityCode, customerId, null, null, prizeTypeCode, ActPrize.STATUS_SEND, null);
		if (prizeListCustomer.size() > 0) {
			data.put("stauts", 0);
			data.put("count", 0);
			return new ResultDto(0, data, "");
		}
		
		List<ActPrize> prizeList = actPrizeBiz.findPrize(activityCode, null, userId, null, prizeTypeCode, ActPrize.STATUS_OCCUPY, null);
		
		ResultDto result = null;
		if (prizeList.size() > 0) {
			Map<String, Object> busparams = Maps.newHashMap();
			busparams.put("userId", userId);
			busparams.put("customerId", customerId);
			busparams.put("code", prizeList.get(0).getRedeemCode());
			result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
			actPrizeBiz.updatePrize(userId, user.getOpenid(), customerId, prizeList.get(0).getId(), ActPrize.STATUS_SEND, customer.getMobile(), null);
		}
		Integer stauts = 1;
		if (result != null && !result.getCode().equals(ResultDto.SUCCESS)) {
			stauts = 0;
		}
		
		data.put("stauts", stauts);
		data.put("count", prizeList.size());
		return new ResultDto(0, data, "");
	}
	
	@Signature(true)
	public ResultDto stepoverview(RequestParams params, AccTokenUser user,Customer customer) {
		String activity = "ACTIVITY.818.2019.3.2.COUPON";
		String prizeTypeCode  =  "PRIZE.818.2019.3.2";
		String customerId = customer == null ? null : customer.getCustomerId();
		StatusObjDto<StepActivtyOverview> result = financeBiz.stepOverView(activity, prizeTypeCode, customerId);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
	}
	
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
