package com.wlzq.activity.turntable.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 奖品列表dto
 * @author 
 * @version 1.0
 */
public class TurntableePrizeDto {
	/** 类型，1：兑换码，2：卡密码，3：积分 */
	@JsonIgnore
	private Integer type;
	/** 价值，type=3时为积分 */
	@JsonIgnore
	private Integer worth;
	/**手机*/
	private String mobile;	
	/**奖品名称*/
	private String prizeName;
	
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getWorth() {
		return worth;
	}
	public void setWorth(Integer worth) {
		this.worth = worth;
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
	
}

