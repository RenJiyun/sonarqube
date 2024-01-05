
package com.wlzq.activity.base.service.cooperation;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
/**
 * CommonService服务类
 * @author 
 * @version 1.0
 */
@Service("activity.prizetypecooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION})
public class ActPrizeTypeCooperation extends BaseService {
	
    @Autowired
    private ActPrizeTypeBiz actPrizeTypeBiz;
    
	
	@Signature(true) 
	public ResultDto create(RequestParams params) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> prizeTypeMap = (List<Map<String, Object>>)params.get("data");
		StatusDto result = actPrizeTypeBiz.createPrizeType(prizeTypeMap);
		Map<String, Object> data = Maps.newHashMap();
		return new ResultDto(result.getCode(), data, result.getMsg());
	}
	
}
