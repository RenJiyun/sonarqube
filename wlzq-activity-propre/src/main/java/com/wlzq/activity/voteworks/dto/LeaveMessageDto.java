package com.wlzq.activity.voteworks.dto;

/**
 * 点赞dto
 * @author 
 * @version 1.0
 */
public class LeaveMessageDto {
	private String nickname;         //昵称
	private String portrait;          //头像
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getPortrait() {
		return portrait;
	}
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
	
}

