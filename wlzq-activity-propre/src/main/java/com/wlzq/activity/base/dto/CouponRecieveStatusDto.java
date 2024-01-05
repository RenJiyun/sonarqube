package com.wlzq.activity.base.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 优惠券领取状态dto (活动奖品领取返回dto)
 *
 * @author
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class CouponRecieveStatusDto implements Serializable {
    public static final CouponRecieveStatusDto NOT_HIT = new CouponRecieveStatusDto()
            .setStatus(ActPrize.STATUS_SEND).setHit(CodeConstant.CODE_NO)
            .setPrizeType("ZFK").setPrizeName("祝福卡");

    public static final Integer STATUS_NOT_RECIEVED = 1;
    public static final Integer STATUS_RECIEVED = 2;
    public static final Integer STATUS_RECIEVE_COMPLETE = 3;
    public static final Integer STATUS_USE = 4;
    public static final Integer STATUS_TODAY_RECIEVE_COMPLETE = 5;

    /** 奖品类型 */
    private String prizeType;
    /** 状态: 1-未领取, 2-已领取, 3-已抢光, 4-已使用, 5-今日已抢光 */
    private Integer status;
    /** 奖品名称 */
    private String prizeName;
    /** 剩余数量 */
    private Integer leftCount;
    /** 总数量 */
    private Integer allCount;
    /** 每日限制数量 */
    private Integer dailyLimit;
    /** 今日剩余数量 */
    private Integer dailyLeftCount;
    private MyPrizeDto prize;
    /** 剩余领取次数 */
    private Integer leftRecieveTimes;
    /** 是否有客户号 */
    private Integer isHasCustomer;
    /** 分享人手机号 */
    private String shareMobile;
    /** 已领取次数 */
    private Integer recieveCount;
    /** 1-领取，2-延期 */
    private Integer operFlag;
    /** 推荐人姓名 */
    private String recommendName;
    /** 是否抽中 */
    private Integer hit;
    private String activityCode;
    private String activityName;
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date sendTime;
    /** 开户日期 */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date openDate;
    /** 上次已经领取 */
    private boolean lastHadReceive;
    /** 上次领取客户号 */
    private String lastCustomerId;


}

