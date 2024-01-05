package com.wlzq.activity.base.dto;

/**
 * 中奖列表dto
 * @author 
 * @version 1.0
 */
public class WinDto {	
	private String showUserName;		// 显示用户名
	private String prizeName;		// 奖品
	public String getShowUserName() {
		return showUserName;
	}
	public void setShowUserName(String showUserName) {
		this.showUserName = showUserName;
	}
	public String getPrizeName() {
		return prizeName;
	}
	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}
	
}

