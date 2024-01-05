package com.wlzq.activity.voteworks.dto;

/**
 * 留言dto
 * @author 
 * @version 1.0
 */
public class VoteOverviewDto {	
	private Long likeCount;		// 开始ID
	private Long hot;         //热度
	private Integer popStatus;		// 点赞是否弹窗提示，0：否，1：是
	private Long leftTime;      //活动结束倒计时
	public Long getLikeCount() {
		return likeCount;
	}
	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}
	public Long getHot() {
		return hot;
	}
	public void setHot(Long hot) {
		this.hot = hot;
	}
	public Integer getPopStatus() {
		return popStatus;
	}
	public void setPopStatus(Integer popStatus) {
		this.popStatus = popStatus;
	}
	public Long getLeftTime() {
		return leftTime;
	}
	public void setLeftTime(Long leftTime) {
		this.leftTime = leftTime;
	}
	
}

