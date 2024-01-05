package com.wlzq.activity.redeem.dto;

public class RedeemDto {
	/**
	 * 状态，0:未使用，1：已使用
	 */
	private Integer status;
	/**
	 * 兑换码
	 */
	private String code;
	
	public RedeemDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RedeemDto(Integer status, String code) {
		super();
		this.status = status;
		this.code = code;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}

