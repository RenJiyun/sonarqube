/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import java.util.Date;

import lombok.Data;

/**
 * 大盘指数Entity
 * @author louie
 * @version 2018-05-21
 */
@Data
public class GuessIndex {
	public static final Integer TYPE_SH = 1;
	private Long id;
	private Integer type;		// 类型，1：上证指数
	private Double open;		// 开市指数
	private Double close;		// 收市指数
	private String indexDate;		// 指数日期
	private Integer guessNo;		// 竟猜场次
	private Date createTime;		// 创建时间
	private String remark;		// 备注
	
}