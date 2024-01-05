package com.wlzq.activity.voteworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 中奖dto
 * @author 
 * @version 1.0
 */
public class VoteLotteryDto {	
	public static final Integer TYPE_LEVEL2_1 = 1;
	public static final Integer TYPE_JDC_8 = 2;
	public static final Integer TYPE_JDC_20 = 3;
	public static final Integer TYPE_JDC_50 = 4;
	public static final Integer TYPE_JDC_88 = 5;
	public static final Integer TYPE_AQYM = 6;
	public static final Integer TYPE_THANKS = 7;
	/**状态，0：未中奖，1：中奖*/
	private Integer status;	
	/**抽奖ID*/
	private String lotteryCode;	
	/**奖品类型*/
	private Integer type;
	@JsonIgnore
	/**价值*/
	private Double worth;
	/**奖品名称*/
	private String prizeName;
	/**兑换码**/
	private String redeemCode;
	
	public String getRedeemCode() {
		return redeemCode;
	}
	
	public void setRedeemCode(String redeemCode) {
		this.redeemCode = redeemCode;
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getPrizeName() {
		return prizeName;
	}
	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}
	public Double getWorth() {
		return worth;
	}
	public void setWorth(Double worth) {
		this.worth = worth;
	}
	public String getLotteryCode() {
		return lotteryCode;
	}
	public void setLotteryCode(String lotteryCode) {
		this.lotteryCode = lotteryCode;
	}
	
}

