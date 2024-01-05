package com.wlzq.activity.returnvisit.service.app;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActLotteryBiz;
import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.base.model.ActLotteryEnum;
import com.wlzq.activity.returnvisit.biz.ReturnVisitActBiz;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wlzq
 */
@Service("activity.returnvisit")
public class ReturnVisitActService extends BaseService {

    @Autowired
    private ActLotteryBiz actLotteryBiz;
    @Autowired
    private ReturnVisitActBiz returnVisitActBiz;

    @Signature
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto lottery(RequestParams params, AccTokenUser user, Customer customer) {
        String customerId = customer.getCustomerId();
        String activityCode = params.getString("activityCode");
        String source = params.getString("taskNo");
        String businessType = params.getString("businessType");

        List<ActLotteryEnum> list = null;
        ActLotteryEnum backup = null;

        int count = returnVisitActBiz.getAvailableDrawsCount(customerId, source, businessType);

        if (count <= 0) {
            throw ActivityBizException.FINSECTION_ZERO_LOTTERY_COUNT;
        }

        StatusObjDto<LotteryDto> result = actLotteryBiz.lottery(list, activityCode, ObjectUtils.isEmptyOrNull(user) ? null : user.getUserId(),
                null, customerId, null, backup);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        returnVisitActBiz.updateDraws(customerId, source, businessType);
        return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
    }
}
