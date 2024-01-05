package com.wlzq.activity.actWL20.service.app;

import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 万联20周年活动-【投顾产品5折抢】
 *
 * @author jjw
 */
@Service("activity.actcoupon5")
public class ActCoupon5Service extends BaseService {

    @Autowired
    private CouponCommonReceiveBiz couponCommonReceiveBiz;

    @Signature(true)
    @CustomerMustLogin(true)
    public ResultDto receive(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = (String) params.get("activityCode");
        String prizeType = (String) params.get("prizeType");
        String userId = user == null ? null : user.getUserId();
        String openId = user == null ? null : user.getOpenid();
        String recommendCode = (String) params.get("recommendCode");

        StatusObjDto<CouponRecieveStatusDto> result = couponCommonReceiveBiz.receive(activityCode, prizeType, userId, openId, customer.getCustomerId(), recommendCode, null);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        ResultDto back = new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
        return back;
    }
}
