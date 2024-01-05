/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * 竞猜活动奖品Entity
 * @author louie
 * @version 2018-05-25
 */
public class GuesssPrize {
	public static final Integer STATUS_NOT_USED = 0;
	public static final Integer STATUS_USED = 1;
	public static final Integer HAS_POPUP_NO = 0;
	public static final Integer HAS_POPUP_YES = 1;
	
	private Long id;		// id
	private String userId;		// user_id
	private String nickName;		// 昵称
	private Long winId;		// 连胜ID
	private Long prizeId;		// 奖品ID
	private String prizeCode;		// 奖品编码
	private String redeemCode;		// 兑换码
	private String cardNo;		// 卡编码
	private String cardPassword;		// 卡密码
	private Date createTime;		// 创建时间
	private Integer type;		// 类型
	private Integer status;		// 状态，1：未发出，2：已发出, 3:已使用
	private Integer hasPopup;		// 是否已经提示，0：否，1：是
	private Integer winCount;		// 获奖时连胜次数
	private String activityCode;
	public GuesssPrize() {
		super();
	}

	@Length(min=1, max=64, message="user_id长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getRedeemCode() {
		return redeemCode;
	}

	public void setRedeemCode(String redeemCode) {
		this.redeemCode = redeemCode;
	}

	@Length(min=0, max=100, message="卡编码长度必须介于 0 和 100 之间")
	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	
	@Length(min=0, max=100, message="卡密码长度必须介于 0 和 100 之间")
	public String getCardPassword() {
		return cardPassword;
	}

	public void setCardPassword(String cardPassword) {
		this.cardPassword = cardPassword;
	}
	
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@NotNull(message="类型不能为空")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getWinId() {
		return winId;
	}

	public void setWinId(Long winId) {
		this.winId = winId;
	}

	public Long getPrizeId() {
		return prizeId;
	}

	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}

	public String getPrizeCode() {
		return prizeCode;
	}

	public void setPrizeCode(String prizeCode) {
		this.prizeCode = prizeCode;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getHasPopup() {
		return hasPopup;
	}

	public void setHasPopup(Integer hasPopup) {
		this.hasPopup = hasPopup;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getWinCount() {
		return winCount;
	}

	public void setWinCount(Integer winCount) {
		this.winCount = winCount;
	}
	
	public String getActivityCode() {
		return activityCode;
	}
	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
	
}