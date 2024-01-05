package com.wlzq.activity.base.model;

import java.io.Serializable;

/**
 * Activity 实体类
 * @author 
 * @version 1.0
 */
public class Activity implements Serializable{
	private static final long serialVersionUID = 1854235331253L;
	public static final Integer STATUS_CLOSE = 0;
	public static final Integer STATUS_OPEN = 1;
	public static final Integer BREAKER_STATUS_NO = 0;
	public static final Integer BREAKER_STATUS_YES = 1;
	/**id*/
	private java.lang.Integer id;	
	/**code*/
	private java.lang.String code;
	private String groupCode;
	/**name*/
	private java.lang.String name;	
	/**createTime*/
	private java.util.Date createTime;	
	/**dateFrom*/
	private java.util.Date dateFrom;	
	/**dateTo*/
	private java.util.Date dateTo;	
	private String timeFrom;		// 每天活动开始时间
	private String timeTo;		// 每天活动结束时间
	/**isDeleted*/
	private Integer isDeleted;	
	/**remark*/
	private java.lang.String remark;	
	/**status*/
	private Integer status;	
	/**totalNum*/
	private Integer totalNum;	
	/**isRebuild*/
	private Integer isRebuild;	
	private String breakerTimeFrom;		// 熔断开始时间
	private String breakerTimeTo;		// 熔断结束时间
	private Long breakerNumber;		// 熔断参与量
	private Integer breakerStatus;		// 是否熔断，0：否，1：是

	/**
	 * 一个手机号可以对应多少个手机号参与活动
	 */
	private Integer customerMobileTimes;

	public java.lang.Integer getId() {
		return id;
	}

	public void setId(java.lang.Integer id) {
		this.id = id;
	}

	public java.lang.String getCode() {
		return code;
	}

	public void setCode(java.lang.String code) {
		this.code = code;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}
	public java.lang.String getName() {
		return this.name;
	}
	
	public void setName(java.lang.String value) {
		this.name = value;
	}

	public java.util.Date getCreateTime() {
		return this.createTime;
	}
	
	public void setCreateTime(java.util.Date value) {
		this.createTime = value;
	}

	public java.util.Date getDateFrom() {
		return this.dateFrom;
	}
	
	public void setDateFrom(java.util.Date value) {
		this.dateFrom = value;
	}

	public java.util.Date getDateTo() {
		return this.dateTo;
	}
	
	public void setDateTo(java.util.Date value) {
		this.dateTo = value;
	}

	public Integer getIsDeleted() {
		return this.isDeleted;
	}
	
	public void setIsDeleted(Integer value) {
		this.isDeleted = value;
	}

	public java.lang.String getRemark() {
		return this.remark;
	}
	
	public void setRemark(java.lang.String value) {
		this.remark = value;
	}

	public Integer getStatus() {
		return this.status;
	}
	
	public void setStatus(Integer value) {
		this.status = value;
	}

	public Integer getTotalNum() {
		return this.totalNum;
	}
	
	public void setTotalNum(Integer value) {
		this.totalNum = value;
	}

	public Integer getIsRebuild() {
		return this.isRebuild;
	}
	
	public void setIsRebuild(Integer value) {
		this.isRebuild = value;
	}

	public String getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(String timeFrom) {
		this.timeFrom = timeFrom;
	}

	public String getTimeTo() {
		return timeTo;
	}

	public void setTimeTo(String timeTo) {
		this.timeTo = timeTo;
	}

	public String getBreakerTimeFrom() {
		return breakerTimeFrom;
	}

	public void setBreakerTimeFrom(String breakerTimeFrom) {
		this.breakerTimeFrom = breakerTimeFrom;
	}

	public String getBreakerTimeTo() {
		return breakerTimeTo;
	}

	public void setBreakerTimeTo(String breakerTimeTo) {
		this.breakerTimeTo = breakerTimeTo;
	}

	public Long getBreakerNumber() {
		return breakerNumber;
	}

	public void setBreakerNumber(Long breakerNumber) {
		this.breakerNumber = breakerNumber;
	}

	public Integer getBreakerStatus() {
		return breakerStatus;
	}

	public void setBreakerStatus(Integer breakerStatus) {
		this.breakerStatus = breakerStatus;
	}

	public Integer getCustomerMobileTimes() {
		return customerMobileTimes;
	}

	public void setCustomerMobileTimes(Integer customerMobileTimes) {
		this.customerMobileTimes = customerMobileTimes;
	}
}

