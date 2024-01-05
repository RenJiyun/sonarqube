package com.wlzq.activity.redeem.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 兑换码类型管理Entity
 * @author louie
 * @version 2017-10-10
 */
public class RedeemType  {

	private Integer id;
	private String code;		// 编号
	private String name;		// 名称
	private Integer validityType;		// 有效期类型，1：日期范围，2：下发起天数
	private Date validityDateFrom;		// 有效期开始时间
	private Date validityDateTo;		// 有效期结束时间
	private Integer validityDay;		// 有效期天数
	private String remark;		// 备注
	
	public RedeemType() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Length(min=1, max=50, message="编号长度必须介于 1 和 50 之间")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Length(min=1, max=50, message="名称长度必须介于 1 和 50 之间")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@NotNull(message="有效期类型，1：日期范围，2：下发起天数不能为空")
	public Integer getValidityType() {
		return validityType;
	}

	public void setValidityType(Integer validityType) {
		this.validityType = validityType;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getValidityDateFrom() {
		return validityDateFrom;
	}

	public void setValidityDateFrom(Date validityDateFrom) {
		this.validityDateFrom = validityDateFrom;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
	
	@Length(min=0, max=255, message="备注长度必须介于 0 和 255 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}