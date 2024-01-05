package com.wlzq.activity.center.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.center.biz.ActivityCenterBiz;
import com.wlzq.activity.center.dto.ActCenterDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author: qiaofeng
 * @date: 2022/3/18 9:30
 * @description: 活动中心
 */
@Service("activity.center")
public class ActivityCenterService extends BaseService {

    @Autowired
    private ActivityCenterBiz activityCenterBiz;

    @Signature(true)
    public ResultDto listactivity(RequestParams params, AccTokenUser user, Customer customer) {
        /*客户端入口，用于查询活动是否可见*/
        Integer cientType = getCientType(params);

        StatusObjDto<List<ActCenterDto>> result = activityCenterBiz.listActivity(cientType);

        Map<String, Object> data = Maps.newHashMap();
        data.put("info", result.getObj());

        return new ResultDto(result.getCode(), data, result.getMsg());
    }
}