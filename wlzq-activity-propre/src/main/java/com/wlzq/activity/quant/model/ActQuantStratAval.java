/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.quant.model;

import java.util.Date;

import lombok.Data;

/**
 * 建模大赛策略评估Entity
 * @author zhaozx
 * @version 2020-10-28
 */
@Data
public class ActQuantStratAval {
	
	private String id;
	private String account;		// 账号
	private Integer accountType;		// 账号类型
	private String strategyName;		// 策略名
	private String evaluation;		// 评估
	
	private String rate;
	private String withdraw;
	private String sharp;
	
	private String score;		// 分数
	private String runTime;		// 运行时间
	private Date runTimeD;		//	
	private Date createTime;		// create_time
	private Date updateTime;		// update_time
	private Integer isDeleted;		// is_deleted
	
	private String backtraceDateStart;
	private String backtraceDateEnd;
}