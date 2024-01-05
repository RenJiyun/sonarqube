package com.wlzq.activity.base.dto;

import java.util.Date;

/**
 * 首页奖品列表dto
 * @author 
 * @version 1.0
 */
public class UserPrizeDto {	
	public static final Integer STATUS_NOT_SEND = 1;
	public static final Integer STATUS_SEND = 2;
	public static final Integer STATUS_USED = 3;
	public static final Integer SOURCE_GUESS = 1;		// 来源，猜涨跌
	public static final  Integer SOURCE_TURNTABLE = 2;		// 来源，大转盘
	public static final  Integer SOURCE_EXPO_TURNTABLE = 3;		// 来源，大转盘
	private Integer type;		// 类型,1:兑换码，2：京东卡，3：积分
	private String redeemCode;		// 兑换码
	private Integer time;		// 使用时间（月）
	private Double worth;		// 价值 
	private String cardNo;		// 卡编码
	private String cardPassword;		// 卡密码
	private Date createTime;		// 获取时间
	private Integer status;		// 状态，1：未发出，2：已领取, 3:已使用
	private Integer source;		// 来源，1：猜涨跌，2：大转盘，3：金交会
	private String sourceText;		// 来源文本
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getRedeemCode() {
		return redeemCode;
	}
	public void setRedeemCode(String redeemCode) {
		this.redeemCode = redeemCode;
	}
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	public Double getWorth() {
		return worth;
	}
	public void setWorth(Double worth) {
		this.worth = worth;
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
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getSource() {
		return source;
	}
	public void setSource(Integer source) {
		this.source = source;
	}
	public String getSourceText() {
		return source.equals(SOURCE_GUESS)?"全民猜涨跌":
			source.equals(SOURCE_TURNTABLE)?"幸运大转盘":
			source.equals(SOURCE_EXPO_TURNTABLE)?"金交会幸运大转盘":"未知";
	}
	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}
	
}

