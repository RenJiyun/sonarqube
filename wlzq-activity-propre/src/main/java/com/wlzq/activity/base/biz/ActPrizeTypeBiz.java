package com.wlzq.activity.base.biz;

import com.wlzq.activity.base.cons.PriceTimesTypeEnum;
import com.wlzq.activity.base.cons.PrizeReceiveStatusEnum;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.core.dto.StatusDto;

import java.util.List;
import java.util.Map;

/**
 * 活动奖品类型业务类
 *
 * @author wlzq
 */
public interface ActPrizeTypeBiz {

    /**
     * 创建奖品类型
     *
     * @param prizeTypeMap
     * @return
     */
    StatusDto createPrizeType(List<Map<String, Object>> prizeTypeMap);


    /**
     * 初始化奖品池
     *
     * @param activityCode
     * @param prizeType
     */
    void initPrizes(String activityCode, ActPrizeType prizeType);


    /**
     * 获取奖品类型
     *
     * @param prizeTypeCode
     * @return
     */
    ActPrizeType getPrizeType(String prizeTypeCode);


    /**
     * 从奖品池中获取一个可用奖品
     * <p>
     * 在调用之前需先初始化奖品池: {@link ActPrizeTypeBiz#initPrizes(String, ActPrizeType)}
     * <p>
     * 若返回 null, 则表示奖品池已空
     *
     * @param activityCode 活动编码
     * @param prizeType    奖品类型
     * @return
     */
    ActPrize getOneAvailablePrize(String activityCode, ActPrizeType prizeType);


    /**
     * 获取奖品领取限制次数类型
     *
     * @param prizeType
     * @param defaultType
     * @return
     */
    PriceTimesTypeEnum getPrizeTimesType(ActPrizeType prizeType, PriceTimesTypeEnum defaultType);

    /**
     * 获取领取状态
     * <p>
     * 如果当前状态下还能领取, 则返回 {@link PrizeReceiveStatusEnum#NOT_RECEIVED}, 否则返回 {@link PrizeReceiveStatusEnum#RECEIVED}
     *
     * @param prizeType   奖品类型
     * @param prizeList   当前已经领取的奖品列表
     * @param activity    活动
     * @param queryStatus
     * @return
     */
    PrizeReceiveStatusEnum getReceiveStatus(ActPrizeType prizeType, List<ActPrize> prizeList, Activity activity, boolean queryStatus);


    /**
     * 是否可以领取指定的奖品
     *
     * @param prizeType         奖品类型
     * @param receivedPrizeList 当前已经领取的奖品列表
     * @param activity          活动
     * @param queryStatus       是否查询领取状态
     * @param queryStatus
     * @return
     */
    boolean canReceive(ActPrizeType prizeType, List<ActPrize> receivedPrizeList, Activity activity, boolean queryStatus);
}
