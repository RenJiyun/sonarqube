package com.wlzq.activity.guess.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 竞猜情况dto
 * @author 
 * @version 1.0
 */
public class GuesssStatusDto {
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
	/**活动编码**/
	private String activityCode;
	
	public Integer getIsNextTradeDate() {
		return isNextTradeDate;
	}
	public void setIsNextTradeDate(Integer isNextTradeDate) {
		this.isNextTradeDate = isNextTradeDate;
	}
	public Long getUpPoint() {
		return upPoint;
	}
	public void setUpPoint(Long upPoint) {
		this.upPoint = upPoint;
	}
	public Long getDownPoint() {
		return downPoint;
	}
	public void setDownPoint(Long downPoint) {
		this.downPoint = downPoint;
	}
	public Long getUpWinPoint() {
		return upWinPoint;
	}
	public void setUpWinPoint(Long upWinPoint) {
		this.upWinPoint = upWinPoint;
	}
	public Long getDownWinPoint() {
		return downWinPoint;
	}
	public void setDownWinPoint(Long downWinPoint) {
		this.downWinPoint = downWinPoint;
	}
	public Integer getUpRatio() {
		return upRatio;
	}
	public void setUpRatio(Integer upRatio) {
		this.upRatio = upRatio;
	}
	public Integer getDownRatio() {
		return downRatio;
	}
	public void setDownRatio(Integer downRatio) {
		this.downRatio = downRatio;
	}
	public Integer getUpCount() {
		return upCount;
	}
	public void setUpCount(Integer upCount) {
		this.upCount = upCount;
	}
	public Integer getDownCount() {
		return downCount;
	}
	public void setDownCount(Integer downCount) {
		this.downCount = downCount;
	}
	public String getActivityCode() {
		return activityCode;
	}
	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
	
}

