package com.wlzq.activity.base.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 活动留言Entity
 * @author cjz
 * @version 2018-06-12
 */
public class ActMessage {
	
	/** id */
	private Integer id;
	/** user_Id */
	@JsonIgnore
	private String userId;
	/** 活动编码 */
	@JsonIgnore
	private String actCode;
	/** 留言时间 */
	@JsonIgnore
	private Date msgTime;
	/** 是否有效,1:有效,0:无效 */
	@JsonIgnore
	private Integer status;
	/** 留言内容 */
	private String content;
	
	/** 微信昵称 */
	private String nickName;
	/** 微信头像 */
	private String headImageUrl;
	
	/** 是否有效,1:有效 */
	public static final Integer STATUS_VALID = 1;
	/** 是否有效,0:无效 */
	public static final Integer STATUS_INVALID = 0;
	
	public static final Integer CONTENT_LEN = 500;
	
	
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
	
	public Date getMsgTime() {
		return msgTime;
	}

	public void setMsgTime(Date msgTime) {
		this.msgTime = msgTime;
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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