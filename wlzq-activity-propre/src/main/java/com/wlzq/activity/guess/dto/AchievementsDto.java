package com.wlzq.activity.guess.dto;

import lombok.Data;

/**
 * 战绩dto
 *
 * @author
 * @version 1.0
 */
@Data
public class AchievementsDto {
	/**
	 * 手机
	 */
	private String mobile;
	/**
	 * 积分
	 */
	private Long point;
	/**
	 * 头像
	 */
	private String portrait;
	/**
	 * 累计竞猜次数
	 */
	private Long total;
	/**
	 * 最高连胜次数
	 */
	private Long winCount;
	/**
	 * 最高连胜排名
	 */
	private Long order;
	/**打败用户比例*/
	private String beatRatio;
	/**
	 * 活动
	 **/
	private String activityCode;

	/*累计胜利次数*/
	private Long allWinCount;

}

