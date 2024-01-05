/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.brokeragemeeting.model;

import java.util.Date;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 经纪业务会议人员列表Entity
 * @author cjz
 * @version 2019-01-17
 */
public class ActBrokeragemeetingPersonnel {
	
	@JsonIgnore
	private Integer id;
	@JsonIgnore
	private String userId;		// user_Id
	@JsonIgnore
	private String openId;		// openid
	private String name;		// 姓名
	private String nickName;		// 昵称
	private String headImageUrl;		// 头像地址
	@JsonIgnore
	private Integer isDeleted;		// 是否删除,0:否，1：是
	@JsonIgnore
	private String department;		// 部门
	@JsonIgnore
	private Integer isLigtedUp;		// 是否点亮
	@JsonIgnore
	private Integer isNeedUnveiling;		// 是否需要揭幕
	private String signOrder;		// 签到序号
	@JsonIgnore
	private Integer isUnveiled;		// 是否已揭幕
	
	/** 签到时间 */
	@JsonIgnore
	private Date signinTime;    // 签到时间
	@JsonIgnore
	private String actCode;		// 活动编码
	@JsonIgnore
	private Integer signinStatus;		// 是否有效,1:有效,0:无效
	
	public static final Integer ISDELETE_NO = 0;
	public static final Integer ISDELETE_YES = 1;
	
	public static final Integer ISLIGHTEDUP_NO = 0;
	public static final Integer ISLIGHTEDUP_YES = 1;
	
	public static final Integer ISNEEDUNVEILING_NO = 0;
	public static final Integer ISNEEDUNVEILING_YES = 1;
	
	public static final Integer ISUNVEILED_NO = 0;
	public static final Integer ISUNVEILED_YES = 1;
	
	public ActBrokeragemeetingPersonnel() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Length(min=0, max=64, message="user_Id长度必须介于 0 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=0, max=100, message="openid长度必须介于 0 和 100 之间")
	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	@Length(min=0, max=100, message="姓名长度必须介于 0 和 100 之间")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Length(min=0, max=100, message="昵称长度必须介于 0 和 100 之间")
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	@Length(min=0, max=500, message="头像地址长度必须介于 0 和 500 之间")
	public String getHeadImageUrl() {
		return headImageUrl;
	}

	public void setHeadImageUrl(String headImageUrl) {
		this.headImageUrl = headImageUrl;
	}
	
	public Integer getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	@Length(min=0, max=100, message="部门长度必须介于 0 和 100 之间")
	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
	
	public Integer getIsLigtedUp() {
		return isLigtedUp;
	}

	public void setIsLigtedUp(Integer isLigtedUp) {
		this.isLigtedUp = isLigtedUp;
	}
	
	public Integer getIsNeedUnveiling() {
		return isNeedUnveiling;
	}

	public void setIsNeedUnveiling(Integer isNeedUnveiling) {
		this.isNeedUnveiling = isNeedUnveiling;
	}
	
	@Length(min=0, max=20, message="签到序号长度必须介于 0 和 20 之间")
	public String getSignOrder() {
		return signOrder;
	}

	public void setSignOrder(String signOrder) {
		this.signOrder = signOrder;
	}
	
	public Integer getIsUnveiled() {
		return isUnveiled;
	}

	public void setIsUnveiled(Integer isUnveiled) {
		this.isUnveiled = isUnveiled;
	}

	public Date getSigninTime() {
		return signinTime;
	}

	public void setSigninTime(Date signinTime) {
		this.signinTime = signinTime;
	}

	public String getActCode() {
		return actCode;
	}

	public void setActCode(String actCode) {
		this.actCode = actCode;
	}

	public Integer getSigninStatus() {
		return signinStatus;
	}

	public void setSigninStatus(Integer signinStatus) {
		this.signinStatus = signinStatus;
	}
	
}