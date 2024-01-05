package com.wlzq.activity.redeem.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * 兑换码管理Entity
 * @author louie
 * @version 2017-10-10
 */
public class Redeem {

	private Integer id;
	private String code;		// 编号
	private String typeCode;		// 编码
	private String activityCode;		// 活动编码
	private Integer goodsId;		// 商品id
	private String mobile;		// 手机号
	private String orderNo;		// 订单号
	private String userId;		// 订单号
	private String openId;		// 编码
	private Integer notityStatus;		// ths通知状态，0：失败，1：成功
	private String notityMessage;		// ths通知提示
	private Date createTime;		// 兑换码发出时间
	private Date outTime;		// 兑换码发出时间
	private Date takeTime;		// 领取时间
	private Integer status;		//状态，0：未占用，1：已占用
	private String recommendMobile;		// 推荐人手机号
	private String recommendName;		// 推荐人姓名
	private String recommendOfficeName;		// 推荐人营业部名称
	private Integer recommendOfficeId;		// 推荐人营业部ID
	private Integer validityType;		// 有效期类型，1：日期范围，2：下发起天数
	private Date validityDateFrom;		// 有效期开始时间
	private Date validityDateTo;		// 有效期结束时间
	private Integer validityDay;		// 有效期天数
	/**goodsId*/
	private String goodsName;
	/**goodsTime*/
	private Integer goodsTime;
	/**timeType*/
	private Integer timeType;		
	
	public Redeem() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	@Length(min=1, max=50, message="活动编码长度必须介于 1 和 50 之间")
	public String getActivityCode() {
		return activityCode;
	}

	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
	
	@NotNull(message="商品id不能为空")
	public Integer getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
	}

	@Length(min=0, max=11, message="手机号长度必须介于 0 和 11 之间")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	@Length(min=0, max=100, message="订单号长度必须介于 0 和 100 之间")
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	public Integer getNotityStatus() {
		return notityStatus;
	}

	public void setNotityStatus(Integer notityStatus) {
		this.notityStatus = notityStatus;
	}
	
	public String getNotityMessage() {
		return notityMessage;
	}

	public void setNotityMessage(String notityMessage) {
		this.notityMessage = notityMessage;
	}
	
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public Date getOutTime() {
		return outTime;
	}

	public void setOutTime(Date outTime) {
		this.outTime = outTime;
	}
	
	public Date getTakeTime() {
		return takeTime;
	}

	public void setTakeTime(Date takeTime) {
		this.takeTime = takeTime;
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
	
	public Integer getRecommendOfficeId() {
		return recommendOfficeId;
	}

	public void setRecommendOfficeId(Integer recommendOfficeId) {
		this.recommendOfficeId = recommendOfficeId;
	}

	public Integer getValidityType() {
		return validityType;
	}

	public void setValidityType(Integer validityType) {
		this.validityType = validityType;
	}

	public Date getValidityDateFrom() {
		return validityDateFrom;
	}

	public void setValidityDateFrom(Date validityDateFrom) {
		this.validityDateFrom = validityDateFrom;
	}

	public Date getValidityDateTo() {
		return validityDateTo;
	}

	public void setValidityDateTo(Date validityDateTo) {
		this.validityDateTo = validityDateTo;
	}

	public Integer getValidityDay() {
		return validityDay;
	}

	public void setValidityDay(Integer validityDay) {
		this.validityDay = validityDay;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
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
	
}