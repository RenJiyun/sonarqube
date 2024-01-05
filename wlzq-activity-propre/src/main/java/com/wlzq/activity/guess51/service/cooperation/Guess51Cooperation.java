package com.wlzq.activity.guess51.service.cooperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.guess51.biz.GuessBiz;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
/**
 * StaffService
 * @author 
 * @version 1.0
 */

@Service("activity.guess51cooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION,ApiServiceTypeEnum.APP})
public class Guess51Cooperation{

    @Autowired
    private GuessBiz guessBiz;

    @Signature(false)
   	public ResultDto setopenindex(RequestParams params) {
   		guessBiz.getOpenIndex();
       	return new ResultDto(0,"");
   	}

    @Signature(false)
   	public ResultDto setmorningcloseindex(RequestParams params) {
   		guessBiz.getMorningCloseIndex();
       	return new ResultDto(0,"");
   	}

    @Signature(false)
   	public ResultDto setcloseindex(RequestParams params) {
   		guessBiz.getCloseIndex();
       	return new ResultDto(0,"");
   	}

    @Signature(false)
   	public ResultDto settle(RequestParams params) {
   		String date = params.getString("date");
   		Integer guessNo = params.getInt("guessNo");
   		if(ObjectUtils.isEmptyOrNull(guessNo)) {
   			guessNo = params.getSysInt("guessNo");
   		}
    	guessBiz.settle(date,guessNo);
       	return new ResultDto(0,"");
   	}
}
