/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import java.util.Date;

import lombok.Data;

/**
 * 竞猜奖品设置Entity
 * @author louie
 * @version 2018-05-25
 */
@Data
public class GuessPrizeSetting{
	public static final Integer TYPE_RED_ENVELOPE = 1;
	private Integer winCount;		// 连胜次数
	private String prizeCode;		// 奖品编码
	private Integer value;		// 奖品值
	private Date createTime;		// 创建时间
	private String remark;		// 备注
}