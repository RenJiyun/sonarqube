/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.redenvelope.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 红包回调参数
 * @author louie
 * @version 2018-04-17
 */
public class CallbackParam {
	public final static String SUCCESS = "success";
	public final static String FAIL = "fail";	
	private String sceneNo;		// 场景值
	private String sceneKey;		// 流水号
	private String userId;		// 用户ID
	private String amount;		// 金额
	private String result;		// 结果，成功:success ,失败： fail
	private String errorMsg;		// 发放错误原因
	private String sendFlag;		// 发放标识状态，0：正常，1：红包补发
	private String paymentTime;		// 发送时间，2017-01-01 12:00:00
	public String getSceneNo() {
		return sceneNo;
	}
	@JsonProperty(value="scene_no")
	public void setSceneNo(String sceneNo) {
		this.sceneNo = sceneNo;
	}
	public String getSceneKey() {
		return sceneKey;
	}
	@JsonProperty(value="scene_key")
	public void setSceneKey(String sceneKey) {
		this.sceneKey = sceneKey;
	}
	public String getUserId() {
		return userId;
	}
	@JsonProperty(value="user_id")
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	@JsonProperty(value="error_msg")
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getSendFlag() {
		return sendFlag;
	}
	@JsonProperty(value="send_flag")
	public void setSendFlag(String sendFlag) {
		this.sendFlag = sendFlag;
	}
	public String getPaymentTime() {
		return paymentTime;
	}
	@JsonProperty(value="payment_time")
	public void setPaymentTime(String paymentTime) {
		this.paymentTime = paymentTime;
	}
	
}