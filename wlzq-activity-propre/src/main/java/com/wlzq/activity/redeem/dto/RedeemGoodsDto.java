package com.wlzq.activity.redeem.dto;

public class RedeemGoodsDto {
	private Integer status;
	/**
	 * 商品描述
	 */
	private String goodsName;
	/**
	 * 商品时间
	 */
	private Integer goodsTime;
	/**
	 * 时间类型
	 */
	private Integer timeType;
	
	public RedeemGoodsDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public Integer getGoodsTime() {
		return goodsTime;
	}
	public void setGoodsTime(Integer goodsTime) {
		this.goodsTime = goodsTime;
	}
	public Integer getTimeType() {
		return timeType;
	}
	public void setTimeType(Integer timeType) {
		this.timeType = timeType;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}

