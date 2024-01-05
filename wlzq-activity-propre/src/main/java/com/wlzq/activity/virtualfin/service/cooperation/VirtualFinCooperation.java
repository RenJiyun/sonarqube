
package com.wlzq.activity.virtualfin.service.cooperation;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.virtualfin.biz.ActFinOrderBiz;
import com.wlzq.activity.virtualfin.model.ActFinOrder;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
/**
 * 作品投票服务类
 * @author 
 * @version 1.0
 */
@Service("activity.virtualfincooperation")
@ApiServiceType(ApiServiceTypeEnum.COOPERATION)
public class VirtualFinCooperation extends BaseService{
	
	@Autowired
	private ActFinOrderBiz finOrderBiz;
 
    @Signature(false)
	public ResultDto updstatus(RequestParams params,AccTokenUser user,Customer customer) {
   		StatusObjDto<List<ActFinOrder>> result = finOrderBiz.updStaus();
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
   		Map<String, Object> data = Maps.newHashMap();
   		data.put("info", result.getObj().size());
   		data.put("data", result.getObj());
    	return new ResultDto(0,data,"");
	}
  

}
