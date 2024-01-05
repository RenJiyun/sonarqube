package com.wlzq.activity.base.biz;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.wlzq.activity.base.biz.impl.CouponCommonReceiveBizImpl;
import com.wlzq.activity.base.dto.AcReceivePriceVO;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Coupon;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author luohc
 * @date 2023/6/21 9:38
 */
@Slf4j
@Service
public class NewCustomerGiftBagBiz {

    @Autowired
    private	ActPrizeBiz actPrizeBiz;
    @Autowired
    private CouponBiz couponBiz;


    public StatusObjDto<List<CouponRecieveStatusDto>> queryPrizeInfo(AcReceivePriceVO priceVO, Customer customer){
        List<AcReceivePriceVO> prizeCodeDtos = new ArrayList<>();
        String activityCode = priceVO.getActivityCode();
        String customerId = priceVO.getCustomerId();
        String userId = priceVO.getUserId();

        String changeablePrizeType = AppConfigUtils.get("activity.prize.new.customer.giftbag.changeable", CouponCommonReceiveBizImpl.NEW_CUSTOMER_VAS_PRIZE_TYPE);

        // 新客理财券: 全渠道, 客户维度
        AcReceivePriceVO priceVO1 = new AcReceivePriceVO().setCustomerId(customerId).setPrizeType(CouponCommonReceiveBizImpl.NEW_CUSTOMER_FINANCE_PRIZE_TYPE);
        AcReceivePriceVO priceV01New = new AcReceivePriceVO().setCustomerId(customerId).setPrizeType(CouponCommonReceiveBizImpl.NEW_CUSTOMER_FINANCE_PRIZE_TYPE_2023_09);

        // 30天免费L2: 活动渠道, 用户维度 || 客户维度
        AcReceivePriceVO priceVO2 = new AcReceivePriceVO().setActivityCode(activityCode)
                .setUserId(userId).setMobile(priceVO.getMobile()).setCustomerId(customerId)
                .setPrizeType(CouponCommonReceiveBizImpl.NEW_CUSTOMER_LEVEL2_PRIZE_TYPE)
                .setUniqueType(AcReceivePriceVO.UNIQUE_MOBILE_OR_CUSTOMER);

        // 14天免费投顾服务券: 活动渠道, 客户维度
        AcReceivePriceVO priceVO3 = new AcReceivePriceVO().setActivityCode(activityCode)
                .setUserId(userId).setMobile(priceVO.getMobile()).setCustomerId(customerId)
                .setPrizeType(CouponCommonReceiveBizImpl.NEW_CUSTOMER_INVESTMENT_PRIZE_TYPE)
                .setUniqueType(AcReceivePriceVO.UNIQUE_CUSTOMER);

        // 决策资讯7天免费体验券: 全渠道, 用户维度 || 客户维度
        AcReceivePriceVO priceVO4 = new AcReceivePriceVO()
                .setUserId(userId).setMobile(priceVO.getMobile()).setCustomerId(customerId)
                .setPrizeType(changeablePrizeType)
                .setUniqueType(AcReceivePriceVO.UNIQUE_MOBILE_OR_CUSTOMER);

        prizeCodeDtos.add(priceVO1);
        prizeCodeDtos.add(priceVO3);
        prizeCodeDtos.add(priceVO4);
        prizeCodeDtos.add(priceVO2);
        List<CouponRecieveStatusDto> result = null;

        Date now = new Date();
        Date secondStartDate = DateUtil.parse(
                AppConfigUtils.get("activity.prize.new.customer.giftbag.second", "2023-10-01"));

        if (now.after(secondStartDate)) {
            // 增加新理财券的查询
            prizeCodeDtos.add(priceV01New);
            result = queryPrizeInfo(prizeCodeDtos, customer);
            CouponRecieveStatusDto resultOfPriceV01 = result.get(0);
            if (resultOfPriceV01.getSendTime() == null) {
                result.remove(0);
            } else {
                // 若已经领取了旧的理财券, 则删除新理财券的查询结果
                result.remove(4);
            }
        } else {
            // 二期开始前, 则只查询旧有的券
            result = queryPrizeInfo(prizeCodeDtos, customer);
        }

        return new StatusObjDto<>(true, result,0,"");
    }


    public List<CouponRecieveStatusDto> queryPrizeInfo(List<AcReceivePriceVO> prizeCodeDtos, Customer customer){
        List<CouponRecieveStatusDto> statusDtos = new ArrayList<>();
        if(CollectionUtils.isEmpty(prizeCodeDtos)){
            return statusDtos;
        }

        for (AcReceivePriceVO priceVO : prizeCodeDtos) {
            List<ActPrize> prizes = actPrizeBiz.queryPrize(priceVO.getActivityCode(), priceVO.getCustomerId(),
                    priceVO.getUserId(),priceVO.getMobile(), priceVO.getPrizeType(), priceVO.getUniqueType());

            CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
            statusDto.setPrizeType(priceVO.getPrizeType());
            statusDto.setStatus(CouponRecieveStatusDto.STATUS_NOT_RECIEVED);
            statusDto.setOpenDate(customer.getOpenDate());
            statusDtos.add(statusDto);
            if (CollectionUtil.isNotEmpty(prizes)) {
                ActPrize actPrize = prizes.get(0);
                String redeemCode = actPrize.getRedeemCode();

                StatusObjDto<Map<String, Object>> couponDto = couponBiz.couponEntityInfo(redeemCode);
                if (!couponDto.isOk()) {
                    log.error("查询优惠券信息异常,{},{}", redeemCode, couponDto.getMsg());
                    throw BizException.COMMON_CUSTOMIZE_ERROR.format(couponDto.getMsg());
                }
                Coupon coupon = com.wlzq.common.utils.BeanUtils.mapToBean(couponDto.getObj(), Coupon.class);

                statusDto.setStatus(coupon.getStatus());
                statusDto.setSendTime(coupon.getSendTime());
                statusDto.setActivityCode(actPrize.getActivityCode());
                statusDto.setActivityName(actPrize.getActivityName());
            }
        }
        return statusDtos;
    }

}
