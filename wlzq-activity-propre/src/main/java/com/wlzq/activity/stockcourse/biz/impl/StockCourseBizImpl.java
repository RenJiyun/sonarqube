package com.wlzq.activity.stockcourse.biz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.stockcourse.biz.StockCourseBiz;
import com.wlzq.activity.stockcourse.dao.StockCourseUserDao;
import com.wlzq.activity.stockcourse.model.StockCourseStatus;
import com.wlzq.activity.stockcourse.model.StockCourseUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.impl.RemotePushBizImpl;
import com.wlzq.remote.service.utils.RemoteUtils;

import cn.hutool.core.date.DateUtil;

/**
 * 股票课程活动接口实现
 * @author zjt
 *
 */
@Service
public class StockCourseBizImpl extends ActivityBaseBiz implements StockCourseBiz {
	
	private Logger logger = LoggerFactory.getLogger(StockCourseBizImpl.class);
	/** 体验券编码*/
	private static final String TEMPLATE_CODE="COUPON.INVEST.STOCKCOURSE.F7";
	/** 报名成功短信通知*/
	private static String SMS_STOCKCOURSE_REGISTRATION_REMIND = "SMS.STOCKCOURSE.REGISTRATION.REMIND";
	/** 课程名称*/
	private static final String ALL_CLASS_NAME="第一课:看盘选股技巧;第二课:多空博弈之量价关系(上);第三课:多空博弈之量价关系(下);第四课:MACD三大特殊买卖法;第五课:区间套利--抄底三式";
	
	@Autowired
	private	StockCourseUserDao stockCourseDao;
	@Autowired
	private RemotePushBizImpl remotePushBizImpl;
    @Value("${stockcourse.customer.service.url}")
    public String stockCourseCustomerServiceUrl;
    @Value("${stockcourse.attend.class.url}")
    public String stockCourseAttendClassUrl;
	

	@Override
	public StatusDto stockCourseUserCheck(String mobile,Customer customer) {
		
		if(StringUtils.isEmpty(mobile)){
			throw BizException.USER_NOT_BIND_MOBILE;
		}
		
		if(StringUtils.isEmpty(customer.getCustomerId())){
			throw BizException.CUSTOMER_NOT_LOGIN_ERROR;
		}
		
		StockCourseUser stockCourseUser = stockCourseDao.getByMobileAndTemplateCode(mobile,TEMPLATE_CODE);
		
		if(ObjectUtils.isEmptyOrNull(stockCourseUser)) {
			throw ActivityBizException.INTERNSHIP_SIGNUP_NOTFOUND;
		}
		
		if(StockCourseUser.RECEIVED_COUPON.equals(stockCourseUser.getReceivedCoupon())) {
			throw ActivityBizException.DOUBLE_RECIEVE_ALREADY;
		}
		
		//更新信息
		stockCourseUser.setCustomerId(customer.getCustomerId());
		stockCourseUser.setAccountOpenTime(customer.getOpenDate());
	    this.registration(stockCourseUser, StockCourseUser.USER_UPDATE);
		return new StatusDto(true, StatusDto.SUCCESS, "");
	}

	@Override
	public StatusObjDto<StockCourseUser> learningprogress(String mobile) {
		if(StringUtils.isEmpty(mobile)){
			throw BizException.USER_NOT_BIND_MOBILE;
		}
		StockCourseUser stockCourseUser = stockCourseDao.getByMobileAndTemplateCode(mobile,TEMPLATE_CODE);
		if(ObjectUtils.isEmptyOrNull(stockCourseUser)) {
			throw ActivityBizException.INTERNSHIP_SIGNUP_NOTFOUND;
		}
		
		Date now = new Date();
		List<StockCourseStatus> stockCourseStatusList = stockCourseDao.findCouseStatusList(mobile,null);
		if(!CollectionUtils.isEmpty(stockCourseStatusList)) {
			Long updatedCount = stockCourseStatusList.stream().filter(s -> now.after(s.getClassOpenDate())).count();
			Long learnedCount = stockCourseStatusList.stream().filter(s -> StockCourseStatus.LEARNED.equals(s.getStatus())).count();
			String courseStatus = stockCourseStatusList.stream().map(s -> String.valueOf(s.getStatus())).collect(Collectors.joining());
			stockCourseUser.setUpdatedCourseNumber(updatedCount.intValue());
			stockCourseUser.setLearnedCourseNumber(learnedCount.intValue());
			stockCourseUser.setCourseStatus(courseStatus);
		}
		return new StatusObjDto<StockCourseUser>(true, stockCourseUser, StatusDto.SUCCESS, "");
	}
	
