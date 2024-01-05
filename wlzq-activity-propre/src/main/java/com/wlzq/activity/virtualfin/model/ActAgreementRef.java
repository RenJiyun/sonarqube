/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.virtualfin.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 活动理财订单Entity
 * @author zhaozx
 * @version 2020-07-27
 */
@Data
public class ActAgreementRef {
	@JsonIgnore
	private String id;
	@JsonIgnore
	private String activityCode;	// 活动代码
	@JsonIgnore
	private String productCode;		// 产品代码
	private String agreementName;		// 物品代码
	private String agreementId;		// 物品名称
	@JsonIgnore
	private Date createTime;
	@JsonIgnore
	private Date updateTime;
	@JsonIgnore
	private Integer isDeleted;
}