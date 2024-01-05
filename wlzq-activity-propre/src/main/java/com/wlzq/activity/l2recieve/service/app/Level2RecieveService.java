
package com.wlzq.activity.l2recieve.service.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.l2recieve.biz.L2RecieveBiz;
import com.wlzq.activity.l2recieve.dto.OpenDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
/**
 * Level2RecieveService服务类
 * @author 
 * @version 1.0
 */
@Service("activity.l2recieve")
public class Level2RecieveService{
    @Autowired
    private L2RecieveBiz l2RecieveBiz;

    @Signature(true)
    @MustLogin(true)
	public ResultDto status(RequestParams params,AccTokenUser user) {
		if(ObjectUtils.isEmptyOrNull(user.getMobile())){
			throw BizException.USER_NOT_BIND_MOBILE;
		}
		String mobile = user.getMobile();
		StatusObjDto<List<OpenDto>> status = l2RecieveBiz.recieveStatus(mobile);
		if(!status.isOk()) {
			return new ResultDto(status.getCode(),status.getMsg());
		}
		
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", status.getObj().size());
		data.put("info", status.getObj());
    	return new ResultDto(0,data,"");
	}
    
    @Signature(true)
    @MustLogin(true)
	public ResultDto recieve(RequestParams params,AccTokenUser user) {
    	if(ObjectUtils.isEmptyOrNull(user.getMobile())){
    		throw BizException.USER_NOT_BIND_MOBILE;
    	}
    	Integer type = params.getInt("type");
    	StatusObjDto<OpenDto> recieveResult = l2RecieveBiz.recieve(type, user.getUserId(), user.getMobile(),params.getClientIp());
		if(!recieveResult.isOk()) {
			return new ResultDto(recieveResult.getCode(),recieveResult.getMsg());
		}
    	
    	return new ResultDto(0,BeanUtils.beanToMap(recieveResult.getObj()),"");
	}
    
    @Signature(true)
	public ResultDto sendcheckcode(RequestParams params) {
    	String mobile = (String)params.get("mobile");
		StatusDto result = l2RecieveBiz.sendCheckCode(mobile);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	
    	return new ResultDto(0,"");
	}
    
    @Signature(true)
	public ResultDto acceptinvitation(RequestParams params) {
    	String mobile = (String)params.get("mobile");
    	String checkCode = (String)params.get("checkCode");
    	String shareCode = (String)params.get("shareCode");
    	StatusDto result = l2RecieveBiz.acceptInvitation(mobile, checkCode, shareCode);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	
    	return new ResultDto(0,"");
	}
    
    @Signature(false)
	public ResultDto openforinvite(RequestParams params) {
		l2RecieveBiz.openInvite();
    	return new ResultDto(0,"调用成功");
	}
    
    @Signature(false)
	public ResultDto openfornotactive(RequestParams params) {
		l2RecieveBiz.openNotActive();
    	return new ResultDto(0,"调用成功");
	}
}
