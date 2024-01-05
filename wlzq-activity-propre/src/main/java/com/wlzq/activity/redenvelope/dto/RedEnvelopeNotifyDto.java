/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.redenvelope.dto;

import java.util.Date;

/**
 * 红包发送成功通知dto
 * @author louie
 * @version 2018-04-17
 */
public class RedEnvelopeNotifyDto {
	private Integer amount;		// 金额
	private String businessNo;		// 业务单号
	private String orderNo;		// 订单号
	private Date payTime;		// 红包发送时间
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
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
	
}