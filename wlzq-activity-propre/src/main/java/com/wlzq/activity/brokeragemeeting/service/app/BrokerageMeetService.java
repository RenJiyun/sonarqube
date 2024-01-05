package com.wlzq.activity.brokeragemeeting.service.app;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.brokeragemeeting.biz.BrokerageMeetingBiz;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

@Service("activity.brokeragemeet")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION})
public class BrokerageMeetService extends BaseService {

	@Autowired
	private BrokerageMeetingBiz brokerageMeetingBiz;
	
	@Signature(true)
	public ResultDto clear(RequestParams params) {
		Integer switchType = params.getInt("switchType");
		StatusObjDto<Map<String, Object>> result = brokerageMeetingBiz.gameSwitch(switchType);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
		
	}
}
