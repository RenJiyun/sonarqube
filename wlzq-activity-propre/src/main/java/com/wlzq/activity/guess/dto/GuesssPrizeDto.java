package com.wlzq.activity.guess.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 首页奖品列表dto
 * @author 
 * @version 1.0
 */
public class GuesssPrizeDto {	
	@JsonIgnore
	private Long id;
	/**昵称*/
	private String nickName;	
	/**奖品名称*/
	private String prizeName;
	/**连胜次数*/
	private Long winCount;
	/**活动代码**/
	private String activityCode;
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getPrizeName() {
		return prizeName;
	}
	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}
	public Long getWinCount() {
		return winCount;
	}
	public void setWinCount(Long winCount) {
		this.winCount = winCount;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getActivityCode() {
		return activityCode;
	}
	public void setActivityCode(String activityCode) {
		this.activityCode = activityCode;
	}
}

