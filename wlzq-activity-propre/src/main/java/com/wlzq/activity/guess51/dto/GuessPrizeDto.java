package com.wlzq.activity.guess51.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 首页奖品列表dto
 * @author 
 * @version 1.0
 */
public class GuessPrizeDto {	
	@JsonIgnore
	private Long id;
	/**昵称*/
	private String nickName;	
	/**奖品名称*/
	private String prizeName;
	/**连胜次数*/
	private Long winCount;
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
	
}