	@Override
	@Transactional
	public StatusDto registration(StockCourseUser stockCourseUser,Integer type) {
		
		if(StringUtils.isEmpty(stockCourseUser.getMobile())){
			throw BizException.USER_NOT_BIND_MOBILE;
		}
		
		if(!StockCourseUser.USER_REGISTRATION.equals(type) && !StockCourseUser.USER_UPDATE.equals(type) ){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("type");
		}
		
		StockCourseUser oldStockCourseUser = stockCourseDao.getByMobile(stockCourseUser.getMobile());
		
		Date now = new Date();
		Date classOpenDate = null;
		StockCourseStatus stockCourseStatus = new StockCourseStatus(); 
		stockCourseStatus.setMobile(stockCourseUser.getMobile());
		
		if(ObjectUtils.isEmptyOrNull(oldStockCourseUser)) {
			stockCourseUser.setWatchedLiveStream(0);
			stockCourseUser.setCreateTime(now);
			//根据手机号获取注册时间
			StatusObjDto<Map<String, Object>> result = getCustomerRegistrationTime(stockCourseUser.getMobile());
			if (ResultDto.SUCCESS.equals(result.getCode()) && ObjectUtils.isNotEmptyOrNull(result.getObj()) && ObjectUtils.isNotEmptyOrNull(result.getObj().get("ZCSJ"))) {
				Map<String, Object> resultObj = result.getObj();
				stockCourseUser.setRegistrationTime(DateUtils.parseDate((String)resultObj.get("ZCSJ"), "yyyyMMdd"));
			}
			stockCourseDao.insert(stockCourseUser);
			stockCourseStatus.setStatus(0);
			stockCourseStatus.setCreateTime(now);
			for(int i=1; i<=5; i++) {
				classOpenDate = DateUtil.parse(DateUtil.format(DateUtils.addDay(now, i-1),"yyyy-MM-dd 00:00:00"),"yyyy-MM-dd HH:mm:ss");
				stockCourseStatus.setClassNo(i);
				stockCourseStatus.setClassOpenDate(classOpenDate);
				stockCourseDao.insertStockCourseStatus(stockCourseStatus);
			}
			
			//短信通知给报名用户
			List<Object> msgParams = new ArrayList<Object>();
			msgParams.add(stockCourseCustomerServiceUrl);
			remotePushBizImpl.sendSmsWithTmpl(SMS_STOCKCOURSE_REGISTRATION_REMIND, stockCourseUser.getMobile(), msgParams);
			
	        //微信推送给报名用户
	        if (StringUtils.isNotEmpty(stockCourseUser.getOpenId())) {
	        	Map<String,String > wechatMsgParams = new HashMap<String,String>();
	            wechatMsgParams.put("first","学习奖励，实体书《股票作手回忆录》！");
	            wechatMsgParams.put("registrationType","《短线抄底-股市实战课》");
	            wechatMsgParams.put("registrationResult","报名成功");
	            wechatMsgParams.put("sendDate",DateUtil.format(now, "yyyy-MM-dd"));
	            wechatMsgParams.put("remark","点我加课程班主任微信，领学习奖励>>>");
	           this.pushToWechat(stockCourseUser.getOpenId(), "OPENTM411612150", wechatMsgParams, stockCourseCustomerServiceUrl);
	        }
		}else {
			if(StockCourseUser.USER_REGISTRATION.equals(type)) {
				throw ActivityBizException.INTERNSHIP_SIGNUP_DUPLICATE;
			}
			//如果课程学习状态为已完成
			if(ObjectUtils.isNotEmptyOrNull(stockCourseUser.getCourseNumber()) && new Integer(StockCourseStatus.LEARNED).equals(stockCourseUser.getCourseNumberStatus())) {
				//开放的课程号码
				Integer openCourseNumber = DateUtils.daysBetween(oldStockCourseUser.getCreateTime(), new Date()) + 1;
				//非当天开放的课程不能打卡；
				if(!stockCourseUser.getCourseNumber().equals(openCourseNumber) ) {
					 return new StatusDto(false, StatusDto.FAIL_COMMON, "非当天开放的课程不能打卡！");
				}								
				//更新课程状态
				stockCourseStatus.setStatus(stockCourseUser.getCourseNumberStatus());
				classOpenDate = DateUtil.parse(DateUtil.format(now,"yyyy-MM-dd 00:00:00"),"yyyy-MM-dd HH:mm:ss");
				stockCourseStatus.setClassOpenDate(classOpenDate);
				stockCourseStatus.setUpdateTime(now);
				stockCourseDao.updateStockCourseStatus(stockCourseStatus);
			}
			stockCourseUser.setId(oldStockCourseUser.getId());
			stockCourseUser.setUpdateTime(now);
			stockCourseDao.update(stockCourseUser);
		}
		return new StatusDto(true, StatusDto.SUCCESS, "");
	}
    
