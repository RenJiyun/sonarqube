/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import java.util.Date;

import lombok.Data;

/**
 * 猜涨跌用户Entity
 * @author louie
 * @version 2018-05-21
 */
@Data
public class GuessUser {
	private String userId;		// 用户ID
	private String openid;		// OPENID
	private String mobile;		// 手机
	private Date createTime;		// 创建时间
	private String remark;		// 备注
	
}