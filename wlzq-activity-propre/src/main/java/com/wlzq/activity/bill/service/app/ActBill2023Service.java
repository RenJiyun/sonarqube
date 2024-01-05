package com.wlzq.activity.bill.service.app;

import com.wlzq.activity.bill.biz.ActBill2023Biz;
import com.wlzq.activity.bill.dto.ActBill2023Dto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author renjiyun
 */
@Service("activity.bill2023")
public class ActBill2023Service extends BaseService {
    @Autowired
    private ActBill2023Biz actBill2023Biz;


    /**
     * 查询企微添加结果
     *
     * @cate 2023年度账单
     */
    @Signature(true)
    @CustomerMustLogin(true)
    public ResultDto chkaddresult(RequestParams params, AccTokenUser user, Customer customer) {
        Map<String, Object> result = new HashMap<>();
        boolean addResult = actBill2023Biz.chkAddResult(customer.getCustomerId());
        if (addResult) {
            result.put("result", 1);
        } else {
            result.put("result", 0);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("info", result);
        return new ResultDto(0, data, "");
    }


    /**
     * 领取登记
     *
     * @param mobile     | 手机号 |  | non-required
     * @cate 2023年度账单
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto receivereg(RequestParams params, AccTokenUser user, Customer customer) {
        String mobile = params.getString("mobile");
        mobile = ObjectUtils.isEmptyOrNull(mobile) ? user.getMobile() : mobile;

        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }

        Map<String, Object> result = new HashMap<>();
        boolean regResult = actBill2023Biz.receiveReg(user.getUserId(), mobile, customer.getCustomerId());
        if (regResult) {
            result.put("result", 1);
        } else {
            result.put("result", 0);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("info", result);
        return new ResultDto(0, data, "");
    }

    /**
     * 获取年度账单
     *
     * @return com.wlzq.activity.bill.dto.ActBill2023Dto
     * @cate 2023年度账单
     */
    @Signature(true)
    public ResultDto view(RequestParams params, AccTokenUser user, Customer customer) {
        ActBill2023Dto actBill2023Dto = actBill2023Biz.view(customer);
        Map<String, Object> data = new HashMap<>();
        data.put("info", actBill2023Dto);
        return new ResultDto(0, data, "");
    }
}
