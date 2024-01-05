/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import java.util.Date;

import lombok.Data;

/**
 * 连胜信息Entity
 * @author louie
 * @version 2018-05-21
 */
@Data
public class GuessWin {
	private Long id;
	private String userId;		// 用户ID
	private Long winCount;		// 连胜次数
	private Date winFromDate;		// 连续开始时间
	private Date winToDate;		// 连续结束时间
	private String winDates;		// 连胜竞猜日期,","隔开
	private Date createTime;		// 创建时间
	private Date updateTime;		// 更新时间
	private String remark;		// 备注
	
}