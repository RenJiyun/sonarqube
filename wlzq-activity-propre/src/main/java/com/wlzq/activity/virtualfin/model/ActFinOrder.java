package com.wlzq.activity.virtualfin.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 活动理财订单
 * @author zhaozx
 * @version 2020-07-28
 */
@Data
public class ActFinOrder {
	
	public static final Integer ORDER_FLAG_BUY = 1;		// 购买
	public static final Integer ORDER_STATUS_UNCOMFIRMED = 1;
	public static final Integer ORDER_STATUS_COMFIRMED = 2;
	public static final Integer ORDER_STATUS_DUE = 3;
	@JsonIgnore
	private String id;
	private String mobile;		// 手机号
	private String userId;		// 用户Id
	private String openId;		// openid
	private String customerId;		// 客户号
	private Integer flag;		// 标识，1-购买
	private String orderId;		// 订单号
	private Double price;		// 价格
	private Integer status;		// 状态，1-待确认，2-确认，3-到期
	private Double profit;		// 利率
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date closeDateStart;		// 封闭期开始时间
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date closeDateEnd;		// 封闭期结束时间
	private String activityCode;	// 活动代码
	private String activityName;	// 活动名称
	private String productCode;		// 产品代码
	private String productName;		//
	private String goodsCode;		// 物品代码
	private String goodsName;		// 物品名称
	private Integer period;
	private Integer leftDays;
	private Double incomeBalance;
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date createTime;
	@JsonIgnore
	private Date updateTime;
	@JsonIgnore
	private Date now;
	@JsonIgnore
	private Integer isDeleted;
}
