/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.push.model;

import java.util.Date;

import lombok.Data;

/**
 * 业务推送纪录Entity
 * @author cjz
 * @version 2021-04-21
 */
@Data
public class PushBusinessRecord {
	
	private Integer id;
	private String businessType;		// 业务类型
	private String secType;		// 第二类型
	private String batch;		// 批次
	private String customerId;		// 客户号
	private String mobile;		// 手机号
	private String customerName;		// 客户姓名
	private String link;		// 内容链接
	private String content;		// 内容
	private Date createdTime;		// 生成时间
	
}