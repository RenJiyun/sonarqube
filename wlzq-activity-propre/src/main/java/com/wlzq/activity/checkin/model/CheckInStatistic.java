/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.checkin.model;

/**
 * 签到记录Entity
 * @author louie
 * @version 2018-03-14
 */
public class CheckInStatistic {
	
	private String startTime;		// 连续签到开始时间
	private String endTime;		// 连续签到结束时间
	private Integer continuousCount;		// 连续签到数
	
	public CheckInStatistic() {
		super();
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Integer getContinuousCount() {
		return continuousCount;
	}

	public void setContinuousCount(Integer continuousCount) {
		this.continuousCount = continuousCount;
	}

	
}