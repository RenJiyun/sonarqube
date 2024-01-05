/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.checkin.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 签到次数Entity
 * @author louie
 * @version 2018-03-14
 */
public class CheckInStatus  {
	private Long id; //id
	private String userId;		// 用户ID
	private String openid;		// openid
	private Long totalCount;		// 总次数
	private Long continuousCount;		// 连续次数
	private Date continuousCountDate;		// 上次连续次数计算时间
	private Date createTime;		// 创建时间
	private Date updateTime;		// 更新时间
	private String prize;		// 奖品
	private Integer prizeType;		// 奖品类型，1：Level2兑换码
	
	public CheckInStatus() {
		super();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	@NotNull(message="总次数不能为空")
	public Long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}
	
	@NotNull(message="连续次数不能为空")
	public Long getContinuousCount() {
		return continuousCount;
	}

	public void setContinuousCount(Long continuousCount) {
		this.continuousCount = continuousCount;
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

	public String getPrize() {
		return prize;
	}

	public void setPrize(String prize) {
		this.prize = prize;
	}

	public Integer getPrizeType() {
		return prizeType;
	}

	public void setPrizeType(Integer prizeType) {
		this.prizeType = prizeType;
	}

	public Date getContinuousCountDate() {
		return continuousCountDate;
	}

	public void setContinuousCountDate(Date continuousCountDate) {
		this.continuousCountDate = continuousCountDate;
	}
}