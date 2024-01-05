package com.wlzq.activity.base.dto;

import java.util.List;

import lombok.Data;

/**
 * 优惠券领取活动dto
 * @author 
 * @version 1.0
 */
@Data
public class CouponRecieveActivityDto {	
	private String activityCode;		// 活动编码
	private List<String> prizeTypes;		// 奖品类型
}

