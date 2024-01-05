package com.wlzq.activity.card.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class CardRedemptionRedis extends RedisFacadeAbstract {

    /** 用于并发控制, 维度为客户号 */
    public static final CardRedemptionRedis CARD_REDEMPTION_LOCK = new CardRedemptionRedis("activity:card:redemption:lock",60);

    private String redisPrefix;
    private int timeoutSeconds;

    public CardRedemptionRedis(String redisPrefix, int timeoutSeconds) {
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
