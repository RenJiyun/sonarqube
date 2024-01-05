package com.wlzq.activity.finsection.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-07-19
 */
@Data
public class FinSectionScheduleDto {

	@JsonIgnore
	private String activityName;	//当前活动名称
	@JsonIgnore
	private String activityCode;	//当前活动代码
	@JsonIgnore
	private String prizeTypeCode;	//当前活动奖品代码
	@JsonIgnore
	private String description;		//当前活动描述
	private Integer step;			//活动阶段
	private List<Integer> subSteps = Lists.newArrayList();		//活动子阶段
	private Map<String, Object> step1Coupon = Maps.newHashMap();
	private Map<String, Object> step2Coupon = Maps.newHashMap();
	private Map<String, Object> step3Coupon = Maps.newHashMap();
	private Map<String, Object> step1Trade = Maps.newHashMap();
	private Map<String, Object> step2Trade = Maps.newHashMap();
	private Map<String, Object> step3Trade = Maps.newHashMap();
	private String nowPercent;		//现在百分比
	private Long countDown;			//倒计时
	private Integer leftPrizeCount;	//剩余奖品数
	
}
