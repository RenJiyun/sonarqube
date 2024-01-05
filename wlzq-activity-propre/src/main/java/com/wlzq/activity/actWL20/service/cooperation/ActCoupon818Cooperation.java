package com.wlzq.activity.actWL20.service.cooperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.actWL20.biz.FundinGoBiz;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;

/**
 * ActCoupon818Cooperation
 * @author jjw
 *
 */
@Service("activity.actcoupon818cooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION,ApiServiceTypeEnum.APP})
public class ActCoupon818Cooperation{

    @Autowired
    private FundinGoBiz fundinGoBiz;

    /*
     * 更新入金信息
     */
    @Signature(false)
   	public ResultDto updatefundingo(RequestParams params) {
		return fundinGoBiz.updateFundinGo();
   	}
}
