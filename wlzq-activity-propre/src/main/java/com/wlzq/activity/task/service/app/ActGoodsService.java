package com.wlzq.activity.task.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.activity.task.biz.ActGoodsBiz;
import com.wlzq.activity.task.dto.ActGoodsRecordInfoDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.*;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wlzq.activity.base.redis.CouponCommonRedis.COUPON_REDEEM_LOCK;

/**
 * @author renjiyun
 */
@Service("activity.goods")
@ApiServiceType({ApiServiceTypeEnum.APP})
@Slf4j
public class ActGoodsService {
    @Autowired
    private ActGoodsBiz actGoodsBiz;


    /**
     * 获取用户活动物品信息
     *
     * @param activityCode  | 活动编码 |  | required
     * @param goodsCode     | 物品编码 |  | required
     * @param includeDetail | 是否包含物品流水信息: 0-不包含(默认), 1-包含 |  | non-required |
     * @param pageIndex     | 页码 | | non-required
     * @param pageSize      | 条数 | | non-required
     * @return com.wlzq.activity.task.dto.ActGoodsRecordInfoDto
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto recordinfo(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        String goodsCode = params.getString("goodsCode");
        if (ObjectUtils.isEmptyOrNull(goodsCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("goodsCode");
        }
        boolean includeDetail = "1".equals(params.getString("includeDetail"));

        Integer pageIndex = params.getString("pageIndex") == null ? null : Integer.parseInt(params.getString("pageIndex"));
        Integer pageSize = params.getString("pageSize") == null ? null : Integer.parseInt(params.getString("pageSize"));
        StatusObjDto<ActGoodsRecordInfoDto> result = actGoodsBiz.recordInfo(activityCode, goodsCode,
                includeDetail, user, customer, pageIndex, pageSize);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("info", result.getObj());
        return new ResultDto(0, data, "");
    }


    /**
     * 活动物品兑换奖品
     *
     * @param activityCode | 活动编码 |  | required
     * @param prizeType    | 奖品编码 |  | required
     * @param goodsCode    | 用于兑换的物品编码 |  | required
     * @return com.wlzq.activity.base.dto.CouponReceiveStatusDto
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto redeem(RequestParams params, AccTokenUser user, Customer customer) {
        // 目前该活动编码应该是: ACTIVITY.2023DOUBLE11.DHL
        String activityCode = params.getString("activityCode");
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        String prizeType = params.getString("prizeType");
        if (ObjectUtils.isEmptyOrNull(prizeType)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
        }
        String goodsCode = params.getString("goodsCode");
        if (ObjectUtils.isEmptyOrNull(goodsCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("goodsCode");
        }

        Long currentThreadId = Thread.currentThread().getId();
        // 以用户手机号维度加锁
        boolean success = COUPON_REDEEM_LOCK.setNXEX(user.getMobile(), currentThreadId);
        log.info("兑换券: lock {}", user.getMobile());
        if (!success) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("兑换中, 请稍后再试");
        }

        StatusObjDto<List<CouponReceiveStatusDto>> result;
        try {
            result = actGoodsBiz.redeem(activityCode, prizeType, goodsCode, user, customer);
        } finally {
            if (currentThreadId.equals(COUPON_REDEEM_LOCK.get(user.getMobile()))) {
                COUPON_REDEEM_LOCK.del(user.getMobile());
                log.info("兑换券: unlock {}", user.getMobile());
            }
        }

        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }

        Map<String, Object> data = Maps.newHashMap();
        data.put("info", result.getObj());
        return new ResultDto(0, data, "");
    }
}
