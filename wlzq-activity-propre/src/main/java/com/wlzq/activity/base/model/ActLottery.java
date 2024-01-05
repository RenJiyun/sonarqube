/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 抽奖记录Entity
 * @author louie
 * @version 2018-11-30
 */
public class ActLottery  {
	private Long id;
	private String activity;		// activity
	private String userId;		// 用户ID
	private String openid;		// OPENID
	private String customerId;		// 客户id
	private Integer isHit;		// 是否中奖,0:否，1：是
	private Integer hasRecieve;		// 是否已领奖,0:否，1：是
	private String recieveCode;		// 领奖码
	private Long prize;		// 奖品ID
	private Date createTime;		// 创建时间
	private Date timeStart;
	private Date timeEnd;
	private Integer threeInOne;		//用户Id，openId，客户Id三合一判断
	
	public ActLottery() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Length(min=1, max=128, message="activity长度必须介于 1 和 128 之间")
	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}
	
	@Length(min=1, max=64, message="用户ID长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=0, max=64, message="OPENID长度必须介于 0 和 64 之间")
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}
	
	@Length(min=0, max=20, message="客户id长度必须介于 0 和 20 之间")
	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public Integer getIsHit() {
		return isHit;
	}

	public void setIsHit(Integer isHit) {
		this.isHit = isHit;
	}
	
	public Long getPrize() {
		return prize;
	}

	public void setPrize(Long prize) {
		this.prize = prize;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(Date timeStart) {
		this.timeStart = timeStart;
	}

	public Date getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(Date timeEnd) {
		this.timeEnd = timeEnd;
	}

	public Integer getHasRecieve() {
		return hasRecieve;
	}

	public void setHasRecieve(Integer hasRecieve) {
		this.hasRecieve = hasRecieve;
	}

	public String getRecieveCode() {
		return recieveCode;
	}

	public void setRecieveCode(String recieveCode) {
		this.recieveCode = recieveCode;
	}
	
	public Integer getThreeInOne() {
		return threeInOne;
	}
	
	public void setThreeInOne(Integer threeInOne) {
		this.threeInOne = threeInOne;
	}
}