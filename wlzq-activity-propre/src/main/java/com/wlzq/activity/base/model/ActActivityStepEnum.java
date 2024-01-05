package com.wlzq.activity.base.model;

import java.util.Calendar;
import java.util.Date;

import com.wlzq.common.utils.DateUtils;

public enum ActActivityStepEnum {
	
	FINSECTION_STEP_3("ACTIVITY.818.2019.3.2.COUPON", 500, new int[] {9, 13}, new int[] {12, 16});
	
	private  String activitCode;
	private Integer prizeMaxCount;
	private int[] OPEN_HOUR;
	private int[] CLOSE_HOUR;
	
	ActActivityStepEnum(String activitCode, Integer prizeMaxCount, int[] openHour, int[] closeHour) {
		this.activitCode = activitCode;
		this.prizeMaxCount = prizeMaxCount;
		this.OPEN_HOUR = openHour;
		this.CLOSE_HOUR = closeHour;
	}

	public String getActivitCode() {
		return activitCode;
	}

	public void setActivitCode(String activitCode) {
		this.activitCode = activitCode;
	}

	public int[] getOPEN_HOUR() {
		return OPEN_HOUR;
	}

	public void setOPEN_HOUR(int[] oPEN_HOUR) {
		OPEN_HOUR = oPEN_HOUR;
	}

	public int[] getCLOSE_HOUR() {
		return CLOSE_HOUR;
	}

	public void setCLOSE_HOUR(int[] cLOSE_HOUR) {
		CLOSE_HOUR = cLOSE_HOUR;
	}
	
	public Integer getPrizeMaxCount() {
		return prizeMaxCount;
	}
	
	public void setPrizeMaxCount(Integer prizeMaxCount) {
		this.prizeMaxCount = prizeMaxCount;
	}
	
	public static ActActivityStepEnum getStepEnumByCode(String activityCode) {
		for (ActActivityStepEnum each : ActActivityStepEnum.values()) {
			if (activityCode.equals(each.getActivitCode())) {
				return each;
			}
		}
		return null;
	}
	
	public static Date getNextOpenDate(ActActivityStepEnum stepEnum) {
		Calendar cal = Calendar.getInstance();
		int hour=cal.get(Calendar.HOUR_OF_DAY);//小时
		int[] openHour = stepEnum.getOPEN_HOUR();
		Date date = null;
		for (int i = 0; i < openHour.length; i++) {
			if (hour < openHour[i]) {
				date = DateUtils.getDayStart(new Date());
				date = DateUtils.addHour(date, openHour[i]);
				break;
			}
		}
		if (date == null) {
			date = DateUtils.getDayStart(DateUtils.tomorrow());
			date = DateUtils.addHour(date, openHour[0]);
		}
		return date;
	}
	
	public static Boolean inOpenTime(ActActivityStepEnum stepEnum) {
		int i = 0;
		Calendar cal = Calendar.getInstance();
		int hour=cal.get(Calendar.HOUR_OF_DAY);//小时
		while(i < stepEnum.getOPEN_HOUR().length) {
			if (hour >= stepEnum.getOPEN_HOUR()[i] && hour < stepEnum.getCLOSE_HOUR()[i]) return true;
			i++;
		}
		return false;
	}
	
	public static Integer index(ActActivityStepEnum stepEnum) {
		Calendar cal = Calendar.getInstance();
		int hour=cal.get(Calendar.HOUR_OF_DAY);//小时
		int index = 0;
		while (index < stepEnum.getOPEN_HOUR().length) {
			if(hour >= stepEnum.getOPEN_HOUR()[index] && hour < stepEnum.getCLOSE_HOUR()[index]) {
				break;
			}
			index++;
		}
		if (index < stepEnum.getOPEN_HOUR().length) {
			return index;
		} else {
			return null;
		}
	}
}
