/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 优惠券Entity
 * @author louie
 * @version 2018-11-23
 */
@Data
public class Coupon  { 
	public static final Integer STATUS_NOT_OBSOLETE = 0;
	public static final Integer STATUS_NOT_SEND = 1;
	public static final Integer STATUS_SENDED = 2;
	public static final Integer STATUS_USED = 3;
	public static final Integer STATUS_EXPIRED = 4;
	
	public static final Integer VALIDITY_TYPE_TIME_RANGE = 1;
	public static final Integer VALIDITY_TYPE_DAYS = 2;

	public static final Integer APPLICABLE_TYPE_ALL = 1; //通用
	public static final Integer APPLICABLE_TYPE_ASSIGN = 2; //指定使用
	
	private Integer type;		// 类型，1：折扣券，2：免单券，3：满减券
	private String template;		// 模板
	private String code;		// 编码
	private String name;		// 名称
	private String displayName;		// 显示名称
	private String description;		// 描述
	private Integer validityType;		// 有效期类型，1：日期范围，2：下发起天数
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateFrom;		// 有效期开始时间
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateTo;		// 有效期结束时间
	private Integer applicableType;		// 适用类型，1：通用，2：指定使用
	private Integer amountSatisfy;		// 满减基础金额
	private Integer amountReducation;		// 满减金额
	private Double discount;		// 折扣
	private Integer validityDays;		// 下发起有效天数
	private String openUrl;		// 打开地址
	private Integer canUse;		// 是否可用，0：否，1：是
	private Integer status;		// 状态，0：已作废，1：未发出，2：已发出，3：已使用,4:已过期
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date sendTime;		// 发出时间
	private String userid;		// 持有用户ID
	private String customerId;		// 持有客户ID
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date useTime;		// 使用时间
	@JsonIgnore
	private String orderNo;		// 使用订单号
	@JsonIgnore
	private Date updateTime;		// 更新时间
	@JsonIgnore
	private String remark;		// 备注
	private String regulation;	//使用规则
	private String recommendCode;
}