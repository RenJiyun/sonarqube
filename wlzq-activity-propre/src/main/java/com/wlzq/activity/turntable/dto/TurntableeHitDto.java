package com.wlzq.activity.turntable.dto;

/**
 * 中奖dto
 * @author 
 * @version 1.0
 */
public class TurntableeHitDto {	
	public static final Integer STATUS_NOT_HIT = 1;
	public static final Integer STATUS_HIT = 2;
	public static final Integer STATUS_NO_POINT = 3;
	public static final Integer TYPE_LEVEL2_1 = 1;
	public static final Integer TYPE_LEVEL2_3 = 2;
	public static final Integer TYPE_POINT_50 = 3;
	public static final Integer TYPE_POINT_100 = 4;
	/**状态，1：未中奖，2：中奖，3：积分不足*/
	private Integer status;	
	/**奖品类型*/
	private Integer type;
	/**价值*/
	private Integer worth;
	/**奖品名称*/
	private String prizeName;
	/**剩余免费抽奖机会*/
	private Integer freeCount;
	/**使用积分*/
	private Integer usePoint;
	/**level2 月数**/
	private Integer time;
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getPrizeName() {
		return prizeName;
	}
	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}
	public Integer getWorth() {
		return worth;
	}
	public void setWorth(Integer worth) {
		this.worth = worth;
	}
	public Integer getFreeCount() {
		return freeCount;
	}
	public void setFreeCount(Integer freeCount) {
		this.freeCount = freeCount;
	}
	public Integer getUsePoint() {
		return usePoint;
	}
	public void setUsePoint(Integer usePoint) {
		this.usePoint = usePoint;
	}
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
}

