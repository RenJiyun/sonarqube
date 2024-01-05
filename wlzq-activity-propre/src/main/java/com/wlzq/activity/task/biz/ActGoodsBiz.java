package com.wlzq.activity.task.biz;

import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.activity.task.dto.ActGoodsRecordInfoDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

/**
 * @author renjiyun
 */
public interface ActGoodsBiz {

    /**
     * 获取用户活动物品信息
     *
     * @param activityCode
     * @param goodsCode
     * @param includeDetail
     * @param user
     * @param customer
     * @param pageIndex
     * @param pageSize
     * @return
     */
    StatusObjDto<ActGoodsRecordInfoDto> recordInfo(String activityCode, String goodsCode, boolean includeDetail,
                                                   AccTokenUser user, Customer customer, Integer pageIndex, Integer pageSize);

    /**
     * 活动物品兑换奖品
     *
     * @param activityCode
     * @param prizeType
     * @param goodsCode
     * @param user
     * @param customer
     * @return
     */
    StatusObjDto<List<CouponReceiveStatusDto>> redeem(String activityCode, String prizeType, String goodsCode,
                                                      AccTokenUser user, Customer customer);

}
