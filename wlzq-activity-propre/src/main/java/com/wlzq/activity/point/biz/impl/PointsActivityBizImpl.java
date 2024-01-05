package com.wlzq.activity.point.biz.impl;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.AcReceivePriceVO;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.point.biz.PointsActivityBiz;
import com.wlzq.activity.point.dto.PointsActivityReqDto;
import com.wlzq.common.model.account.PointRecord;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.common.account.PointBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PointsActivityBizImpl extends ActivityBaseBiz implements PointsActivityBiz {
    @Autowired
    private ActPrizeDao actPrizeDao;
    @Autowired
    private PointBiz pointBiz;
    @Autowired
    private CouponCommonReceiveBiz couponRecieveBiz;

    @Transactional
    @Override
    public StatusObjDto<Object> redeem(PointsActivityReqDto req) {
        String userId = req.getUserId();
        String mobile = req.getMobile();
        String customerId = req.getCustomerId();
        /*活动编码*/
        String activityCode = req.getActivityCode();
        /*奖品编码*/
        String prizeType = req.getPrizeType();
        /* 积分使用描述 */
        String description = req.getDescription();

        // 判断当前客户号是否与其它手机号绑定兑换过奖品
        ActPrize queryByCustomerId = new ActPrize()
                .setActivityCode(activityCode)
                .setCustomerId(customerId);
        /*按活动编码和customerId查奖品兑换记录*/
        List<ActPrize> dbActPrizes = actPrizeDao.findList(queryByCustomerId);
        Optional<ActPrize> first = dbActPrizes.stream().findFirst();
        if (first.isPresent() && !Objects.equals(first.get().getUserId(), userId)) {
            /*手机号脱敏*/
            String bound = first.get().getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");

            /*情况1：用户登录的客户号已经被其他手机号绑定对换过奖品*/
            HashMap<Object, Object> data = Maps.newHashMap();
            data.put("mobile", bound);
            return new StatusObjDto<>(false, data,
                    ActivityBizException.ACT_PRIZE_POINT_BOUND_MOBILE.getCode(),
                    ActivityBizException.ACT_PRIZE_POINT_BOUND_MOBILE.format(bound).getMsg());
        }
        ActPrize queryByUserId = new ActPrize()
                .setActivityCode(activityCode)
                .setUserId(userId);
        /*按活动编码和userId查奖品兑换记录*/
        dbActPrizes = actPrizeDao.findList(queryByUserId);
        first = dbActPrizes.stream().findFirst();
        if (first.isPresent() && !Objects.equals(first.get().getCustomerId(), customerId)) {
            /*客户号脱敏*/
            String bound = first.get().getCustomerId().replaceAll("(\\d{2})\\d{4}(\\d{2})", "$1****$2");

            /*情况2：用户此前已登录a客户号对换过奖品，这次又登陆了b客户号*/
            HashMap<Object, Object> data = Maps.newHashMap();
            data.put("customerId", bound);
            return new StatusObjDto<>(false, data,
                    ActivityBizException.ACT_PRIZE_POINT_BOUND_CUSTOMERID.getCode(),
                    ActivityBizException.ACT_PRIZE_POINT_BOUND_CUSTOMERID.format(bound).getMsg());
        }

        // 判断积分是否足够兑换奖品
        ActPrizeType actPrizeType = findPrizeType(prizeType);
        if (actPrizeType.getPoint() == null) {
            throw ActivityBizException.ACT_PRIZE_POINT_NOT_SETTING;
        }
        /*奖品的积分价值*/
        Long prizePoint = actPrizeType.getPoint().longValue();
        /*查总积分*/
        Long point = pointBiz.getPoint(userId, null).getObj();
        if (point < prizePoint) {
            throw ActivityBizException.ACT_PRIZE_POINT_INSUFFICIENT;
        }

        // 兑换奖品
        AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO()
                .setActivityCode(activityCode)
                .setUserId(userId)
                .setCustomerId(customerId)
                .setPrizeType(prizeType)
                .setMobile(mobile);
        List<CouponRecieveStatusDto> recieveStatusDtos = couponRecieveBiz.receivePriceCommon(acReceivePriceVO);

        // 扣减积分、积分记录
        StatusDto statusDto = pointBiz.addPoint(userId, prizePoint, PointRecord.SOURCE_POINT_PRIZE, PointRecord.FLOW_PLUS, description, activityCode);
        if (!statusDto.isOk()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(statusDto.getMsg());
        }

        return new StatusObjDto<>(true, recieveStatusDtos);
    }
}
