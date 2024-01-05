package com.wlzq.activity.stockcourse.service.cooperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.guess51.biz.GuessBiz;
import com.wlzq.activity.stockcourse.biz.StockCourseBiz;
import com.wlzq.activity.stockcourse.model.StockCourseUser;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
/**
 * StockCourseCooperation
 * @author zjt
 * @version 1.0
 */

@Service("activity.stockcoursecooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION,ApiServiceTypeEnum.APP})
public class StockCourseCooperation{
    @Autowired
    private StockCourseBiz stockCourseBiz;

    @Signature(false)
   	public ResultDto stockcoursewechatpush(RequestParams params) {
    	stockCourseBiz.stockCourseWechatPush();
       	return new ResultDto(0,"");
   	}
    
    @Signature(false)
   	public ResultDto stockcourseapppush(RequestParams params) {
    	stockCourseBiz.stockCourseAppPush();
       	return new ResultDto(0,"");
   	}

}
