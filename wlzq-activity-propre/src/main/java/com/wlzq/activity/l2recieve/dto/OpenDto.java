package com.wlzq.activity.l2recieve.dto;

/**
 * OpenDto
 * @author 
 * @version 1.0
 */
public class OpenDto {
	public static final Integer RECIEVE_NOT = 0;
	public static final Integer RECIEVE_SUCCESS = 1;
	public static final Integer RECIEVE_ALREADY = 2;
	public static final Integer RECIEVE_EXPIRE = 3;

	public static final Integer STATUS_NOT_ACTIVE = 0;
	public static final Integer STATUS_ACTIVE = 1;
	/**领取成功状态，0：未领取，1：成功，2：已领取，3：超过领取有效期*/
	private java.lang.Integer recieveStatus;
	/**是否生效，0：未生效，1：已生效*/
	private java.lang.Integer activeStatus;	
	/**付款金额（单位：分）*/
	private java.lang.Integer amount;	
	/**类型，1：新开户，2：新增有效户，3：新开信用账户*/
	private java.lang.Integer type;
	/**开通手机号*/
	private java.lang.String mobile;
	
	public java.lang.Integer getRecieveStatus() {
		return recieveStatus;
	}
	public void setRecieveStatus(java.lang.Integer recieveStatus) {
		this.recieveStatus = recieveStatus;
	}
	
	public java.lang.Integer getActiveStatus() {
		return activeStatus;
	}
	public void setActiveStatus(java.lang.Integer activeStatus) {
		this.activeStatus = activeStatus;
	}
	public java.lang.Integer getAmount() {
		return amount;
	}
	public void setAmount(java.lang.Integer amount) {
		this.amount = amount;
	}
	public java.lang.Integer getType() {
		return type;
	}
	public void setType(java.lang.Integer type) {
		this.type = type;
	}
	public java.lang.String getMobile() {
		return mobile;
	}
	public void setMobile(java.lang.String mobile) {
		this.mobile = mobile;
	}
	
}

