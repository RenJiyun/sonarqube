package com.wlzq.activity.base.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class CouponReceiveStatusDto implements Serializable {
    public static final Integer STATUS_NOT_RECEIVED = 1;
    public static final Integer STATUS_RECEIVED = 2;
    public static final Integer STATUS_RECEIVE_COMPLETE = 3;
    public static final Integer STATUS_USED = 4;
    public static final Integer STATUS_TODAY_RECEIVE_COMPLETE = 5;

    /** 奖品类型 */
    private String prizeType;
    /** 奖品名称 */
    private String prizeName;
    /** 状态: 1-未领取, 2-已领取, 3-已抢光, 4-已使用, 5-今日已抢光 */
    private Integer status;
    /** 奖品剩余数量 */
    private Integer leftCount;
    /** 奖品总数量 */
    private Integer allCount;
    /** 每日限制数量 */
    private Integer dailyLimit;
    /** 今日剩余数量 */
    private Integer dailyLeftCount;
    /** 兑换所需积分数 */
    private Integer point;
    /** 描述 */
    private String description;
    /** 领取到的奖品 */
    private PrizeDto prize;
    /** 领取到的奖品列表 */
    private List<PrizeDto> prizeList = new ArrayList<>();
}
