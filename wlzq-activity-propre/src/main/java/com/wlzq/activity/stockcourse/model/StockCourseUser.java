/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.stockcourse.model;

import lombok.Data;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

/**
 * 股票课活动Entity
 * @author zjt
 * @version 2021-10-25
 */
@Data
public class StockCourseUser {
	
	public static final String ACT_STOCK_COURSE = "ACTIVITY.STOCKCOURSE.COUPON";
	/** 已领券体验券*/
	public static final Integer RECEIVED_COUPON = 1;
	/** 已经学习课程*/
	public static final Integer LEARNED = 1;
	/**用户注册*/
	public static final Integer USER_REGISTRATION = 1;
	/**用户更新*/
	public static final Integer USER_UPDATE = 2;
	
	private static final long serialVersionUID = 1L;
	@JsonIgnore
	private Long id;
	private String mobile;		// 报名手机号
	@JsonIgnore
	private String openId;		// open_id
	@JsonIgnore
	private String wechatName;		// 微信昵称
	@JsonIgnore
	private String customerId;		// 客户号
	@JsonIgnore
	private Date registrationTime;		// app注册时间
	@JsonIgnore
	private Date accountOpenTime;		// 开户时间
	private Integer updatedCourseNumber;		// 已更新课程节数
	private Integer learnedCourseNumber;		// 已学习课程节数
	@JsonIgnore
	private Integer courseNumber;		// 课程节数
	@JsonIgnore
	private Integer courseNumberStatus;		// 是否完成课程
	@JsonIgnore
	private Integer watchedLiveStream;		// 是否看过直播
	private Integer receivedCoupon;		// 是否领取体验券
	@JsonIgnore
	private Integer purchasedWanShanHong;		// 是否购买万山红
	private String name;		// 姓名
	private String receivingAddress;		// 收货地址
	private String receivingMobile;		// 收货人手机号
	private String courseStatus;		// 课程观看状态：如10111：下标1代表第一节课观看状态：0代表用户未完成，1代表用户已完成
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date createTime;		// 报名时间
	@JsonIgnore
	private Date updateTime;		// update_time
	@JsonIgnore
	private String couponTemplate;
	
}