/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.turntable.model;

import java.util.List;

import lombok.Data;

/**
 * 大转盘参数Enum
 * @author zhaozx
 * @version 2021-01-25
 */
@Data
public class TurntableeParam {
	
	public static final Integer TURN_MODEL_BALLPOOL = 1;
	public static final Integer TURN_MODEL_DICE = 2;
	public static final Integer UNIQUE_BY_ACTIVITY = 1;
	public static final Integer UNIQUE_BY_PRIZETYPE = 2;
	
	/** 活动编码*/
	private String activity;
	/**redis key**/
	private String redisKey;
	/** 1-抽球模型，2-扔骰子模型*/
	private Integer modelType;
	/** 免费抽奖中奖积分*/
	private Integer freeHitPoint;		
	/** 免费抽奖机会数*/
	private Integer freeLotteryCount;
	/** 总抽奖限制数*/
	private Integer lotteryLimitTimes;
	/** 抽奖消耗积分*/
	private Integer pointLotteryUse;
	/** 基础骰子*/
	private List<TurntableeDice> baseDices;	
	/** 客户骰子*/
	private List<TurntableeDice> customerDices;
	/** 空抽奖次数*/
	private Integer emptyLotteryCount;
	/** 空奖品数**/
	private Integer emptyPrizeCount;
	/** 奖品唯一类型, 1-活动,2-奖品**/
	private Integer uniqueType;		
}