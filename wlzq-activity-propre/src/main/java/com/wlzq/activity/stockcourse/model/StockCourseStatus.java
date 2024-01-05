/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.stockcourse.model;

import lombok.Data;
import java.util.Date;

/**
 * 股票课状态Entity
 * @author zjt
 * @version 2021-10-25
 */
@Data
public class StockCourseStatus {
	/** 已经学习课程*/
	public static final Integer LEARNED = 1;
	private static final long serialVersionUID = 1L;
	private Long id;
	private String mobile;		// 报名手机号
	private Integer classNo;	// 课程编号
	private Date classOpenDate;	// 课程开放日期
	private Integer status;		// 课程观看状态：0：未完成；1：已完成
	private Date createTime;	// 创建时间
	private Date updateTime;	// update_time
	private Date pushTime;		// push_time推送时间

	
}