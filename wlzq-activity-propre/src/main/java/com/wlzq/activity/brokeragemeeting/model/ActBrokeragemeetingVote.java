/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.brokeragemeeting.model;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 经纪业务会议游戏投票Entity
 * @author cjz
 * @version 2019-01-21
 */
public class ActBrokeragemeetingVote {
	
	@JsonIgnore
	private Integer id;
	@JsonIgnore
	private String userId;		// user_Id
	private String word;		// 投票关键词
	private Integer voteCount;		// 投票次数
	@JsonIgnore
	private Integer status;		// 状态
	@JsonIgnore
	private String name;		// 姓名
	@JsonIgnore
	private String nickName;		// 昵称
	
	public static final Integer STATUS_VALID = 1;
	public static final Integer STATUS_INVALID = 0;
	
	public ActBrokeragemeetingVote() {
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
	
	@Length(min=0, max=64, message="投票关键词长度必须介于 0 和 64 之间")
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
	
	public Integer getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(Integer voteCount) {
		this.voteCount = voteCount;
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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
	
}