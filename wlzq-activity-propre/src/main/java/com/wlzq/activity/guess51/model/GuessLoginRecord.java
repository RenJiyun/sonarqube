/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import java.util.Date;

import lombok.Data;

/**
 * 当天首次登录信息Entity
 * @author louie
 * @version 2018-05-22
 */
@Data
public class GuessLoginRecord {
	private String userId;		// 用户ID
	private Date createTime;		// 创建时间
	private Date createTimeFrom;		// 创建开始时间
	private Date createTimeTo;		// 创建结束时间
}