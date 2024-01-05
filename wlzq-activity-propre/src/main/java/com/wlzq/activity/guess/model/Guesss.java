/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 竞猜信息Entity
 * @author louie
 * @version 2018-05-21
 */
public class Guesss {
	public static final Integer DIRECTION_UP = 1;
	public static final Integer DIRECTION_DOWN = 0;
	@JsonIgnore
	private Long id;
	@JsonIgnore
	private String userId;		// 用户ID
	private Integer direction;		// 猜涨跌，0：跌，1：涨
	private Integer usePoint;		// 使用积分
	private String guessDate;		// 竞猜交易日期
	private Integer status;		// 结算状态，0：未结算，1：已结算
	private Double ratio;		// 结算赔率
	private Integer resultDirection;		// 结果涨跌，0：趺，1：涨
	private Long winPoint;		// 赢得积分
	@JsonIgnore
	private Date createTime;		// 创建时间
	@JsonIgnore
	private Date updateTime;		// 更新时间
	@JsonIgnore
	private String remark;		// 备注
	
	private String activityCode;
	
	public Guesss() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Length(min=1, max=64, message="用户ID长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@NotNull(message="猜涨跌，0：跌，1：涨不能为空")
	public Integer  getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}
	
	@NotNull(message="使用积分不能为空")
	public Integer getUsePoint() {
		return usePoint;
	}

	public void setUsePoint(Integer usePoint) {
		this.usePoint = usePoint;
	}
	
	@NotNull(message="竞猜交易日期不能为空")
	public String getGuessDate() {
		return guessDate;
	}

	public void setGuessDate(String guessDate) {
		this.guessDate = guessDate;
	}
	
	public Double getRatio() {
		return ratio;
	}

	public void setRatio(Double ratio) {
		this.ratio = ratio;
	}
	
	public Integer getResultDirection() {
		return resultDirection;
	}

	public void setResultDirection(Integer resultDirection) {
		this.resultDirection = resultDirection;
	}
	
	public Long getWinPoint() {
		return winPoint;
	}

	public void setWinPoint(Long winPoint) {
		this.winPoint = winPoint;
	}
	
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
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

	public Integer getStatus() {
		return this.ratio == null?0:1;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getActivityCode() {
		return activityCode;
	}
	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
	
}