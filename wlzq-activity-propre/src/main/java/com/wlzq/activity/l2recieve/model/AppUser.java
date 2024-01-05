package com.wlzq.activity.l2recieve.model;

import java.util.Date;

/**
 * 
 * @author louie
 * @version 2017-10-10
 */
public class AppUser  {
	private String mobile;		// 手机号
	
	private Date registTime;		// 注册时间
	
	public AppUser() {
		super();
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Date getRegistTime() {
		return registTime;
	}

	public void setRegistTime(Date registTime) {
		this.registTime = registTime;
	}

}