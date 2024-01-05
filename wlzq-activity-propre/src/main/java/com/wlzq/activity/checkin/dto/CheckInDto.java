package com.wlzq.activity.checkin.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wlzq.activity.checkin.model.CheckIn;

/**
 * 签到dto
 * @author 
 * @version 1.0
 */
public class CheckInDto implements Serializable{
	private static final long serialVersionUID = 100343476457L;
	/**当前月份*/
	@JsonIgnore
	private String currentMonth;
	/**当月天数*/
	private Integer days;	
	/**签到日历*/
	private List<Date> calendar;	
	/**当月第一天*/
	private Date currentTime;	
	/**当月第一天星期几*/
	private Integer firstDayOfWeek;	
	/**连续签到次数*/
	private Integer continuousCount;		
	/**还有多少次签到获奖*/
	private Integer toPrizeCount;	
	/**补签机会数*/
	private Integer opportunites;
	/**签到数（累计总数）*/
	private Integer count;
	/**签到历史*/
	private List<CheckIn> checkInList;
	/**是否已获奖*/
	private Integer hasPrize;
	
	public Integer getDays() {
		return days;
	}
	public void setDays(Integer days) {
		this.days = days;
	}

	public List<Date> getCalendar() {
		return calendar;
	}
	public void setCalendar(List<Date> calendar) {
		this.calendar = calendar;
	}

	public String getCurrentMonth() {
		return currentMonth;
	}
	public void setCurrentMonth(String currentMonth) {
		this.currentMonth = currentMonth;
	}
	public Date getCurrentTime() {
		return currentTime;
	}
	public void setCurrentTime(Date currentTime) {
		this.currentTime = currentTime;
	}
	public Integer getFirstDayOfWeek() {
		return firstDayOfWeek;
	}
	public void setFirstDayOfWeek(Integer firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}
	public Integer getContinuousCount() {
		return continuousCount;
	}
	public void setContinuousCount(Integer continuousCount) {
		this.continuousCount = continuousCount;
	}
	public Integer getOpportunites() {
		return opportunites;
	}
	public void setOpportunites(Integer opportunites) {
		this.opportunites = opportunites;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public List<CheckIn> getCheckInList() {
		return checkInList;
	}
	public void setCheckInList(List<CheckIn> checkInList) {
		this.checkInList = checkInList;
	}
	public Integer getToPrizeCount() {
		return toPrizeCount;
	}
	public void setToPrizeCount(Integer toPrizeCount) {
		this.toPrizeCount = toPrizeCount;
	}
	public Integer getHasPrize() {
		return hasPrize;
	}
	public void setHasPrize(Integer hasPrize) {
		this.hasPrize = hasPrize;
	}
	
}

