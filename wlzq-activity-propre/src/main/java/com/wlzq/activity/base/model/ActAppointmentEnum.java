package com.wlzq.activity.base.model;

public enum ActAppointmentEnum {
	
	FINSECTION_2020_LIVE("ACTIVITY.818.APPOINTMENT", "ACTIVITY.818.2020.LIVE", "投顾直播", "1,2"),
	FINSECTION_2020_SALE_1("ACTIVITY.818.APPOINTMENT", "ACTIVITY.818.2020.SALE1", "固定-7天期开售", "1,2"),
	FINSECTION_2020_SALE_2("ACTIVITY.818.APPOINTMENT", "ACTIVITY.818.2020.SALE2", "固定-35天期开售", "1,2"),
	FINSECTION_2020_SALE_3("ACTIVITY.818.APPOINTMENT", "ACTIVITY.818.2020.SALE3", "浮动-中证500看涨开售", "1,2"),
	FINSECTION_2020_SALE_4("ACTIVITY.818.APPOINTMENT", "ACTIVITY.818.2020.SALE4", "浮动-中证500看跌开售", "1,2"),
	FINSECTION_2020_SALE_5("ACTIVITY.818.APPOINTMENT", "ACTIVITY.818.2020.SALE5", "浮动-上证50单鲨看涨开售", "1,2"),
	
	DOUBLEELEVEN_2021_SALE("ACTIVITY.SURPRISE.20211111.APPOINTMENT", "ACTIVITY.SURPRISE.20211111.APPOINTMENT", "双十一理财产品开售", "1,2"),
	ACTIVITY_APPOINTMENT_20220308("ACTIVITY.SURPRISE.20220308.APPOINTMENT", "ACTIVITY.SURPRISE.20220308.APPOINTMENT", "2022三八女神节", "1,2");
	
	
	private String activitCode;
	private String appointmentCode;
	private String appointmentName;
	private String reachType;
	
	ActAppointmentEnum(String activitCode, String appointmentCode, String appointmentName, String reachType) {
		this.activitCode = activitCode;
		this.appointmentCode = appointmentCode;
		this.appointmentName = appointmentName;
		this.reachType = reachType;
	}

	public String getActivitCode() {
		return activitCode;
	}

	public void setActivitCode(String activitCode) {
		this.activitCode = activitCode;
	}

	
	public static ActAppointmentEnum getByActivityCode(String activityCode) {
		for (ActAppointmentEnum each : ActAppointmentEnum.values()) {
			if (activityCode.equals(each.getActivitCode())) {
				return each;
			}
		}
		return null;
	}

	public static ActAppointmentEnum getByAppointmentCode(String appointmentCode) {
		for (ActAppointmentEnum each : ActAppointmentEnum.values()) {
			if (appointmentCode.equals(each.getAppointmentCode())) {
				return each;
			}
		}
		return null;
	}
	
	public String getAppointmentName() {
		return appointmentName;
	}

	public void setAppointmentName(String appointmentName) {
		this.appointmentName = appointmentName;
	}

	public String getReachType() {
		return reachType;
	}

	public void setReachTypes(String reachType) {
		this.reachType = reachType;
	}
	
	public String getAppointmentCode() {
		return appointmentCode;
	}
	
	public void setAppointmentCode(String appointmentCode) {
		this.appointmentCode = appointmentCode;
	}
}
