package com.wlzq.activity.guess51.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 竞猜情况dto
 * @author 
 * @version 1.0
 */
@Data
public class GuessStatusDto {
	/**是否为下个交易日,0：否，1：是*/
	private Integer isNextTradeDate;
	/**猜涨总积分*/
	private Long upPoint;	
	/**猜跌总积分*/
	private Long downPoint;	
	/**猜涨押注100赢总积分*/
	private Long upWinPoint;	
	/**猜跌押注100赢总积分*/
	private Long downWinPoint;		
	/**猜涨数*/
	@JsonIgnore
	private Integer upCount;	
	/**猜跌数*/
	@JsonIgnore
	private Integer downCount;
	/**猜涨比例*/
	private Integer upRatio;	
	/**猜跌比例*/
	private Integer downRatio;	
	/**当前场次*/
	private Integer guessNo;	
}

