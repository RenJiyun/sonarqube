package com.wlzq.activity.voteworks.dto;

/**
 * 点赞dto
 * @author 
 * @version 1.0
 */
public class LikeResultDto {
	private Integer status;		// 是否可抽奖，0：否，1：是
	private Integer likeCount;		// 当天点赞次数
	private String lotteryCode;         //抽奖码
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getLotteryCode() {
		return lotteryCode;
	}
	public void setLotteryCode(String lotteryCode) {
		this.lotteryCode = lotteryCode;
	}
	public Integer getLikeCount() {
		return likeCount;
	}
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	
}

