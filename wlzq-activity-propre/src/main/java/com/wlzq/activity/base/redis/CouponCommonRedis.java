package com.wlzq.activity.base.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

/**
 * @author renjiyun
 */
public class CouponCommonRedis extends RedisFacadeAbstract {

    /** 优惠券领取锁 */
    public static final CouponCommonRedis COUPON_RECEIVE_LOCK = new CouponCommonRedis("activity:coupon:receive:lock",60);

    /** 活动物品兑换锁 */
    public static final CouponCommonRedis COUPON_REDEEM_LOCK = new CouponCommonRedis("activity:coupon:redeem:lock",60);
    private String redisPrefix;
    private int timeoutSeconds;

    public CouponCommonRedis(String redisPrefix, int timeoutSeconds) {
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
