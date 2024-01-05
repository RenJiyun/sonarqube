
package com.wlzq.activity.redenvelope.service.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.redenvelope.biz.RedEnvelopeBiz;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
/**
 * RedEnvelopeCallback服务类
 * @author 
 * @version 1.0
 */
@Service("activity.redenvelopecallback")
@ApiServiceType(ApiServiceTypeEnum.CALLBACK)
public class RedEnvelopeCallback{
    @Autowired
    private RedEnvelopeBiz redEnvelopeBiz;
	
    @Signature(false)
	public ResultDto notify(RequestParams params) {
		String inputStr = params.getSysString("result");
		StatusDto notifyResult = redEnvelopeBiz.notify(inputStr);
    	ResultDto backResult = new ResultDto(notifyResult.getCode(),"");
    	backResult.setOutputText(backResult.getCode().toString());
    	return backResult;
	}
}
