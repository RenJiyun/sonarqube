package com.wlzq.activity.base.dto;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 我的奖品dto
 * @author
 * @version 1.0
 */
@Data
public class MyPrizeDto {
	public static final Integer STATUS_NOT_USED = 2;
	public static final Integer STATUS_USED = 3;
	public static final Integer STATUS_EXPIRE = 4;
	private Long id;
	private Integer status;		// 状态,2:未使用，3：已使用，4：已过期
	private Integer isCurrentEnable;		// 当前是否可用，0：否，1：是
	private Integer type;		// 类型,1:Level2兑换码，2：京东卡，4：优惠券,5:第一财经兑换码
	private Integer couponType;		// 优惠券类型,1:折扣券，2：免单券，4：权益券
	private String name;     //奖品名称
	private String description;  //奖品描述
	private String code;		// 兑换码/优惠券编码
	private Integer time;		// 使用时间（月）
	private Double worth;		// 价值
	private Double discount;   //折扣
	private String regulation;	// 使用规则
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateFrom; //有效开始时间
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateTo;   //有效结束时间
	private String openUrl; //(优惠券)打开地址
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date recieveTime; //领取时间
	private String cardNo;		// 卡编码
	private String cardPassword;		// 卡密码
	private String sourceText;		// 原文：
	private String redeemCode;		// 兑换码
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date createTime; //领取时间
	private Integer redEnvelopeAmt;	//红包金额,单位分
	private Integer isDeliveredInfo;	//是否已填写实物奖品收货信息

	private Integer amountSatisfy;		// 满减基础金额
	private Integer amountReducation;		// 满减金额

	private Integer point; //积分(应用场景：奖品可以由多少积分兑换)
}

