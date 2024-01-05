package com.wlzq.activity.redeem.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 兑换码领取记录Entity
 * @author louie
 * @version 2017-10-17
 */
public class RedeemReceiveRecord  {
	
	private Integer id;
	private String activityCode;		// 编码
	private String typeCode;		// 类型编码
	private String receiveId;		// 活动编码
	private String fundAccount;		// 商品id
	private Date createTime;		// 创建时间
	
	public RedeemReceiveRecord() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Length(min=1, max=64, message="编码长度必须介于 1 和 64 之间")
	public String getActivityCode() {
		return activityCode;
	}

	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
	
	@Length(min=1, max=64, message="类型编码长度必须介于 1 和 64 之间")
	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}
	
	@Length(min=1, max=64, message="活动编码长度必须介于 1 和 64 之间")
	public String getReceiveId() {
		return receiveId;
	}

	public void setReceiveId(String receiveId) {
		this.receiveId = receiveId;
	}
	
	@Length(min=1, max=50, message="商品id长度必须介于 1 和 50 之间")
	public String getFundAccount() {
		return fundAccount;
	}

	public void setFundAccount(String fundAccount) {
		this.fundAccount = fundAccount;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}