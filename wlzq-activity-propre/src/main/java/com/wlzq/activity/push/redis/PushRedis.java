package com.wlzq.activity.push.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

/**
 * @author: qiaofeng
 * @date: 2022/5/16 13:32
 * @description:
 */
public class PushRedis extends RedisFacadeAbstract {
    /*已发放优惠券，并且已经发送短信通知的用户信息*/
    public static final PushRedis RECEIVED_NOTICED = new PushRedis("activity:push:received:", 24 * 3600);
    public static final PushRedis NOTICED = new PushRedis("activity:push:noticed:", 24 * 3600);

    private String redisPrefix;
    private int timeoutSeconds;

    public PushRedis(String redisPrefix, int timeoutSeconds) {
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
