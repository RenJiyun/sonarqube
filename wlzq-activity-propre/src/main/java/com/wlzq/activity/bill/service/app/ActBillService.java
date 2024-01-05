package com.wlzq.activity.bill.service.app;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.bill.biz.ActBillBiz;
import com.wlzq.activity.bill.dto.ActBillDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 2022年度账单
 * @author jjw
 *
 */
@Service("activity.bill")
public class ActBillService extends BaseService {
	@Autowired
	private ActBillBiz actBillBiz; 

	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto view(RequestParams params, AccTokenUser user,Customer customer) {
		StatusObjDto<ActBillDto> result = actBillBiz.view(user, customer);
		if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto wish(RequestParams params, AccTokenUser user,Customer customer) {
		String wish = (String) params.get("wish");
		return actBillBiz.wish(user, customer, wish);
	}
	
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto share(RequestParams params, AccTokenUser user,Customer customer) {
		return actBillBiz.share(user, customer);
	}
}
