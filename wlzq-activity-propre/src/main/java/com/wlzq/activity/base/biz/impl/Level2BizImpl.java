package com.wlzq.activity.base.biz.impl;

import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.biz.Level2Biz;
import com.wlzq.activity.base.dto.Level2OpenDto;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.HttpClientUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.common.utils.RegxUtils;
import com.wlzq.common.utils.security.RSAUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

/**
 * Level2开通接口实现
 * @author louie
 *
 */
@Service
public class Level2BizImpl implements Level2Biz {
	//同花顺加密RSA参数配置
	public static final String CONFIG_RSA_KEY="base.level2.rsakey";
	//同花顺加密RSA参数配置
	public static final String CONFIG_MOBILE_NEED_ENC="base.level2.openneedenc";
	@Value("${lottery.ths.order.create}")
	private String createOrderUrl;
	@Value("${lottery.ths.order.notify}")
	private String notifyUrl;
	
	@Override
	public StatusObjDto<Level2OpenDto> openLevel2(Integer goodsId,String mobile, Integer payMode, String reason, String recommendMobile) {
		if(ObjectUtils.isEmptyOrNull(goodsId)) {
			return new StatusObjDto<Level2OpenDto>(false,StatusDto.FAIL_COMMON,"goodsId不能为空"); 
		}
		if(ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<Level2OpenDto>(false,StatusDto.FAIL_COMMON,"mobile不能为空"); 
		}
		if(!RegxUtils.isMobile(mobile)) {
			return  new StatusObjDto<Level2OpenDto>(false,StatusDto.FAIL_COMMON,"mobile无效"); 
		}
		if(ObjectUtils.isEmptyOrNull(payMode)) {
			return new StatusObjDto<Level2OpenDto>(false,StatusDto.FAIL_COMMON,"payMode不能为空"); 
		}
		if(payMode < 6 || payMode > 7) {
			return new StatusObjDto<Level2OpenDto>(false,StatusDto.FAIL_COMMON,"payMode无效"); 
		}
		String url = "";
		Level2OpenDto openDto = new Level2OpenDto();
		try {	
			//创建订单
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("goodsId", goodsId);
			Integer mobileNeedEnc = AppConfigUtils.getInt(CONFIG_MOBILE_NEED_ENC,0);
			if(CodeConstant.CODE_YES.equals(mobileNeedEnc)) {
				String rsaKey =  AppConfigUtils.get(CONFIG_RSA_KEY);
				RSAPublicKey pubKey = RSAUtils.getPublicKey(rsaKey);
				String mobileJson = "{\"mobile_phone\":\""+mobile+"\"}";
				mobile = RSAUtils.encryptByPublicKeyToBase64(mobileJson, pubKey);
			}
			params.put("customerId", mobile);
			if(ObjectUtils.isNotEmptyOrNull(mobile)) {
				params.put("phone", mobile);
			}
			url = createOrderUrl+",params:"+JsonUtils.mapToJson(params);
			String createResultS = HttpClientUtils.doPost(createOrderUrl, params);
			Map<String,Object> orderMap = JsonUtils.json2Map(createResultS);
			Integer code = (Integer) orderMap.get("code");
			if(code == null || !code.equals(0)) {
				openDto.setOrderNo("");
				openDto.setStatus(Level2OpenDto.STATUS_FAIL);
				openDto.setMessage(url+",result:"+createResultS);
				return new StatusObjDto<Level2OpenDto>(false,openDto,1,"");
			}
			String order = (String) orderMap.get("data");

			//修改权限
			params.clear();
			params.put("orderNo", order);	
			params.put("reason", reason);
			params.put("channelCode", 8);
			params.put("payStatus", 1); 
			params.put("payMode", payMode);
			params.put("customerProductType", 1);
			params.put("practicalPrice", "0"); //实收金额设置为0
			if(ObjectUtils.isNotEmptyOrNull(recommendMobile)) {
				params.put("phone", recommendMobile);
			}
			url = notifyUrl+",params:"+JsonUtils.mapToJson(params);
			String notifyResultS = HttpClientUtils.doPost(notifyUrl, params);
			Map<String,Object> notifyMap = JsonUtils.json2Map(notifyResultS);
			code = (Integer) notifyMap.get("code");
			if(code == null || !code.equals(0)) {
				openDto.setOrderNo(order);
				openDto.setStatus(Level2OpenDto.STATUS_FAIL);
				openDto.setMessage(url+",result:"+notifyResultS);
				return new StatusObjDto<Level2OpenDto>(false,openDto,code,"");
			}
			openDto.setOrderNo(order);
			openDto.setStatus(Level2OpenDto.STATUS_SUCCESS);
			openDto.setMessage(url+",result:"+notifyResultS);
			return  new StatusObjDto<Level2OpenDto>(true,openDto,StatusDto.SUCCESS,"");
		} catch (Exception e) {
			openDto.setOrderNo("");
			openDto.setStatus(Level2OpenDto.STATUS_FAIL);
			openDto.setMessage(url+",result:"+e.toString());
			e.printStackTrace();
		}
		return  new StatusObjDto<Level2OpenDto>(false,openDto,1,"");
	}
	
}
