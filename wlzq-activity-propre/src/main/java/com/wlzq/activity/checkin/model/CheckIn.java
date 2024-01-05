/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.checkin.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 签到记录Entity
 * @author louie
 * @version 2018-03-14
 */
public class CheckIn implements Serializable{
	private static final long serialVersionUID = 1003763476457L;
	@JsonIgnore
	private String userId;		// 用户ID
	@JsonIgnore
	private String openid;		// openid
	private Integer type;		// 签到类型，1：正常签到,2: 补签
	private Date checkInTime;		// 签到时间
	@JsonIgnore
	private Date createTime;		// 创建时间
	@JsonIgnore
	private String remark;		// 备注
	
	public CheckIn() {
		super();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@Length(min=0, max=128, message="备注长度必须介于 0 和 128 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Date getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}
	
}