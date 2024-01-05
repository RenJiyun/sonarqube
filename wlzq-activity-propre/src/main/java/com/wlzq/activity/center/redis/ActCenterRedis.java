package com.wlzq.activity.center.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

/**
 * @author: qiaofeng
 * @date: 2022/3/21 10:14
 * @description: 活动中心缓存
 */
public class ActCenterRedis extends RedisFacadeAbstract {
    /*不同客户端展示的活动列表不同
    key: cientType，value：List<ActCenter>*/
    public static final ActCenterRedis ACT_CENTER_INFO = new ActCenterRedis("activity:center:actlist:", 30 * 24 * 3600);

    private String redisPrefix;
    private int timeoutSeconds;

    public ActCenterRedis(String redisPrefix, int timeoutSeconds) {
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
