package com.wlzq.activity.returnvisit.biz.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.returnvisit.biz.ReturnVisitActBiz;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.remote.service.utils.RemoteUtils;

@Service
public class ReturnVisitActBizImpl implements ReturnVisitActBiz {

	@Override
	public int getAvailableDrawsCount(String customerId, String source, String businessType) {
		Map<String, Object> busParams = Maps.newHashMap();
		busParams.put("customerId", customerId);
		busParams.put("source", source);
		busParams.put("businessType", businessType);
		ResultDto result = RemoteUtils.call("business.returnvisitcooperation.getavailabledrawscount", ApiServiceTypeEnum.COOPERATION, busParams, true);
		if (result != null && result.getCode().intValue() == 0 && result.getData() != null && result.getData().size() > 0) {
			Integer count = (Integer) result.getData().get("count");
			if (count != null) {
				return count.intValue();
			}
		}

		return 0;
	}

	@Override
	public int updateDraws(String customerId, String source, String businessType) {
		Map<String, Object> busParams = Maps.newHashMap();
		busParams.put("customerId", customerId);
		busParams.put("source", source);
		busParams.put("businessType", businessType);
		ResultDto result = RemoteUtils.call("business.returnvisitcooperation.usedrawchance", ApiServiceTypeEnum.COOPERATION, busParams, true);
		if (result != null && result.getCode().intValue() == 0 && result.getData() != null && result.getData().size() > 0) {
			Integer r = (Integer) result.getData().get("r");
			if (r != null) {
				return r.intValue();
			}
		}

		return -1;
	}

}
