package com.wlzq.activity.virtualfin.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 活动理财订单
 * @author zhaozx
 * @version 2020-07-28
 */
@Data
public class ActFirstLogin {
	
	@JsonIgnore
	private String id;
	private String mobile;		// 手机号
	private String userId;		// 用户Id
	private String openId;		// openid
	private String customerId;		// 客户号
	private Integer isFirstLogin;	// 1-是，0-否
	private String activityCode;	// 活动代码
	@JsonIgnore
	private Date createTime;
	@JsonIgnore
	private Date updateTime;
	@JsonIgnore
	private Integer isDeleted;
}
