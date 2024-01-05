
package com.wlzq.activity.redeem.service.app;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.redeem.biz.RedeemBiz;
import com.wlzq.activity.redeem.dto.RedeemDto;
import com.wlzq.activity.redeem.dto.RedeemGoodsDto;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
/**
 * KLineService服务类
 * @author 
 * @version 1.0
 */
@Service("activity.redeem")
public class RedeemService{
	private static final String ACTIVITY_CODE = "ACTIVITY.REDEEM"; 
    @Autowired
    private RedeemBiz redeemBiz;

    
    @Signature(false)
	public ResultDto sendtowechat(RequestParams params) {
    	
    	if(params != null) {
    		return  new ResultDto(1,"活动已过期");
    	}
    	String openid = params.getSysString("openid");
		String nickname = params.getSysString("nickname");
		String fundAccount = params.getSysString("fundAccount");
		if(ObjectUtils.isEmptyOrNull(openid)) {
			return new ResultDto(1,"openid不能为空");
		}
		if(ObjectUtils.isEmptyOrNull(fundAccount)) {
			return new ResultDto(1,"fundAccount不能为空");
		}
		
		StatusDto result = redeemBiz.sendCodeToWechat(ACTIVITY_CODE, openid,fundAccount,nickname);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	
    	return new ResultDto(0,new HashMap<String,Object>(),"");
	}
    
    @Signature(true)
   	public ResultDto findRedeem(RequestParams params,AccTokenUser user) {
    	if(user == null) {
    		return  new ResultDto(CodeConstant.NOT_LOGIN,"未登录");
    	}
       	Integer thirdType = user.getThirdType();
       	if(thirdType == null || !thirdType.equals(1)) {
       		return new ResultDto(1,"请在公众号打开");
       	}
       	String openId = user.getThirdUid();
   		if(ObjectUtils.isEmptyOrNull(openId)) {
   			return new ResultDto(1,"openid不能为空");
   		}
   		
   		StatusObjDto<RedeemDto> result = redeemBiz.findRedeemByOpenId(openId);
   		Map<String,Object> data = result.isOk()?BeanUtils.beanToMap(result.getObj()):new HashMap<String,Object>();
       	
       	return new ResultDto(0,data,"");
   	}
    
    @Signature(true)
   	public ResultDto receive(RequestParams params,AccTokenUser user) {
   		if(user == null) {
   			return new ResultDto(CodeConstant.NOT_LOGIN,"未登录");
   		}
   		if(ObjectUtils.isEmptyOrNull(user.getMobile())){
   			return new  ResultDto(CodeConstant.NOT_LOGIN,"未绑定手机号");
   		}
   		
   		String activityCode = (String)params.get("activityCode");
   		
   		String code = params.getString("code");
		String mobile = user.getMobile();
		String userId = user.getUserId();
		String recommendMobile = params.getString("recommendMobile");
		StatusObjDto<RedeemGoodsDto>  result = redeemBiz.recieve(activityCode, code, mobile, userId, recommendMobile);
   		if(!result.isOk()) {
   			return new ResultDto(result.getCode(),result.getMsg());
   		}
       	
       	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
   	}
    
}
