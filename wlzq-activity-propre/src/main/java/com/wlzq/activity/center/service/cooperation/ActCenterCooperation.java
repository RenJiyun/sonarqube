package com.wlzq.activity.center.service.cooperation;

import com.wlzq.activity.center.biz.ActivityCenterBiz;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: qiaofeng
 * @date: 2022/3/21 11:27
 * @description: 活动中心
 */
@Service("activity.centercooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION, ApiServiceTypeEnum.APP})
public class ActCenterCooperation {

    @Autowired
    private ActivityCenterBiz activityCenterBiz;

    @Signature(false)
    public ResultDto delcache(RequestParams params, AccTokenUser user, Customer customer) {

        StatusDto result = activityCenterBiz.delCache();

        return new ResultDto(result.getCode());
    }
}
