package com.wlzq.activity.voteworks.dto;

import java.util.Date;

/**
 * 点赞dto
 * @author 
 * @version 1.0
 */
public class LikeDto {
	private String nickname;         //昵称
	private Date date;          //最新点赞时间
	private Long likeCount;		// 开始ID
	public Long getLikeCount() {
		return likeCount;
	}
	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}

