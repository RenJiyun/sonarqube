/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * level2领取活动邀请记录Entity
 * @author louie
 * @version 2018-05-03
 */
public class Level2RecieveInvite {
	public static final Integer STATUS_NO_USER = 1;
	public static final Integer STATUS_OPEN_SUCCESS = 2;
	public static final Integer STATUS_REGIST_BEFORE = 3;
	
	private Long id;
	private String userId;		// 邀请人用户ID
	private String userMobile;		// 邀请人手机号
	private String inviteMobile;		// 被邀请用户手机号
	private Integer status;		// 状态，1：无新户，2：开通成功，3：注册时间早于邀请时间
	private Date createTime;		// 邀请时间
	private String remark;		// 备注
	
	private Date createTimeBegin;
	private Date createTimeEnd;
	
	public Level2RecieveInvite() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Length(min=1, max=64, message="邀请人用户ID长度必须介于 1 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=1, max=11, message="邀请人手机号长度必须介于 1 和 11 之间")
	public String getUserMobile() {
		return userMobile;
	}

	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}
	
	@Length(min=0, max=11, message="被邀请用户手机号长度必须介于 0 和 11 之间")
	public String getInviteMobile() {
		return inviteMobile;
	}

	public void setInviteMobile(String inviteMobile) {
		this.inviteMobile = inviteMobile;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="邀请时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@Length(min=0, max=128, message="备注长度必须介于 0 和 128 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreateTimeBegin() {
		return createTimeBegin;
	}

	public void setCreateTimeBegin(Date createTimeBegin) {
		this.createTimeBegin = createTimeBegin;
	}

	public Date getCreateTimeEnd() {
		return createTimeEnd;
	}

	public void setCreateTimeEnd(Date createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}
	
}