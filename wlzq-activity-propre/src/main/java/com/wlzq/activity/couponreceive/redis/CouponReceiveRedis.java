package com.wlzq.activity.couponreceive.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class CouponReceiveRedis  extends RedisFacadeAbstract {
	/**客户领取状态，key:customerid,value:0/1*/
	public static final CouponReceiveRedis CUSTOMER_RECIEVE_STATUS = new CouponReceiveRedis("activity:couponrecieve:status:",14*24*3600);
	
	/**优惠券已领取数量 key:id,value:hasrecievecount*/
	public static final CouponReceiveRedis RECIEVE_COUNT = new CouponReceiveRedis("activity:couponrecieve:count:",14*24*3600);

	/**优惠券信息 key:id,value:hasrecievecount*/
	public static final CouponReceiveRedis COUPON_INFO = new CouponReceiveRedis("activity:couponrecieve:coupon:",14*24*3600);

	private String redisPrefix;
	private int timeoutSeconds;

	private CouponReceiveRedis(String redisPrefix) {
		this.redisPrefix = redisPrefix;
	}

	private CouponReceiveRedis(String redisPrefix,int timeoutSeconds) {
		this.redisPrefix = redisPrefix;
		this.timeoutSeconds = timeoutSeconds;
	}
	
	@Override
	protected String getRedisPrefix() {
		return redisPrefix;
	}

	@Override
	protected int getTimeoutSeconds() {
		return timeoutSeconds;
	}

}