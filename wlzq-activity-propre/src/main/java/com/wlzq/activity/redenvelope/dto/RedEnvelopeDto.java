/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.redenvelope.dto;

/**
 * 红包DTO
 * @author louie
 * @version 2018-04-17
 */
public class RedEnvelopeDto {
	private Integer amount;		// 金额
	private String businessCode;		// 业务编号
	private String businessNo;		// 业务单号
	private String orderNo;		// 订单号
	private String recieveUrl;	// 领取红包的链接
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getBusinessCode() {
		return businessCode;
	}
	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode;
	}
	public String getBusinessNo() {
		return businessNo;
	}
	public void setBusinessNo(String businessNo) {
		this.businessNo = businessNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getRecieveUrl() {
		return recieveUrl;
	}
	public void setRecieveUrl(String recieveUrl) {
		this.recieveUrl = recieveUrl;
	}
	
}