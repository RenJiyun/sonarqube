package com.wlzq.activity.card.biz;

import cn.hutool.core.collection.CollectionUtil;
import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.cons.PrizeReceiveStatusEnum;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.card.dto.CardRedemptionStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardRedemptionBiz {

    @Autowired
    private ActPrizeDao actPrizeDao;
    @Autowired
    private CouponCommonReceiveBiz couponCommonRecieveBiz;
    @Autowired
    private ActPrizeTypeBiz actPrizeTypeBiz;

    public CardRedemptionStatusDto redeem(Activity activity, ActPrizeType prizeType, List<Long> cardIdList,
                                          AccTokenUser user, Customer customer) {


        CardRedemptionStatusDto result = new CardRedemptionStatusDto();

        // 获取已经兑换的奖品
        ActPrize queryPrize = new ActPrize()
                .setActivityCode(activity.getCode())
                .setCustomerId(customer.getCustomerId())
                .setCode(prizeType.getCode());
        List<ActPrize> receivedPrizeList = actPrizeDao.findList(queryPrize);

        if (actPrizeTypeBiz.getReceiveStatus(prizeType, receivedPrizeList, activity, false) == PrizeReceiveStatusEnum.RECEIVED) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("该奖品已经兑换过");
        }

        int cardNeeded = getNeededCardCount(prizeType);

        List<ActPrize> availableCardList = getAvailableCardList(activity.getCode(), prizeType, cardIdList, user);

        if (CollectionUtil.isEmpty(availableCardList) || availableCardList.size() < cardNeeded) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("卡牌数量不足");
        }


        // 每种卡牌类型只取一张
        availableCardList = availableCardList.stream()
                .collect(Collectors.groupingBy(ActPrize::getCode))
                .values().stream().map(actPrizes -> actPrizes.get(0)).collect(Collectors.toList());

        if (availableCardList.size() < cardNeeded) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("卡牌数量不足");
        }

        availableCardList = availableCardList.subList(0, cardNeeded);

        Date now = new Date();
        List<Long> usedCardIdList = new ArrayList<>();
        for (int i = 0; i < cardNeeded; i++) {
            ActPrize card = availableCardList.get(i);
            usedCardIdList.add(card.getId());
        }

        // 将使用的卡牌 id 记录到所兑换的券的备注字段上
        String remark = usedCardIdList.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        // 发券
        List<ActPrizeType> actPrizeTypeList = new ArrayList<>();
        actPrizeTypeList.add(prizeType);
        List<CouponReceiveStatusDto> couponReceiveStatusDtoList =
                couponCommonRecieveBiz.receiveCoupon(activity, actPrizeTypeList, receivedPrizeList, user, customer, remark);

        result.setUsedCardIds(usedCardIdList);
        CouponReceiveStatusDto couponReceiveStatusDto = couponReceiveStatusDtoList.get(0);
        result.setCode(couponReceiveStatusDto.getPrize().getRedeemCode());
        result.setName(couponReceiveStatusDto.getPrize().getName());
        result.setId(couponReceiveStatusDto.getPrize().getId());

        // 兑换完之后将卡牌状态改为已使用
        for (int i = 0; i < cardNeeded; i++) {
            ActPrize card = availableCardList.get(i);
            card.setStatus(ActPrize.STATUS_USED);
            card.setUpdateTime(now);
            actPrizeDao.update(card);
        }
        return result;
    }

    private List<ActPrize> getAvailableCardList(String activityCode, ActPrizeType prizeType,
                                                List<Long> cardIdList, AccTokenUser user) {
        // 这里必须按用户维度查询, 因为抽卡牌是不需要登录客户号的
        ActPrize queryPrize = new ActPrize()
                .setUserId(user.getUserId())
                .setIds(cardIdList)
                .setStatus(ActPrize.STATUS_SEND);

        return actPrizeDao.getUserPrizeList(queryPrize);
    }

    /**
     * 兑换券需要的卡牌数量
     */
    private int getNeededCardCount(ActPrizeType prizeType) {
        if ("PRIZE.COUPON.ALV2.2023818".equals(prizeType.getCode())) {
            // 	A股 level-2 一个月
            return 4;
        } else if ("PRIZE.COUPON.XTJJDS.2023818".equals(prizeType.getCode())) {
            // 形态掘金大师一个月
            return 5;
        } else if ("PRIZE.COUPON.SSS818.2023818".equals(prizeType.getCode())) {
            // 聚利宝28天期6.xx%理财券
            return 6;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
