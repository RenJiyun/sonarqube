/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.voteworks.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 抽奖记录Entity
 * @author louie
 * @version 2018-08-10
 */
public class VoteWorksLottery {
	private Long  id;
	private String lotteryCode;		// 抽奖码
	private String userId;		// 用户ID
	private String openId;		// openid
	private Integer status;		// 状态，0：未抽奖，1：已抽奖
	private Integer isHit;		// 是否中奖，0：否，1：是
	private Long prizeId;		// 奖品ID
	private Integer prizeType;		// // 类型，1：兑换码，2：卡密码，3：积分
	private String mobile;		// 发送奖品信息手机号
	private String prizeName;		// 奖品名称
	private String redeemCode;		// 兑换码
	private String cardNo;		// 卡编码
	private String cardPassword;		// 卡密码
	private Integer sendStatus;		// 是否已发送中奖信息，0：否，1：是
	private Date createTime;		// 创建时间
	private Date updateTime;		// 更新时间
	private Date createTimeFrom;		// 创建开始时间
	private Date createTimeTo;		// 创建结束时间
	@JsonIgnore
	private String activitycode;	// 活动编码
	
	
	public VoteWorksLottery() {
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
	
	@Length(min=1, max=64, message="openid长度必须介于 1 和 64 之间")
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	@NotNull(message="是否中奖，0：否，1：是不能为空")
	public Integer getIsHit() {
		return isHit;
	}

	public void setIsHit(Integer isHit) {
		this.isHit = isHit;
	}
	
	public Long getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}
	
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTimeFrom() {
		return createTimeFrom;
	}

	public void setCreateTimeFrom(Date createTimeFrom) {
		this.createTimeFrom = createTimeFrom;
	}

	public Date getCreateTimeTo() {
		return createTimeTo;
	}

	public void setCreateTimeTo(Date createTimeTo) {
		this.createTimeTo = createTimeTo;
	}

	public Integer getSendStatus() {
		return sendStatus;
	}

	public void setSendStatus(Integer sendStatus) {
		this.sendStatus = sendStatus;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getLotteryCode() {
		return lotteryCode;
	}

	public void setLotteryCode(String lotteryCode) {
		this.lotteryCode = lotteryCode;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getPrizeName() {
		return prizeName;
	}

	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}

	public String getRedeemCode() {
		return redeemCode;
	}

	public void setRedeemCode(String redeemCode) {
		this.redeemCode = redeemCode;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardPassword() {
		return cardPassword;
	}

	public void setCardPassword(String cardPassword) {
		this.cardPassword = cardPassword;
	}

	public Integer getPrizeType() {
		return prizeType;
	}

	public void setPrizeType(Integer prizeType) {
		this.prizeType = prizeType;
	}
	
	public String getActivitycode() {
		return activitycode;
	}
	
	public void setActivitycode(String activitycode) {
		this.activitycode = activitycode;
	}
	
}