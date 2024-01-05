
package com.wlzq.activity.couponreceive.service.app;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.couponreceive.biz.CouponRecieveBiz;
import com.wlzq.activity.couponreceive.dto.CouponsDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
/**
 * CommonService服务类
 * @author 
 * @version 1.0
 */
@Service("activity.couponrecieve")
public class CoponRecieveService extends BaseService {
	
    @Autowired
    private CouponRecieveBiz coponRecieveBiz;

    @Signature(true)
	public ResultDto coupons(RequestParams params,AccTokenUser user,Customer customer) {
		Long time =  params.getLong("timestamp");
    	if(time == null) {
    		throw BizException.COMMON_PARAMS_NOT_NULL.format("timestamp");
    	}
		String customerId = ObjectUtils.isNotEmptyOrNull(customer)?customer.getCustomerId():null;
    	StatusObjDto<Map<Integer,CouponsDto>> result = coponRecieveBiz.coupons(customerId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	for(Integer key:result.getObj().keySet()) {
    		data.put(key.toString(), result.getObj().get(key));
    	}
    	return new ResultDto(0,data,"");
	}

    @Signature(true)
    @CustomerMustLogin(true) 
	public ResultDto recieve(RequestParams params,AccTokenUser user,Customer customer) {
		String couponId =  params.getString("couponId");
		String userId =  user == null?"":user.getUserId();
    	StatusObjDto<Integer> result = coponRecieveBiz.recieve(couponId,customer.getCustomerId(),userId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object>  data = Maps.newHashMap();
    	data.put("status", result.getObj());
    	return new ResultDto(0,data,"");
	}

}
