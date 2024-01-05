/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.redenvelope.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 红包创建参数
 * @author louie
 * @version 2018-04-17
 */
public class CreateParam {
	private String mchNo;		// 商户简称
	private String sceneNo;		// 场景值
	private String sceneKey;		// 流水号
	private String userId;		// 用户ID
	private String amount;		// 金额
	private String descript;		// 描述
	
	@JsonProperty(value="mch_no")
	public String getMchNo() {
		return mchNo;
	}
	public void setMchNo(String mchNo) {
		this.mchNo = mchNo;
	}
	@JsonProperty(value="scene_no")
	public String getSceneNo() {
		return sceneNo;
	}
	public void setSceneNo(String sceneNo) {
		this.sceneNo = sceneNo;
	}
	@JsonProperty(value="scene_key")
	public String getSceneKey() {
		return sceneKey;
	}
	public void setSceneKey(String sceneKey) {
		this.sceneKey = sceneKey;
	}
	@JsonProperty(value="user_id")
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	
}