package com.wlzq.activity.guess51.dto;

/**
 * 概览dto
 * @author 
 * @version 1.0
 */
public class GuessInfoDto {
	public static final Integer NO = 0;
	public static final Integer YES = 1;
	
	/**是否首次登录，0：否，1：是*/
	private Integer isLoginFirst;	
	/**首次登录获取积分*/
	private Integer firstPoint;	
	/**是否当天首次登录，0：否，1：是*/
	private Integer isTodayLoginFirst;	
	/**每日登录获取积分*/
	private Integer loginPoint;	
	/**是否首次，0：否，1：是*/
	private Integer  isGetPrize;	
	/**是否未使用的奖品，0：否，1：是*/
	private Integer  hasNotUsePrize;	
	/**奖励*/
	private GuessPrizeDto  prize;
	/**是否已竞猜,0：否，1：是*/
	private Integer hasGuess;
	
	public Integer getFirstPoint() {
		return firstPoint;
	}
	public void setFirstPoint(Integer firstPoint) {
		this.firstPoint = firstPoint;
	}
	public Integer getLoginPoint() {
		return loginPoint;
	}
	public void setLoginPoint(Integer loginPoint) {
		this.loginPoint = loginPoint;
	}
	public GuessPrizeDto getPrize() {
		return prize;
	}
	public void setPrize(GuessPrizeDto prize) {
		this.prize = prize;
	}
	public Integer getIsGetPrize() {
		return isGetPrize;
	}
	public void setIsGetPrize(Integer isGetPrize) {
		this.isGetPrize = isGetPrize;
	}
	public Integer getIsLoginFirst() {
		return isLoginFirst;
	}
	public void setIsLoginFirst(Integer isLoginFirst) {
		this.isLoginFirst = isLoginFirst;
	}
	public Integer getIsTodayLoginFirst() {
		return isTodayLoginFirst;
	}
	public void setIsTodayLoginFirst(Integer isTodayLoginFirst) {
		this.isTodayLoginFirst = isTodayLoginFirst;
	}
	public Integer getHasNotUsePrize() {
		return hasNotUsePrize;
	}
	public void setHasNotUsePrize(Integer hasNotUsePrize) {
		this.hasNotUsePrize = hasNotUsePrize;
	}

	public Integer getHasGuess() {
		return hasGuess;
	}
	public void setHasGuess(Integer hasGuess) {
		this.hasGuess = hasGuess;
	}
}

