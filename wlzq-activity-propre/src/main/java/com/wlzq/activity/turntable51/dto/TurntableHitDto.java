package com.wlzq.activity.turntable51.dto;

import lombok.Data;

/**
 * 中奖dto
 * @author 
 * @version 1.0
 */
@Data
public class TurntableHitDto {	
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
	/**价值*/
	private Integer time;
	/**奖品名称*/
	private String prizeName;
	/**剩余免费抽奖机会*/
	private Integer freeCount;
	/**使用积分*/
	private Integer usePoint;
}

