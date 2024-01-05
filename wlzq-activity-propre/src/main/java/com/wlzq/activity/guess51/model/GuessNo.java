/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import lombok.Data;

/**
 * 连胜信息Entity
 * @author louie
 * @version 2018-05-21
 */
@Data
public class GuessNo {
	private String guessDate;		// 竟猜日期
	private Integer guessNo;		// 竟猜场次
	private String startTime;		// 竟猜
	private String endTime;		// 连胜次数
}