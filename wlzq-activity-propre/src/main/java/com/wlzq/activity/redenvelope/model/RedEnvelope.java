/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package  com.wlzq.activity.redenvelope.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 红包Entity
 * @author louie
 * @version 2018-04-17
 */
public class RedEnvelope {
	public final static Integer	STATUS_NO_RECIEVE = 0;
	public final static Integer	STATUS_RECIEVE = 1;
	public final static Integer	STATUS_RECIEVE_FAIL = 2;
	private String userId;		// 用户id
	private String openId;		// 用户openId
	private String mobile;		// 用户手机号
	private Integer amount;		// 金额
	private String businessCode;		// 业务编号
	private String businessNo;		// 业务单号
	private String orderNo;		// 订单号
	private Integer status;		// 状态，0：未领取，1：已领取，2：领取失败
	private Integer checkStatus;		// 审核状态，1：无需审核，2：审核中，3：审核通过，4：审核失败
	private String notifyUrl;		// 回调平台地址
	private String recieveUrl;		// 领取地址
	private Date createTime;		// 创建时间
	private Date callbackTime;		// 回调时间
	private String sendData;		// 发送数据
	private String callbackData;		// 回调数据
	private Date payTime;		// 红包发送时间
	private String remark;		// 备注
	
	public RedEnvelope() {
		super();
	}

	@Length(min=1, max=64, message="用户id长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=1, max=11, message="用户手机号长度必须介于 1 和 11 之间")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	@NotNull(message="金额不能为空")
	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	
	@Length(min=1, max=64, message="业务编号长度必须介于 1 和 64 之间")
	public String getBusinessCode() {
		return businessCode;
	}

	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode;
	}
	
	@Length(min=1, max=64, message="业务单号长度必须介于 1 和 64 之间")
	public String getBusinessNo() {
		return businessNo;
	}

	public void setBusinessNo(String businessNo) {
		this.businessNo = businessNo;
	}
	
	@Length(min=1, max=64, message="订单号长度必须介于 1 和 64 之间")
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	@NotNull(message="status不能为空")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@Length(min=0, max=512, message="回调平台地址长度必须介于 0 和 512 之间")
	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	
	@Length(min=0, max=512, message="领取地址长度必须介于 0 和 512 之间")
	public String getRecieveUrl() {
		return recieveUrl;
	}

	public void setRecieveUrl(String recieveUrl) {
		this.recieveUrl = recieveUrl;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCallbackTime() {
		return callbackTime;
	}

	public void setCallbackTime(Date callbackTime) {
		this.callbackTime = callbackTime;
	}
	
	@Length(min=0, max=1024, message="回调数据长度必须介于 0 和 1024 之间")
	public String getCallbackData() {
		return callbackData;
	}

	public void setCallbackData(String callbackData) {
		this.callbackData = callbackData;
	}
	
	public String getSendData() {
		return sendData;
	}

	public void setSendData(String sendData) {
		this.sendData = sendData;
	}

	@Length(min=0, max=128, message="备注长度必须介于 0 和 128 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Integer getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(Integer checkStatus) {
		this.checkStatus = checkStatus;
	}
	
}