package com.wlzq.activity.base.dto;

/**
 * Level2OpenDto
 * @author 
 * @version 1.0
 */
public class Level2OpenDto {
	public static final Integer STATUS_FAIL = 0;
	public static final Integer STATUS_SUCCESS = 1;
	
	/**状态，0：失败，1：成功*/
	private java.lang.Integer status;	
	/**订单号*/
	private java.lang.String orderNo;	
	/**消息*/
	private java.lang.String message;
	
	public java.lang.Integer getStatus() {
		return status;
	}
	public void setStatus(java.lang.Integer status) {
		this.status = status;
	}
	public java.lang.String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(java.lang.String orderNo) {
		this.orderNo = orderNo;
	}
	public java.lang.String getMessage() {
		return message;
	}
	public void setMessage(java.lang.String message) {
		this.message = message;
	}	
	
}

