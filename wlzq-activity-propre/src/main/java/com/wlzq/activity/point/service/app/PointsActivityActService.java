package com.wlzq.activity.point.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.point.biz.PointsActivityBiz;
import com.wlzq.activity.point.dto.PointsActivityReqDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.*;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 积分奖励、积分兑换活动相关服务
 */
@Service("activity.pointsactivity")
@ApiServiceType({ApiServiceTypeEnum.APP})
public class PointsActivityActService {

    @Autowired
    private PointsActivityBiz pointsActivityBiz;
    /**
     * 积分兑换奖品
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto redeem(RequestParams params, AccTokenUser user, Customer customer) {
        String userId = user.getUserId();
        String mobile = user.getMobile();
        String customerId = customer.getCustomerId();
        /*活动编码*/
        String activityCode = params.getString("activityCode");
        /*奖品编码*/
        String prizeType = params.getString("prizeType");
        /* 积分使用描述 */
        String description = params.getString("description");

        PointsActivityReqDto req = new PointsActivityReqDto().setUserId(userId)
                .setMobile(mobile)
                .setCustomerId(customerId)
                .setActivityCode(activityCode)
                .setPrizeType(prizeType)
                .setDescription(description);

        StatusObjDto<Object> result = pointsActivityBiz.redeem(req);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), BeanUtils.beanToMap(result.getObj()), result.getMsg());
        }

        Map<String, Object> data = Maps.newHashMap();
        data.put("list", result.getObj());
        return new ResultDto(0, data, "");
    }
}
