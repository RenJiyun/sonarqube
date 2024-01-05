package com.wlzq.activity.task.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

/**
 * @author renjiyun
 */
public class BatchChkRedis extends RedisFacadeAbstract {
    /** 北交所权限开通记录上次数据处理时间, 格式为: yyyyMMdd */
    public static final BatchChkRedis ACT_2023DOUBLE11_BJS = new BatchChkRedis("activity:2023double11:bjs:", 200 * 24 * 3600);
    /** 非有效户入金流水记录上次数据处理时间, 格式为: yyyyMMdd */
    public static final BatchChkRedis ACT_2023DOUBLE11_RJ = new BatchChkRedis("activity:2023double11:rj:", 200 * 24 * 3600);

    private final String redisPrefix;
    private final int timeoutSeconds;

    public BatchChkRedis(String redisPrefix, int timeoutSeconds) {
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
