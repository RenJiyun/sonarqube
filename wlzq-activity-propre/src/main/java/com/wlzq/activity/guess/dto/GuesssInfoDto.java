package com.wlzq.activity.guess.dto;

import lombok.Data;

/**
 * 概览dto
 * @author
 * @version 1.0
 */
@Data
public class GuesssInfoDto {
	public static final Integer NO = 0;
	public static final Integer YES = 1;

	/**是否首次登录，0：否，1：是*/
	private Integer isLoginFirst;
	/**首次登录获取积分*/
	private Integer firstPoint;
	/**是否当天首次登录，0：否，1：是*/
	private Integer isTodayLoginFirst;
	/**每日登录获取积分*/
	private Integer loginPoint;
	/**是否首次，0：否，1：是*/
	private Integer  isGetPrize;
	/**是否未使用的奖品，0：否，1：是*/
	private Integer  hasNotUsePrize;
	/**奖励*/
	private GuesssPrizeDto  prize;
	/**是否已竞猜,0：否，1：是*/
	private Integer hasGuess;
	/**猜涨跌活动**/
	private String activityCode;
	/*猜涨跌，0：跌，1：涨*/
	private Integer direction;
	/*是否交易日,0：否，1：是*/
	private Integer isTradeDate;
}

