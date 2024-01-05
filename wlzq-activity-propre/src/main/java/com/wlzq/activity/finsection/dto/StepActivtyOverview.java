package com.wlzq.activity.finsection.dto;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-08-15
 */
@Data
public class StepActivtyOverview {
	
	public static final Integer OPEN = 1;
	public static final Integer CLOSE = 0;
	
	public static final Integer HAS_COUPON = 1;
	public static final Integer NOT_HAS_COUPON = 0;
	
	public static final Integer ENOUGH = 1;
	public static final Integer NOT_ENOUGH = 0;
	
	private Integer enoughCoupon = 0;	//是否有券
	private Integer isOpen;			//0-否
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date nextOpenTime;		//下场开始时间
	private Integer hasCoupon = 0;		//是否已有券
	private Long leftTime;			//剩余时间
	private Long countToOpen = 0l;		//开启时间倒计时
	private Long countToClose = 0l;		//关闭倒计时
}
