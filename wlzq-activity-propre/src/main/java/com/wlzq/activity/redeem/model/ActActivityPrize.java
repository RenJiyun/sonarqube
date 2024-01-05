/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.redeem.model;

import java.util.Date;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 活动奖品表Entity
 * @author cjz
 * @version 2018-09-28
 */
public class ActActivityPrize {
	
	private Integer id;
	private String activityCode;		// 活动编码
	private String userId;		// 用户id
	private Integer redeemId;		// 兑换码id
	private Date createTime;		// 创建时间
	
	private Integer redeemStatus;  // 兑换码状态
	private String redeemCode;     // 兑换码
	private String typeName;       // 奖品类型
	private String goodsName;      // 奖品名称
	private Integer goodsTime;     // 奖品期限
	
	public ActActivityPrize() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Length(min=0, max=50, message="活动编码长度必须介于 0 和 50 之间")
	public String getActivityCode() {
		return activityCode;
	}

	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
	
	@Length(min=0, max=64, message="用户id长度必须介于 0 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Integer getRedeemId() {
		return redeemId;
	}

	public void setRedeemId(Integer redeemId) {
		this.redeemId = redeemId;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getRedeemStatus() {
		return redeemStatus;
	}

	public void setRedeemStatus(Integer redeemStatus) {
		this.redeemStatus = redeemStatus;
	}

	public String getRedeemCode() {
		return redeemCode;
	}

	public void setRedeemCode(String redeemCode) {
		this.redeemCode = redeemCode;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public Integer getGoodsTime() {
		return goodsTime;
	}

	public void setGoodsTime(Integer goodsTime) {
		this.goodsTime = goodsTime;
	}
	
}