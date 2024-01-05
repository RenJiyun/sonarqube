package com.wlzq.activity.base.biz;

import com.wlzq.activity.base.dto.*;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

/**
 * Level2业务接口
 *
 * @author louie
 */
public interface CouponCommonReceiveBiz {

    /**
     * 发现领券活动
     *
     * @param plate       平台，1：投顾平台，2：理财平台
     * @param productCode 产品代码
     * @return
     */
    public StatusObjDto<List<CouponRecieveActivityDto>> findActivities(Integer plate, String productCode);


    /**
     * 领取新客大礼包
     *
     * @param acReceivePriceVO
     * @param customer
     * @return
     */
    List<CouponRecieveStatusDto> receiveNewCustomerGiftBag(AcReceivePriceVO acReceivePriceVO, Customer customer);

    /**
     * 领取礼包
     *
     * @param acReceivePriceVO
     * @param customer
     * @return
     */
    StatusObjDto<List<CouponRecieveStatusDto>> receiveGiftBag(AcReceivePriceVO acReceivePriceVO, Customer customer);

    /**
     * 不用登录客户号就可以领取的奖品编码
     *
     * @param prizeType
     * @return
     */
    boolean isNoNeedCustomerLoginPrizeType(String prizeType);

    /**
     * 领取活动奖品
     *
     * @param acReceivePriceVO
     * @return
     */
    List<CouponRecieveStatusDto> receivePriceCommon(AcReceivePriceVO acReceivePriceVO);


    /**
     * 领取奖品
     *
     * @param acReceivePriceVO
     * @return
     */
    StatusObjDto<List<PrizeReceiveSimpDto>> receivePriceCoop(AcReceivePriceVO acReceivePriceVO);

    /**
     * 领取优惠券（即领取活动奖品）
     *
     * @param activityCode
     * @param prizeType
     * @param userId
     * @param openId
     * @param customerId
     * @param recommendCode
     * @param remark
     * @return
     */
    StatusObjDto<CouponRecieveStatusDto> receive(String activityCode, String prizeType, String userId, String openId,
                                                 String customerId, String recommendCode, String remark);

    /**
     * 领取优惠券（即领取活动奖品）
     *
     * @param activityCode
     * @param prizeType
     * @param userId
     * @param openId
     * @param customerId
     * @param recommendCode
     * @param remark
     * @param mobile
     * @return
     */
    StatusObjDto<CouponRecieveStatusDto> receive(String activityCode, String prizeType, String userId, String openId,
                                                 String customerId, String recommendCode, String remark, String mobile);


    /**
     * 领取优惠券
     *
     * @param activityCode
     * @param prizeType
     * @param userId
     * @param openId
     * @param customerId
     * @param recommendCode
     * @param remark
     * @return
     */
    StatusObjDto<CouponRecieveStatusDto> receiveByUserId(String activityCode, String prizeType, String userId, String openId,
                                                         String customerId, String recommendCode, String remark);


    /**
     * 领取状态
     *
     * @param acReceivePriceVO TODO
     * @return
     */
    public StatusObjDto<List<CouponRecieveStatusDto>> status(AcReceivePriceVO acReceivePriceVO);

    /**
     * 领取优惠券
     *
     * @param activityCode
     * @param prizeType
     * @param userId
     * @param openId
     * @param customerId
     * @return
     */
    public StatusObjDto<Integer> sectionReceive(String activityCode, String prizeType, String userId, String openId,
                                                String customerId);

    /**
     * 发券到客户号
     *
     * @param customerId
     * @param activityCode
     * @param prizeType
     * @return
     */
    StatusDto receiveToCustomer(String customerId, String activityCode, String prizeType);


    /**
     * 用户优惠券领取情况
     *
     * @param activity
     * @param prizeTypeCodes
     * @param user
     * @param customer
     * @param dimension
     * @param startDateStr
     * @param endDateStr
     * @return
     */
    StatusObjDto<List<CouponReceiveStatusDto>> userReceiveCouponStatus(Activity activity, String[] prizeTypeCodes,
                                                                       AccTokenUser user, Customer customer,
                                                                       Integer dimension, String startDateStr, String endDateStr);

    /**
     * 领取优惠券
     *
     * @param activity
     * @param prizeTypeCodes
     * @param receivedPrizeList
     * @param user
     * @param customer
     * @return
     */
    List<CouponReceiveStatusDto> receiveCoupon(Activity activity, String[] prizeTypeCodes,
                                               List<ActPrize> receivedPrizeList,
                                               AccTokenUser user, Customer customer);


    /**
     * 客户领取优惠券
     *
     * @param activity
     * @param prizeTypeList
     * @param receivedPrizeList
     * @param user
     * @param customer
     * @param remark
     * @return
     */
    List<CouponReceiveStatusDto> receiveCoupon(Activity activity, List<ActPrizeType> prizeTypeList,
                                               List<ActPrize> receivedPrizeList,
                                               AccTokenUser user, Customer customer, String remark);

    /**
     * 设置新客理财券信息
     *
     * @param acReceivePriceVO
     * @return
     */
    StatusObjDto<List<CouponRecieveStatusDto>> getNewCustomerCouponStatus(AcReceivePriceVO acReceivePriceVO);

}
