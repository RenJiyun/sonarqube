package com.wlzq.activity.redenvelope.biz.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.redenvelope.biz.RedEnvelopeBiz;
import com.wlzq.activity.redenvelope.config.RedEnvelopeConfig;
import com.wlzq.activity.redenvelope.dao.RedEnvelopeDao;
import com.wlzq.activity.redenvelope.dto.CallbackParam;
import com.wlzq.activity.redenvelope.dto.CreateParam;
import com.wlzq.activity.redenvelope.dto.RedEnvelopeDto;
import com.wlzq.activity.redenvelope.dto.RedEnvelopeNotifyDto;
import com.wlzq.activity.redenvelope.helper.RedPactetHelper;
import com.wlzq.activity.redenvelope.model.RedEnvelope;
import com.wlzq.activity.redenvelope.observer.Observers;
import com.wlzq.activity.redenvelope.observer.RedEnvelopeObserver;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.service.base.serialnum.SerialNumberUtils;

/**
 * 红包业务接口实现
 * @author louie
 *
 */
@Service
public class RedEnvelopeBizImpl implements RedEnvelopeBiz { 
	
	private Logger logger = LoggerFactory.getLogger(RedEnvelopeBizImpl.class);

	private SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); 
	
	@Autowired
	private RedEnvelopeConfig config;
	
	@Autowired
	private	RedEnvelopeDao redEnvelopeDao;

	@Override
	public StatusObjDto<RedEnvelopeDto> create(RedEnvelope redEnvelope) {
		return create(redEnvelope,false);
	}
	
	@Override
	public StatusObjDto<RedEnvelopeDto> create(RedEnvelope redEnvelope, boolean isNeedCheck) {
		
		checkParams(redEnvelope);
		RedEnvelopeDto envelopeDto = new RedEnvelopeDto();
		//检查是否有相同的业务单
		RedEnvelope existEnvelope = redEnvelopeDao.findByBusinessNo(redEnvelope.getBusinessNo());
		if(existEnvelope != null) {
			if(existEnvelope.getStatus().equals(RedEnvelope.STATUS_RECIEVE)) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("红包已领取");
			}
			//直接返回
			BeanUtils.copyProperties(existEnvelope, envelopeDto);
			return new  StatusObjDto<RedEnvelopeDto>(true,envelopeDto,0,"");
		}
		
		String orderNo = SerialNumberUtils.getRedEnvelopeNo();
		redEnvelope.setOrderNo(orderNo);
		CreateParam param = getCreateParam(redEnvelope);
		String paramJson = JsonUtils.object2JSON(param);
		Map<String,String> params = JsonUtils.json2Map(paramJson);
		String recieveUrl = RedPactetHelper.assemUrl(config.getUrl(), params, config.getSecret(), null);
		redEnvelope.setRecieveUrl(recieveUrl);
		redEnvelope.setStatus(RedEnvelope.STATUS_NO_RECIEVE);
		redEnvelope.setSendData(paramJson);
		Integer checkStatus = isNeedCheck?2:1;
		redEnvelope.setCheckStatus(checkStatus);
		redEnvelope.setCreateTime(new Date());
		redEnvelopeDao.insert(redEnvelope);
		
		BeanUtils.copyProperties(redEnvelope, envelopeDto);
		
		return new  StatusObjDto<RedEnvelopeDto>(true,envelopeDto,0,"");
	}

	@Override
	public StatusDto notify(String result) {
		logger.error("红包回调结果："+result);
		if(ObjectUtils.isEmptyOrNull(result)) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("回调信息为空");
		}
		Map<String,String> mapResult = RedPactetHelper.decrypt(config.getSecret(), result);
		if(mapResult == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("回调数据解密错误");
		}
		logger.info(mapResult.toString());
		CallbackParam callbackParam = JsonUtils.json2Bean(JsonUtils.map2Json(mapResult),CallbackParam.class);
		String orderNo = callbackParam.getSceneKey();
		RedEnvelope envelope = redEnvelopeDao.findByOrderNo(orderNo);
		if(envelope == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("红包不存在");
		}
		if(envelope.getStatus().equals(RedEnvelope.STATUS_RECIEVE)) {
			//throw BizException.COMMON_CUSTOMIZE_ERROR.format("红包已发送成功");
		}
		Integer status = callbackParam.getResult().equals(CallbackParam.SUCCESS)?RedEnvelope.STATUS_RECIEVE:RedEnvelope.STATUS_RECIEVE_FAIL;
		envelope.setStatus(status);
		if(status.equals(RedEnvelope.STATUS_RECIEVE) && ObjectUtils.isNotEmptyOrNull(callbackParam.getPaymentTime())) {
			try {
				Date payTime = sdf.parse(callbackParam.getPaymentTime());
				envelope.setPayTime(payTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		envelope.setRemark(callbackParam.getErrorMsg());
		envelope.setCallbackTime(new Date());
		envelope.setCallbackData(JsonUtils.object2JSON(callbackParam));
		
		redEnvelopeDao.update(envelope);
		//通知业务平台处理
		if(status.equals(RedEnvelope.STATUS_RECIEVE)) {
			notify(envelope);
		}
		
		return new StatusDto(true,0,"");
	}
	
	private void checkParams(RedEnvelope redEnvelope) {
		if(ObjectUtils.isEmptyOrNull(redEnvelope.getUserId())) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		if(ObjectUtils.isEmptyOrNull(redEnvelope.getOpenId())) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
		}
		if(ObjectUtils.isEmptyOrNull(redEnvelope.getBusinessCode())) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("businessCode");
		}
		if(ObjectUtils.isEmptyOrNull(redEnvelope.getBusinessNo())) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("businessNo");
		}
		Integer amount = redEnvelope.getAmount();
		if(ObjectUtils.isEmptyOrNull(amount)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("amount");
		}
		if(amount <= 0) {
			throw BizException.COMMON_PARAMS_IS_ILLICIT.format("amount");
		}
//		if(ObjectUtils.isEmptyOrNull(redEnvelope.getMobile())) {
//			throw BizException.COMMON_CUSTOMIZE_ERROR.format("mobile");
//		}
		if(ObjectUtils.isEmptyOrNull(redEnvelope.getNotifyUrl())) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("notifyUrl");
		}
	}
	
	private CreateParam getCreateParam(RedEnvelope redEnvelope){
		CreateParam param = new CreateParam();
		param.setMchNo(config.getMchNo());
		param.setSceneNo(config.getSceneNo());
		param.setSceneKey(redEnvelope.getOrderNo());
		param.setUserId(redEnvelope.getOpenId());
		String amount = String.valueOf(Double.valueOf(redEnvelope.getAmount())/100);
		param.setAmount(amount);
		param.setDescript("");
		return param;
	}
	
	private void notify(RedEnvelope envelope) {
		List<RedEnvelopeObserver> observers = Observers.getObservers();
		RedEnvelopeNotifyDto notifyDto = new RedEnvelopeNotifyDto();
		BeanUtils.copyProperties(envelope, notifyDto);
		for(RedEnvelopeObserver observer:observers) {
			observer.notify(notifyDto);
		}
	}

}
