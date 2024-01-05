/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.model;

import java.util.Date;

import lombok.Data;

/**
 * level2领取活动新用户推送数据Entity
 * @author louie
 * @version 2018-05-03
 */
@Data
public class Level2RecieveUser {
	private String custmerId;		// 客户ID
	private String mobile;		// 手机
	private Integer type;		// 类型，1：新开户，2：新增有效户，3：新开信用账户
	private Date effectiveDate;		// 生效时间
	private Date createTime;		// 写入时间
	private Date effectiveDateBegin;		// 生效开始时间（查询使用）
	private Date effectiveDateEnd;		// 生效结束时间（查询使用）
}