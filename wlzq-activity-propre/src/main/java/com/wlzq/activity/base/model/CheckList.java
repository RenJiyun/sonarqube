/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 活动审核名单Entity
 * @author louie
 * @version 2018-05-09
 */
public class CheckList  {
	public static final Integer SOURCE_ANSWER = 1;
	public static final Integer STATUS_PASS = 1;
	public static final Integer STATUS_NOT_PASS = 2;
	private String userId;		// 用户ID
	private String mobile;		// 手机
	private Integer sourceu;		// 来源,1:答题有奖
	private Integer status;		// 状态,1:审核通过,2:审核失败
	private String remark;		// 备注
	private Date createTime;		// 创建时间
	
	public CheckList() {
		super();
	}

	@Length(min=1, max=64, message="用户ID长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=0, max=11, message="手机长度必须介于 0 和 11 之间")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	@NotNull(message="来源,1:答题有奖不能为空")
	public Integer getSourceu() {
		return sourceu;
	}

	public void setSourceu(Integer sourceu) {
		this.sourceu = sourceu;
	}
	
	@NotNull(message="状态,1:审核通过,2:审核失败不能为空")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@Length(min=0, max=256, message="备注长度必须介于 0 和 256 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}