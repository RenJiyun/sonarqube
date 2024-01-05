
package com.wlzq.activity.base.service.cooperation;

import java.util.List;
import java.util.Map;

import com.wlzq.activity.base.biz.ActPrizeMonitoringBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
/**
 * 服务类
 * @author 
 * @version 1.0
 */
@Service("activity.prizecooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION})
public class ActPrizeCooperation extends BaseService {
	
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
	private ActPrizeMonitoringBiz actPrizeMonitoringBiz;
	
	@Signature(true) 
	public ResultDto create(RequestParams params) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> prizeMap = (List<Map<String, Object>>)params.get("data");
		StatusDto result = actPrizeBiz.createPrize(prizeMap);
		Map<String, Object> data = Maps.newHashMap();
		return new ResultDto(result.getCode(), data, result.getMsg());
	}
	
	@Signature(true) 
	public ResultDto delredis(RequestParams params) {
		String redisKey = params.getString("redisKey");
		StatusDto result = actPrizeBiz.delRedis(redisKey);
		Map<String, Object> data = Maps.newHashMap();
		return new ResultDto(result.getCode(), data, result.getMsg());
	}
	/**
	 * 奖品剩余数量监控
	 */
	@Signature(false)
	public ResultDto monitoring(RequestParams params){
		StatusDto result = actPrizeMonitoringBiz.selectPrizeCount();
		return new ResultDto(result.getCode());
	}
}
