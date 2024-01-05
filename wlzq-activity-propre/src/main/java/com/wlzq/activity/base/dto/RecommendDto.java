package com.wlzq.activity.base.dto;

public class RecommendDto {
	private Integer openStatus;
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
	
	private Integer recommendStatus;		

	private String recommendMobile;		// 推荐人手机号
	private String recommendName;		// 推荐人姓名
	private String recommendOfficeName;		// 推荐人营业部名称
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

	public String getRecommendMobile() {
		return recommendMobile;
	}
	public void setRecommendMobile(String recommendMobile) {
		this.recommendMobile = recommendMobile;
	}
	public String getRecommendName() {
		return recommendName;
	}
	public void setRecommendName(String recommendName) {
		this.recommendName = recommendName;
	}
	public String getRecommendOfficeName() {
		return recommendOfficeName;
	}
	public void setRecommendOfficeName(String recommendOfficeName) {
		this.recommendOfficeName = recommendOfficeName;
	}
	public Integer getOpenStatus() {
		return openStatus;
	}
	public void setOpenStatus(Integer openStatus) {
		this.openStatus = openStatus;
	}
	public Integer getRecommendStatus() {
		return recommendStatus;
	}
	public void setRecommendStatus(Integer recommendStatus) {
		this.recommendStatus = recommendStatus;
	}
	
}

