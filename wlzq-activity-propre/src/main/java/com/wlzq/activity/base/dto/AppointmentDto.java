package com.wlzq.activity.base.dto;

import lombok.Data;

@Data
public class AppointmentDto {

	private Integer status;			// 通知状态，0-否，1-是
	private String appointmentCode;
	private String activityCode;
	private String mobile;			// 客户手机号
}
