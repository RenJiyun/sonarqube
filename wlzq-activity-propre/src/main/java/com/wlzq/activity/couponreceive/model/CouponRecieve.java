/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.couponreceive.model;

import java.util.Date;

import lombok.Data;

/**
 * 优惠券领取Entity
 * @author louie
 * @version 2019-05-24
 */
@Data
public class CouponRecieve {
	private String userId;		// 用户ID
	private String customerId;		// 客户号
	private String fundAccount;		// 资金账号
	private String couponTemplate;		// 优惠券模板编码
	private String couponCode;		// 优惠券编码
	private Date createTime;		// 写入时间
	private Date createTimeFrom;		// 写入时间
	private Date createTimeTo;		// 写入时间
}