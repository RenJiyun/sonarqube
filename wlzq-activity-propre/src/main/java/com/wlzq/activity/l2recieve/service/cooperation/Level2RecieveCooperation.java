package com.wlzq.activity.l2recieve.service.cooperation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.l2recieve.biz.L2RecieveBiz;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 产品协作服务类
 * 
 * @author
 * @version 1.0
 */
@Service("activity.l2recievecooperation")
@ApiServiceType({ ApiServiceTypeEnum.COOPERATION ,ApiServiceTypeEnum.APP})
public class Level2RecieveCooperation extends BaseService {
	
	@Autowired
    private L2RecieveBiz l2RecieveBiz;

    @Signature(true)
	public ResultDto recieve(RequestParams params) {
    	String userId = params.getString("userId");
		String mobile = params.getString("mobile");
		StatusObjDto<Integer> recieveResult = l2RecieveBiz.hasRecieve(userId,mobile);
		if(!recieveResult.isOk()) {
			return new ResultDto(recieveResult.getCode(),recieveResult.getMsg());
		}
    	
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("popupStatus", recieveResult.getObj());
    	return new ResultDto(0,data,"");
	}

    @Signature(false)
	public ResultDto cacherecievestatus(RequestParams params) {
		StatusDto recieveResult = l2RecieveBiz.cacheRecieveStatus();
		if(!recieveResult.isOk()) {
			return new ResultDto(recieveResult.getCode(),recieveResult.getMsg());
		}
    	return new ResultDto(0,"");
	}
    
}
