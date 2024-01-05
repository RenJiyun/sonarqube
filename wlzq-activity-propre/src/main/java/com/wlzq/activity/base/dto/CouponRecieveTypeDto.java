package com.wlzq.activity.base.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 优惠券领取奖品dto
 * @author 
 * @version 1.0
 */
@Data
public class CouponRecieveTypeDto {	
	private String prizeType;		// 奖品类型
	private Integer type;		// 类型,1:兑换码，2：京东卡，4：优惠券
	private String name;     //奖品名称
	private String description;  //奖品描述
	private Integer time;		// 使用时间（月）
	private Double worth;		// 价值 
	private String regulation;	// 使用规则
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateFrom; //有效开始时间
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateTo;   //有效结束时间
	@JsonIgnore
	private String openUrl; //(优惠券)打开地址
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date recieveTime; //领取时间
	private String cardNo;		// 卡编码
	private String cardPassword;		// 卡密码
	private String sourceText;		// 原文：
	private String redeemCode;		// 兑换码
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date createTime; //领取时间
}