    private void pushToWechat(String openId, String templateCode, Map<String,String> wechatMsgParams, String linkUrl) {
        String jsonBizParams = JsonUtils.map2Json(wechatMsgParams);
        Map<String, Object> busParams = new HashMap<String, Object>();
        busParams.put("openId", openId);
        busParams.put("templateCode",templateCode);
        busParams.put("linkUrl", linkUrl);
        busParams.put("jsonBizParams", jsonBizParams);
        ResultDto result;
        try {
            result = RemoteUtils.call("push.pushcooperation.pushwechatmessage", ApiServiceTypeEnum.COOPERATION, busParams,false);
            logger.info("微信推送:openId:{},busParams:{},resultCode:{},data", openId, busParams, result.getCode(),result.getData());
        } catch (Exception e) {
            logger.error("微信推送:openId:{},busParams:{}", openId, busParams, e);
        }
    }
	
	/**
	 * 通过手机号码获取客户注册时间
	 * @param mobile
	 * @return
	 */ 
	public StatusObjDto<Map<String, Object>> getCustomerRegistrationTime(String mobile) {
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
		} 
		Map<String, Object> busparams = Maps.newHashMap();
		String serviceId = new String("ext.sjzx.ewt.khzcsj");
		Map<String, Object> params = Maps.newHashMap();
		params.put("V_SJHM", mobile);
		busparams.put("serviceId", serviceId);
		busparams.put("isNeedLogin", "1");
		busparams.put("params", JsonUtils.map2Json(params));
		ResultDto resultDto = RemoteUtils.call("base.fsdpcoopration.callservice", ApiServiceTypeEnum.COOPERATION, busparams, false);
		if (!ResultDto.SUCCESS.equals(resultDto.getCode()) || resultDto.getData() == null || resultDto.getData().isEmpty() || resultDto.getData().get("O_RESULT") == null) {
			logger.error("获取注册时间失败: busparams：{};code:{},msg:{}",busparams,resultDto.getCode(),resultDto.getMsg());
			return new StatusObjDto<>(false, resultDto.getCode(), resultDto.getMsg());
		}
		logger.info("获取注册时间: busparams：{};data:{}",busparams,resultDto.getData());
		return new StatusObjDto<>(true, parseToMap(resultDto), StatusDto.SUCCESS, "");
	}
	
	private Map<String, Object> parseToMap(ResultDto resultDto) {
		Map<String, Object> result = Maps.newHashMap();
		try {
			List<Map<String, Object>> list = (List<Map<String, Object>>)resultDto.getData().get("O_RESULT");
			if(!CollectionUtils.isEmpty(list)) {
				result = list.get(0);
			}
		} catch (Exception e) {
			logger.error("parseMap error: " + e.getMessage());
		}
		return result;
	}

	@Override
	public void stockCourseWechatPush() {
		Date now = new Date();
		Date classOpenDate = DateUtil.parse(DateUtil.format(now,"yyyy-MM-dd 00:00:00"),"yyyy-MM-dd HH:mm:ss");
		Date  pushTime = DateUtil.parse(DateUtil.format(now,"yyyy-MM-dd 19:00:00"),"yyyy-MM-dd HH:mm:ss");
		
		List<StockCourseUser> stockCourseUserList = stockCourseDao.getUserToPush(classOpenDate,pushTime);
		if(!CollectionUtils.isEmpty(stockCourseUserList)) {
			StockCourseStatus courseStatus = new StockCourseStatus();
			for(StockCourseUser stockCourseUser : stockCourseUserList) {
		        //微信推送给报名用户
		        if (StringUtils.isNotEmpty(stockCourseUser.getOpenId())) {
		        	Map<String,String > wechatMsgParams = new HashMap<String,String>();
		            wechatMsgParams.put("first","学习送书：巴菲特推荐《股票作手回忆录》");
		            wechatMsgParams.put("className",ALL_CLASS_NAME.split(";")[stockCourseUser.getCourseNumber()-1]);
		            wechatMsgParams.put("classTime","24点前学完，即为打卡成功");
		            wechatMsgParams.put("classRoom","点击我去学习>>>");
		            wechatMsgParams.put("classTeacher","万联证券 田春燕");
		            wechatMsgParams.put("remark","");
		            //微信推送
		            this.pushToWechat(stockCourseUser.getOpenId(), "OPENTM401202194", wechatMsgParams, stockCourseAttendClassUrl);
		            //更新推送时间
		            courseStatus.setUpdateTime(new Date());
		            courseStatus.setPushTime(new Date());
		            courseStatus.setMobile(stockCourseUser.getMobile());
		            courseStatus.setClassOpenDate(classOpenDate);
		            stockCourseDao.updateStockCourseStatus(courseStatus);
		        }
			}
		}
	}

	@Override
	public void stockCourseAppPush() {
		Date now = new Date();
		Date classOpenDate = DateUtil.parse(DateUtil.format(now, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		Date  pushTime = DateUtil.parse(DateUtil.format(now, "yyyy-MM-dd 12:30:00"), "yyyy-MM-dd HH:mm:ss");
		
		List<StockCourseUser> stockCourseUserList = stockCourseDao.getUserToPush(classOpenDate, pushTime);
		if(!CollectionUtils.isEmpty(stockCourseUserList)) {
			StockCourseStatus courseStatus = new StockCourseStatus();
			for(StockCourseUser stockCourseUser : stockCourseUserList) {
		        if (StringUtils.isNotEmpty(stockCourseUser.getMobile())) {
		        	//app上课提醒推送
		            this.pushToApp(stockCourseUser.getMobile(),stockCourseUser.getCourseNumber());
		            //更新推送时间
		            courseStatus.setUpdateTime(new Date());
		            courseStatus.setPushTime(new Date());
		            courseStatus.setMobile(stockCourseUser.getMobile());
		            courseStatus.setClassOpenDate(classOpenDate);
		            stockCourseDao.updateStockCourseStatus(courseStatus);
		        }
			}
		}
	}

	private void pushToApp(String mobile, Integer couserNumber) {
		Map<String,String > appPushParams = new HashMap<String,String>();
		appPushParams.put("mobile", mobile);
		appPushParams.put("url", stockCourseAttendClassUrl);
		appPushParams.put("content", "跳转链接");
		appPushParams.put("cv", "hl");
		switch (couserNumber) {
			case 1:
				appPushParams.put("title", "开课啦！第1课：看盘选股技巧");
				appPushParams.put("intro", "券商金牌投顾教学，学习有礼");
				break;
			case 2:
				appPushParams.put("title", "上课提醒！第2课：多空博弈之价量关系（上）");
				appPushParams.put("intro", "坚持学习，做时间和财富的朋友");
				break;
			case 3:
				appPushParams.put("title", "上课啦！解密股市抄底秘诀");
				appPushParams.put("intro", "第3课：多空博弈之价量关系（下）");
				break;
			case 4:
				appPushParams.put("title", "快来学！第4课：MACD三大特殊买卖法");
				appPushParams.put("intro", "告别盲目追涨杀跌");
				break;	
			case 5:
				appPushParams.put("title", "最后一课：区间套利--抄底三式");
				appPushParams.put("intro", "点我马上学！");
				break;
			default: break;	
		}
	    String jsonData = JsonUtils.map2Json(appPushParams);
        Map<String, Object> busParams = new HashMap<String, Object>();
        busParams.put("jsonData", jsonData);
	    ResultDto result;
        try {
            result = RemoteUtils.call("push.pushcooperation.pushapp", ApiServiceTypeEnum.COOPERATION, busParams,false);
            logger.info("APP推送:busParams:{},resultCode:{},data", busParams, result.getCode(), result.getData());
        } catch (Exception e) {
            logger.error("APP推送:busParams:{}", busParams, e);
        }
		
	}

	@Override
	public StatusObjDto<Boolean> isRegister(String mobile) {
		if(StringUtils.isEmpty(mobile)){
			return new StatusObjDto<Boolean>(true, false,StatusDto.SUCCESS, "");
		}
		StockCourseUser stockCourseUser = stockCourseDao.getByMobileAndTemplateCode(mobile,TEMPLATE_CODE);
		if(ObjectUtils.isEmptyOrNull(stockCourseUser)) {
			return new StatusObjDto<Boolean>(true, false,StatusDto.SUCCESS, "");
		}
		return new StatusObjDto<Boolean>(true, true,StatusDto.SUCCESS, "");
	}

}
