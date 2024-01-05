
package com.wlzq.activity.l2recieve.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.activity.l2recieve.biz.L2Recieve2024Biz;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author renjiyun
 */
@Service("activity.l2recieve2024")
public class Level2Recieve2024Service {
    @Autowired
    private L2Recieve2024Biz l2Recieve2024Biz;


    /**
     * 领取L2
     *
     * @param activityCode | 活动编码 |  | required
     * @param prizeType    | 奖品类型编码 (可上传多个, 用逗号隔开) |  | required
     * @return com.wlzq.activity.base.dto.CouponReceiveStatusDto
     * @cate 2024年L2领取活动
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto receive(RequestParams params, AccTokenUser user, Customer customer) {
        if (ObjectUtils.isEmptyOrNull(user.getMobile())) {
            throw BizException.USER_NOT_BIND_MOBILE;
        }
        String prizeType = params.getString("prizeType");
        if (ObjectUtils.isEmptyOrNull(prizeType)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }
        StatusObjDto<List<CouponReceiveStatusDto>> recieveResult = l2Recieve2024Biz.receive(
                prizeType, user, customer);
        if (!recieveResult.isOk()) {
            return new ResultDto(recieveResult.getCode(), recieveResult.getMsg());
        }

        Map<String,Object> data = Maps.newHashMap();
        data.put("total",recieveResult.getObj().size());
        data.put("info", recieveResult.getObj());
        return new ResultDto(0, data, "");
    }
}
