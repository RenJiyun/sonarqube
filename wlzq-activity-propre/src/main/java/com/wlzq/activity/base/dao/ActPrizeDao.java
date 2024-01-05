/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.dto.PrizeCustomerDto;
import com.wlzq.activity.base.dto.ShowPrizeDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.push.dto.NoticeDto;
import com.wlzq.activity.push.dto.RenewedReceivedNoticeDto;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 活动奖品管理DAO接口
 *
 * @author louie
 * @version 2018-05-25
 */
@MybatisScan
public interface ActPrizeDao extends CrudDao<ActPrize> {

    /**
     * 查询可用奖品
     *
     * @param activityCode 活动编码
     * @return
     */
    List<Long> findAvailablePrizes(String activityCode);

    /**
     * 查询可用奖品
     *
     * @param activityCode  活动编码
     * @param prizeTypeCode 奖品类型编码
     * @return
     */
    List<Long> findAvailablePrizesByType(@Param("activityCode") String activityCode, @Param("code") String prizeTypeCode);

    /**
     * 查询一个可用奖品
     *
     * @param activityCode  活动编码
     * @param prizeTypeCode 奖品类型编码
     * @return
     */
    ActPrize findOneAvailablePrize(@Param("activityCode") String activityCode, @Param("code") String prizeTypeCode);

    /**
     * 查询可用奖品
     *
     * @param code 奖品编码
     * @return
     */
    ActPrize findAvailablePrize(String code);

    /**
     * 兑换查询奖品
     *
     * @param redeemCode
     * @return
     */
    ActPrize findByRedeem(String redeemCode);

    /**
     * 查询用户/客户奖品
     *
     * @param prize
     * @return
     */
    List<ActPrize> findPrizes(ActPrize prize);

    /**
     * 查询客户奖品
     *
     * @param prize
     * @return
     */
    List<ActPrize> findCustomerPrizes(ActPrize prize);

    /**
     * 查询用户奖品，包括领取与未领取的
     *
     * @param prize
     * @return
     */
    List<ActPrize> findUserPrizes(ActPrize prize);

    /**
     * 查询用户奖品
     *
     * @param prize
     * @return
     */
    List<ActPrize> queryUserPrizes(ActPrize prize);

    /**
     * 查询客户奖品
     *
     * @param activityCode
     * @param start
     * @param end
     * @return
     */
    List<PrizeCustomerDto> findCustomerPrizeList(@Param("activityCode") String activityCode, @Param("start") Integer start, @Param("end") Integer end);

    /**
     * 查询类型不重复的奖品(各取一个)
     *
     * @param activityCode 活动编码
     * @param type         奖品类型
     * @return
     */
    List<ActPrize> findDistinctPrizes(@Param("activityCode") String activityCode, @Param("type") Integer type);

    /**
     * 获取指定活动下可用的奖品编码列表
     *
     * @param activityCode
     * @return
     */
    List<String> findAvailablePrizeCode(@Param("activityCode") String activityCode);

    /**
     * 获取所有已经领取的奖品
     *
     * @param activityCode
     * @param start
     * @param end
     * @return
     */
    List<ShowPrizeDto> findAllReceivedPrizes(@Param("activityCode") String activityCode, @Param("start") Integer start, @Param("end") Integer end);

    /**
     * 批量插入奖品
     *
     * @param list
     * @return
     */
    int insertBatch(List<ActPrize> list);

    /**
     * 查找奖品数量
     *
     * @param prize
     * @return
     */
    Integer findPrizeCount(ActPrize prize);

    /**
     * 延迟奖品
     *
     * @param prize
     * @return
     */
    int delayPrize(ActPrize prize);

    /**
     * findDeliveredCount
     *
     * @param redeemCode
     * @return
     */
    Integer findDeliveredCount(@Param("redeemCode") String redeemCode);

    /**
     * 查找领取奖品成功的客户号和手机号，用户发短信通知
     *
     * @param activityCode
     * @param updateTimeFrom
     * @param updateTimeTo
     * @return
     */
    List<RenewedReceivedNoticeDto> findNoticeList(@Param("activityCode") String activityCode,
                                                  @Param("updateTimeFrom") Date updateTimeFrom,
                                                  @Param("updateTimeTo") Date updateTimeTo);

    /**
     * 查找领取奖品成功的客户号和手机号，用户发短信通知
     *
     * @param activityCode
     * @param updateTimeFrom
     * @param updateTimeTo
     * @return
     */
    List<NoticeDto> findNotice(@Param("activityCode") String activityCode,
                               @Param("updateTimeFrom") Date updateTimeFrom,
                               @Param("updateTimeTo") Date updateTimeTo);


    /**
     * 获取指定用户抽到的奖品
     *
     * @param activityCode
     * @param userId
     * @return
     */
    List<ActPrize> findLotteryActPrize(@Param("activityCode") String activityCode, @Param("userId") String userId);


    /**
     * 获取最近领取到的奖品
     *
     * @param activityCode
     * @param topPrizeCodes
     * @return
     */
    ActPrize queryLastPrizes(@Param("activityCode") String activityCode, @Param("prizeCodes") List<String> topPrizeCodes);

    /**
     * getUserPrizeList
     *
     * @param prize
     * @return
     */
    List<ActPrize> getUserPrizeList(ActPrize prize);

    /**
     * 获取抽到的特殊奖品
     *
     * @param activityCode
     * @param specialPrizeTypes
     * @return
     */
    List<ShowPrizeDto> findSpecialPrizes(@Param("activityCode") String activityCode, @Param("specialPrizeTypes") String[] specialPrizeTypes);
}
