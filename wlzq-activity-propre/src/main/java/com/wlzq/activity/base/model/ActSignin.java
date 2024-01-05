package com.wlzq.activity.base.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 活动签到Entity
 * @author cjz
 * @version 2018-06-12
 */
public class ActSignin {
	
	/** id */
	@JsonIgnore
	private Integer id;
	/** user_Id */
	@JsonIgnore
	private String userId;
	/** 活动编码 */
	@JsonIgnore
	private String actCode;
	/** 签到时间 */
	@JsonIgnore
	private Date signinTime;
	/** 是否有效,1:有效,0:无效 */
	@JsonIgnore
	private Integer status;
	/** 签到码 */
	private String signInCode;
	
	/** 微信昵称 */
	private String nickName;
	/** 微信头像 */
	private String headImageUrl;
	
	/** 是否有效,1:有效 */
	public static final Integer STATUS_VALID = 1;
	/** 是否有效,0:无效 */
	public static final Integer STATUS_INVALID = 0;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getActCode() {
		return actCode;
	}

	public void setActCode(String actCode) {
		this.actCode = actCode;
	}
	
	public Date getSigninTime() {
		return signinTime;
	}

	public void setSigninTime(Date signinTime) {
		this.signinTime = signinTime;
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getSignInCode() {
		return signInCode;
	}

	public void setSignInCode(String signInCode) {
		this.signInCode = signInCode;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getHeadImageUrl() {
		return headImageUrl;
	}

	public void setHeadImageUrl(String headImageUrl) {
		this.headImageUrl = headImageUrl;
	}
	
}