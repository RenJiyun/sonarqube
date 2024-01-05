/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 竞猜信息Entity
 * @author louie
 * @version 2018-05-21
 */
@Data
public class Guess {
	public static final Integer DIRECTION_UP = 1;
	public static final Integer DIRECTION_DOWN = 0;
	@JsonIgnore
	private Long id;
	@JsonIgnore
	private String userId;		// 用户ID
	private Integer direction;		// 猜涨跌，0：跌，1：涨
	private Integer usePoint;		// 使用积分
	private String guessDate;		// 竞猜交易日期
	private Integer guessNo;		// 竟猜场次
	private Integer status;		// 结算状态，0：未结算，1：已结算
	private Double ratio;		// 结算赔率
	private Integer resultDirection;		// 结果涨跌，0：趺，1：涨
	private Long winPoint;		// 赢得积分
	@JsonIgnore
	private Date createTime;		// 创建时间
	@JsonIgnore
	private Date updateTime;		// 更新时间
	@JsonIgnore
	private String remark;		// 备注
}