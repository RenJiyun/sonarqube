package com.wlzq.activity.guess.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wlzq.common.utils.ObjectUtils;

/**
 * 连胜榜dto
 * @author 
 * @version 1.0
 */
public class WinRanksDto {	
	/**排名*/
	private Long order;		
	/**手机*/
	@JsonIgnore
	private String mobile;	
	/**昵称*/
	private String nickName;	
	/**头像*/
	private String portrait;
	/**连胜次数*/
	private Long winCount;
	/**活动编码**/
	private String activitCode;
	public Long getOrder() {
		return order;
	}
	public void setOrder(Long order) {
		this.order = order;
	}
	public String getNickName() {
		if(ObjectUtils.isEmptyOrNull(this.nickName) && 
				ObjectUtils.isNotEmptyOrNull(this.mobile)) {
			return this.mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
		}
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getPortrait() {
		return portrait;
	}
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
	public Long getWinCount() {
		return winCount;
	}
	public void setWinCount(Long winCount) {
		this.winCount = winCount;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getActivitCode() {
		return activitCode;
	}
	public void setActivitCode(String activitCode) {
		this.activitCode = activitCode;
	}
	
}

