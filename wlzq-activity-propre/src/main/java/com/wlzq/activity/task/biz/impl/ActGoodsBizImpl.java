package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.task.biz.ActGoodsBiz;
import com.wlzq.activity.task.dao.ActGoodsRecordDao;
import com.wlzq.activity.task.dto.ActGoodsRecordDto;
import com.wlzq.activity.task.dto.ActGoodsRecordInfoDto;
import com.wlzq.activity.task.model.ActGoodsRecord;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author renjiyun
 */
@Service
public class ActGoodsBizImpl implements ActGoodsBiz {

    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActPrizeDao actPrizeDao;
    @Autowired
    private ActPrizeTypeBiz actPrizeTypeBiz;
    @Autowired
    private CouponCommonReceiveBiz couponCommonReceiveBiz;
    @Autowired
    private ActGoodsRecordDao actGoodsRecordDao;

    @Override
    public StatusObjDto<ActGoodsRecordInfoDto> recordInfo(String activityCode, String goodsCode, boolean includeDetail,
                                                          AccTokenUser user, Customer customer,
                                                          Integer pageIndex, Integer pageSize) {

        ActGoodsRecordInfoDto actGoodsRecordInfoDto = new ActGoodsRecordInfoDto();
        actGoodsRecordInfoDto.setMobile(desensitizeMobile(user.getMobile()));
        List<ActGoodsRecord> actGoodsRecordList = getAllGoodsRecord(user.getMobile(), activityCode, goodsCode);
        if (CollectionUtil.isEmpty(actGoodsRecordList)) {
            return new StatusObjDto<>(true, actGoodsRecordInfoDto, StatusObjDto.SUCCESS, "success");
        }

        Long remainCount = actGoodsRecordList.stream()
                .map(e -> e.getFlag().equals(1) ? e.getGoodsQuantity() : -e.getGoodsQuantity())
                .reduce(0L, Long::sum);
        actGoodsRecordInfoDto.setRemainCount(remainCount);
        if (!includeDetail) {
            // 若不需要流水信息, 则直接返回
            return new StatusObjDto<>(true, actGoodsRecordInfoDto, StatusObjDto.SUCCESS, "success");
        }

        List<ActGoodsRecordDto> actGoodsRecordDtoList = actGoodsRecordList.stream()
                .map(e -> {
                    Long quantity = e.getFlag().equals(1) ? e.getGoodsQuantity() : -e.getGoodsQuantity();
                    return new ActGoodsRecordDto()
                            .setId(e.getId())
                            .setGoodsQuantity(quantity)
                            .setGoodsCode(e.getGoodsCode())
                            .setActivityCode(e.getActivityCode())
                            .setRemark(e.getRemark())
                            .setCreateTime(e.getCreateTime());
                }).collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(actGoodsRecordDtoList)) {
            if (pageIndex != null && pageSize != null) {
                int fromIndex = (pageIndex - 1) * pageSize;
                int toIndex = Math.min(pageIndex * pageSize, actGoodsRecordDtoList.size());
                if (fromIndex >= toIndex) {
                    fromIndex = toIndex;
                }
                actGoodsRecordDtoList = actGoodsRecordDtoList.subList(fromIndex, toIndex);
            }
        }
        actGoodsRecordInfoDto.setRecordList(actGoodsRecordDtoList);
        return new StatusObjDto<>(true, actGoodsRecordInfoDto, StatusObjDto.SUCCESS, "");
    }

    private List<ActGoodsRecord> getAllGoodsRecord(String mobile, String activityCode, String goodsCode) {
        ActGoodsRecord qryActGoodsRecord = new ActGoodsRecord()
                .setMobile(mobile)
                .setActivityCode(activityCode)
                .setGoodsCode(goodsCode);
        return actGoodsRecordDao.findList(qryActGoodsRecord);
    }

    @Override
    public StatusObjDto<List<CouponReceiveStatusDto>> redeem(String activityCode, String prizeType, String goodsCode,
                                                             AccTokenUser user, Customer customer) {
        // 校验活动是否有效
        StatusDto actCheckResult = activityBaseBiz.isValid(activityCode);
        if (!actCheckResult.isOk()) {
            return new StatusObjDto<>(false, null, actCheckResult.getCode(), actCheckResult.getMsg());
        }

        // 校验该客户号是否已经和其他手机号绑定参与过活动
        checkHaveBoundMobile(activityCode, prizeType, user, customer);
        // 校验该手机号是否已经和其他客户号绑定参与过活动
        checkHaveBoundCustomerId(activityCode, prizeType, user, customer);

        ActPrizeType actPrizeType = actPrizeTypeBiz.getPrizeType(prizeType);
        if (actPrizeType == null) {
            throw ActivityBizException.ACT_PRIZE_TYPE_NOT_EXIST;
        }
        if (actPrizeType.getPoint() == null) {
            throw ActivityBizException.ACT_PRIZE_POINT_NOT_SETTING;
        }

        // 校验物品总数是否充足
        // 所需的物品数量在这里指奖品类型中的 point 字段
        long actGoodsNumNeeded = actPrizeType.getPoint().longValue();
        long actGoodsNumRemain = getActGoodsNumRemain(activityCode, user.getMobile(), goodsCode);
        if (actGoodsNumRemain < actGoodsNumNeeded) {
            throw ActivityBizException.ACT_PRIZE_POINT_INSUFFICIENT;
        }

        Activity activity = activityBaseBiz.findActivity(activityCode);
        List<CouponReceiveStatusDto> couponReceiveStatusDtoList = doRedeem(activity, actPrizeType, user, customer);

        // 若兑换成功, 则新增物品消耗记录
        if (CollectionUtil.isNotEmpty(couponReceiveStatusDtoList)) {
            ActGoodsRecord actGoodsRecord = new ActGoodsRecord()
                    .setGoodsCode(goodsCode)
                    .setGoodsQuantity(actPrizeType.getPoint().longValue())
                    .setFlag(2)
                    .setMobile(user.getMobile())
                    .setCustomerId(customer.getCustomerId())
                    .setActivityCode(activityCode)
                    .setRemark("兑换" + actPrizeType.getName())
                    .setCreateTime(new Date());
            actGoodsRecordDao.insert(actGoodsRecord);
        }
        return new StatusObjDto<>(true, couponReceiveStatusDtoList);
    }

    private List<CouponReceiveStatusDto> doRedeem(Activity activity, ActPrizeType prizeType, AccTokenUser user, Customer customer) {
        ActPrize queryPrize = new ActPrize()
                .setActivityCode(activity.getCode())
                .setCustomerId(customer.getCustomerId())
                .setPriceTypes(new String[]{prizeType.getCode()});
        List<ActPrize> receivedPrizeList = actPrizeDao.getUserPrizeList(queryPrize);
        return couponCommonReceiveBiz.receiveCoupon(activity, new String[]{prizeType.getCode()}, receivedPrizeList, user, customer);
    }

    private long getActGoodsNumRemain(String activityCode, String mobile, String goodsCode) {
        List<ActGoodsRecord> actGoodsRecordList = getAllGoodsRecord(mobile, activityCode, goodsCode);
        if (CollectionUtil.isEmpty(actGoodsRecordList)) {
            return 0L;
        }
        return actGoodsRecordList.stream().map(e -> {
            return e.getFlag().equals(1) ? e.getGoodsQuantity() : -e.getGoodsQuantity();
        }).reduce(0L, Long::sum);
    }


    private void checkHaveBoundMobile(String activityCode, String prizeType, AccTokenUser user, Customer customer) {
        ActPrize qryActPrize = new ActPrize()
                .setActivityCode(activityCode)
                .setCustomerId(customer.getCustomerId());
        List<ActPrize> actPrizeList = actPrizeDao.findList(qryActPrize);
        Optional<ActPrize> optionalActPrize = actPrizeList.stream().findFirst();

        // 若此前的奖品记录中已经有了该客户号的记录, 但是用户不一致, 则抛出已绑定异常
        if (optionalActPrize.isPresent() && !Objects.equals(optionalActPrize.get().getUserId(), user.getUserId())) {
            String boundMobile = desensitizeMobile(optionalActPrize.get().getMobile());
            throw ActivityBizException.ACT_PRIZE_POINT_BOUND_MOBILE.format(boundMobile);
        }
    }

    private void checkHaveBoundCustomerId(String activityCode, String prizeType, AccTokenUser user, Customer customer) {
        ActPrize qryActPrize = new ActPrize()
                .setActivityCode(activityCode)
                .setUserId(user.getUserId());
        List<ActPrize> actPrizeList = actPrizeDao.findList(qryActPrize);
        Optional<ActPrize> optionalActPrize = actPrizeList.stream().findFirst();

        // 若此前的奖品记录中已经有了该用户的记录, 但是客户号不一致, 则抛出已绑定异常
        if (optionalActPrize.isPresent() && !Objects.equals(optionalActPrize.get().getCustomerId(), customer.getCustomerId())) {
            String boundCustomerId = desensitizeCustomerId(optionalActPrize.get().getCustomerId());
            throw ActivityBizException.ACT_PRIZE_POINT_BOUND_CUSTOMERID.format(boundCustomerId);
        }
    }

    private String desensitizeMobile(String mobile) {
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    private String desensitizeCustomerId(String customerId) {
        return customerId.replaceAll("(\\d{2})\\d{4}(\\d{2})", "$1****$2");
    }

}
