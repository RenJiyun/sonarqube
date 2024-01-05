package com.wlzq.activity.base.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class PrizeDto {
    public static final Integer STATUS_NOT_USED = 2;
    public static final Integer STATUS_USED = 3;
    public static final Integer STATUS_EXPIRE = 4;
    private Long id;
    /** 状态: 2-未使用 3-已使用 4-已过期 */
    private Integer status;
    /** 当前是否可用: 0-否 1-是 */
    private Integer isCurrentEnable;
    /** 类型: 1-Level2兑换码 2-京东卡 4-优惠券 5-第一财经兑换码 */
    private Integer type;
    /** 优惠券类型: 1-折扣券 2-免单券 4-权益券 */
    private Integer couponType;
    /** 奖品名称 */
    private String name;
    /** 奖品描述 */
    private String description;
    /** 兑换码/优惠券编码 */
    private String code;
    /** 使用时间（月） */
    private Integer time;
    /** 价值 */
    private Double worth;
    /** 折扣 */
    private Double discount;
    /** 使用规则 */
    private String regulation;
    /** 有效开始时间 */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date validityDateFrom;
    /** 有效结束时间 */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date validityDateTo;
    /** (优惠券)打开地址 */
    private String openUrl;
    /** 领取时间 */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date recieveTime;
    /** 兑换码 */
    private String redeemCode;
    /** 领取时间 */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date createTime;
    /** 备注 */
    private String remark;
}
