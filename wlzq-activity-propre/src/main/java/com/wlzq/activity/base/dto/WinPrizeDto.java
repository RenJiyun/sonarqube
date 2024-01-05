package com.wlzq.activity.base.dto;

/**
 * 中奖dto
 * @author 
 * @version 1.0
 */
public class WinPrizeDto {	
	public final static Integer STATUS_YES = 1;
	public final static Integer STATUS_NO = 2;
	public final static Integer TYPE_LEVEL2_1 = 1;
	public final static Integer TYPE_LEVEL2_3 = 2;
	private Integer status;		// 是否中奖，0：否，1：是
	private Integer type;		// 奖品，1：level2 一个月，2：level2 三个月
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
	
}

