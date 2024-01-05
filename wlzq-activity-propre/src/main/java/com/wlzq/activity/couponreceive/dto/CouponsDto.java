package com.wlzq.activity.couponreceive.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 优惠券dto
 * @author 
 * @version 1.0
 */
@Data
public class CouponsDto {
	public static final Integer STATUS_NOT_BEGIN = 0;
	public static final Integer STATUS_CAN_RECIEVE = 1;
	public static final Integer STATUS_RECIEVE_COMPLETE = 2;
	public static final Integer STATUS_HAS_RECIEVED = 3;
	public static final Integer STATUS_EXPIRE = 4;
	public Integer canRecieve;
	private Date nextTime;
	private List<CouponDto> coupons;
	private List<CouponDto> nextCoupons;
}

