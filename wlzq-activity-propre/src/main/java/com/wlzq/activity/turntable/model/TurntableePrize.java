/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.turntable.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 大转盘奖品Entity
 * @author louie
 * @version 2018-05-30
 */
public class TurntableePrize {
	private String userId;		// user_id
	private String mobile;		// 用户手机
	private String redeemCode;		// 兑换码
	private Double worth;		// 积分
	private Integer type;		// 类型,1:兑换码，2：京东卡，3：积分
	private Long turntableId;		// 转盘记录id
	private Long prizeId;		// 奖品ID
	private String prizeName;
	private String prizeCode;		// 奖品编码
	private Date createTime;		// 创建时间
	private Integer time;
	
	public TurntableePrize() {
		super();
	}

	@Length(min=1, max=64, message="user_id长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=0, max=100, message="兑换码长度必须介于 0 和 100 之间")
	public String getRedeemCode() {
		return redeemCode;
	}

	public void setRedeemCode(String redeemCode) {
		this.redeemCode = redeemCode;
	}
	
	public Double getWorth() {
		return worth;
	}

	public void setWorth(Double worth) {
		this.worth = worth;
	}
	
	@NotNull(message="类型,1:兑换码，2：京东卡，3：积分不能为空")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	@NotNull(message="转盘记录id不能为空")
	public Long getTurntableId() {
		return turntableId;
	}

	public void setTurntableId(Long turntableId) {
		this.turntableId = turntableId;
	}
	
	public Long getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}
	
	@Length(min=1, max=100, message="奖品编码长度必须介于 1 和 100 之间")
	public String getPrizeCode() {
		return prizeCode;
	}

	public void setPrizeCode(String prizeCode) {
		this.prizeCode = prizeCode;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String getPrizeName() {
		return prizeName;
	}
	
	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}
	
	public Integer getTime() {
		return time;
	}
	
	public void setTime(Integer time) {
		this.time = time;
	}
	
}