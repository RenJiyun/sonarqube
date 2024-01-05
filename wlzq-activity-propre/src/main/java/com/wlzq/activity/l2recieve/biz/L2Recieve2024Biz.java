package com.wlzq.activity.l2recieve.biz;

import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

/**
 * @author renjiyun
 */
public interface L2Recieve2024Biz {

    /**
     * 领取
     *
     * @param prizeType
     * @param user
     * @param customer
     * @return
     */
    StatusObjDto<List<CouponReceiveStatusDto>> receive(String prizeType, AccTokenUser user, Customer customer);
}
