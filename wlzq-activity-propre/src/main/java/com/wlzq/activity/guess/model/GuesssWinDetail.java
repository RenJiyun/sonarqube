/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 连胜信息(按类型)Entity
 * @author louie
 * @version 2018-05-21
 */
public class GuesssWinDetail {
	public static final Integer TYPE_MONTH = 1;
	private Long id;
	private Integer type;       //按月
	private String time;       //按月
	private String userId;		// 用户ID
	private Long winCount;		// 连胜次数
	private Date winFromDate;		// 连续开始时间
	private Date winToDate;		// 连续结束时间
	private String winDates;		// 连胜竞猜日期,","隔开
	private Date createTime;		// 创建时间
	private Date updateTime;		// 更新时间
	private String remark;		// 备注
	private String activityCode;
	
	public GuesssWinDetail() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Length(min=1, max=64, message="用户ID长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@NotNull(message="连胜次数不能为空")
	public Long getWinCount() {
		return winCount;
	}

	public void setWinCount(Long winCount) {
		this.winCount = winCount;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="连续开始时间不能为空")
	public Date getWinFromDate() {
		return winFromDate;
	}

	public void setWinFromDate(Date winFromDate) {
		this.winFromDate = winFromDate;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="连续结束时间不能为空")
	public Date getWinToDate() {
		return winToDate;
	}

	public void setWinToDate(Date winToDate) {
		this.winToDate = winToDate;
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
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	@Length(min=0, max=128, message="备注长度必须介于 0 和 128 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getWinDates() {
		return winDates;
	}

	public void setWinDates(String winDates) {
		this.winDates = winDates;
	}

	public String getActivityCode() {
		return activityCode;
	}
	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
}