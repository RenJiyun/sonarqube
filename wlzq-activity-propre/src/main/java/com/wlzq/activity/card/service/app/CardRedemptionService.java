package com.wlzq.activity.card.service.app;


import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.card.biz.CardRedemptionBiz;
import com.wlzq.activity.card.dto.CardRedemptionStatusDto;
import com.wlzq.activity.card.redis.CardRedemptionRedis;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.*;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仅用于 2023 年 818 理财活动: 卡牌兑换理财券
 *
 * @author renjiyun
 */
@Service("activity.cardredemption")
@ApiServiceType({ApiServiceTypeEnum.APP})
@Slf4j
public class CardRedemptionService {

    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private ActPrizeTypeBiz actPrizeTypeBiz;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActPrizeDao actPrizeDao;
    @Autowired
    private CardRedemptionBiz cardRedemptionBiz;

    /**
     * 卡牌兑换理财券
     *
     * @param activityCode | 活动编码 |  | required
     * @param cardIds      | 卡牌 id (可上传多个, 用逗号隔开) |  | required
     * @param prizeType    | 奖品类型编码 |  | required
     * @return com.wlzq.activity.card.dto.CardRedemptionStatusDto
     * @cate 2023818理财活动
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto redeem(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = (String) params.get("activityCode");
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }

        String cardIds = (String) params.get("cardIds");
        if (ObjectUtils.isEmptyOrNull(cardIds)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("cardIds");
        }

        List<Long> cardIdList = Arrays.stream(cardIds.split(",")).filter(StringUtils::isNotBlank)
                .map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        if (cardIdList.isEmpty()) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("cardIds");
        }

        String prizeTypeCode = (String) params.get("prizeType");
        if (ObjectUtils.isEmptyOrNull(prizeTypeCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }

        ActPrizeType prizeType = actPrizeTypeBiz.getPrizeType(prizeTypeCode);
        if (prizeType == null) {
            throw ActivityBizException.ACT_PRIZE_TYPE_NOT_EXIST;
        }

        Activity activity = activityBaseBiz.findActivity(activityCode);
        StatusDto actValidResult = activityBaseBiz.isValid(activity);
        if (!actValidResult.isOk()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(actValidResult.getMsg());
        }

        // 对于理财券 (PRIZE.COUPON.SSS818.2023818) 的兑换, 应该在活动结束前一天就结束, 此规则很特殊, 需要留意
        if ("PRIZE.COUPON.SSS818.2023818".equals(prizeType.getCode())) {
            if (activity.getDateTo().getTime() - System.currentTimeMillis() < 24 * 60 * 60 * 1000) {
                throw BizException.COMMON_CUSTOMIZE_ERROR.format("该券的兑换时间已截止");
            }
        }

        // 增加简单的并发控制
        Long currentThreadId = Thread.currentThread().getId();
        boolean success = CardRedemptionRedis.CARD_REDEMPTION_LOCK.setNXEX(customer.getCustomerId(), currentThreadId);
        if (!success) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("兑换中, 请稍后再试");
        }

        log.info("卡牌兑换: lock {}", customer.getCustomerId());
        try {
            CardRedemptionStatusDto cardRedemptionStatusDto =
                    cardRedemptionBiz.redeem(activity, prizeType, cardIdList, user, customer);

            return new ResultDto(0, BeanUtils.beanToMap(cardRedemptionStatusDto), "");
        } finally {
            if (currentThreadId.equals(CardRedemptionRedis.CARD_REDEMPTION_LOCK.get(customer.getCustomerId()))) {
                CardRedemptionRedis.CARD_REDEMPTION_LOCK.del(customer.getCustomerId());
                log.info("卡牌兑换: unlock {}", customer.getCustomerId());
            }
        }
    }

}
