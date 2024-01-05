package com.wlzq.activity.guess.service.cooperation;

import com.wlzq.core.dto.StatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.guess.biz.GuesssBiz;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
/**
 * GuesssCooperation
 * @author
 * @version 1.0
 */
@Service("activity.guesscooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION,ApiServiceTypeEnum.APP})
public class GuesssCooperation{

    @Autowired
    private GuesssBiz guessBiz;

   /**
	* 指数获取
	*/
	@Signature(false)
	public ResultDto getindex(RequestParams params) {
		/*指数类型：1-上证指数， 2-沪深300指数*/
		Integer type = params.getSysInt("type");

		StatusDto status = guessBiz.getIndex(type);

		return new ResultDto(0,"");
	}

	/**
	 * 积分结算
	 */
    @Signature(false)
   	public ResultDto settle(RequestParams params) {
		String activityCode = ObjectUtils.isEmptyOrNull(params.getSysString("activityCode")) ? "ACTIVITY.SPRING2021.GUESS" : params.getSysString("activityCode");
   		String date = params.getString("date");
		Integer type = params.getSysInt("type");
    	guessBiz.settle(date, activityCode, type);
       	return new ResultDto(0,"");
   	}
}
