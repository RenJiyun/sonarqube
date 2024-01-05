package com.wlzq.activity.couponreceive.config;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class RecieveInfo {
	private Date timeStart;
	private Date timeEnd;
	List<RecieveCoupon> coupons;
}
