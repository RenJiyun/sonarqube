package com.wlzq.activity.double11.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class Double11Redis extends RedisFacadeAbstract {
    /** e万通个人奖励冲刺奖列表 */
    public static final Double11Redis PERSONAL_SPRINT_AWARD_LIST = new Double11Redis("activity:double11:personalSprintAward:",3600 * 24 * 30);
    /** e万通个人奖励爆发奖列表 */
    public static final Double11Redis PERSONAL_OUTBREAK_AWARD_LIST = new Double11Redis("activity:double11:personalOutbreakAward:",3600 * 24 * 30);
    /** e万通个人奖励更新时间 */
    public static final Double11Redis PERSONAL_AWARD_UPDATE_TIME = new Double11Redis("activity:double11:personalAwardUpdateTime:",3600 * 24 * 30);
    /** 同花顺投顾团队冲刺奖列表 */
    public static final Double11Redis THS_ADVISER_TEAM_SPRINT_AWARD_LIST = new Double11Redis("activity:double11:thsAdviserTeamSprintAward:",3600 * 24 * 30);
    /** 同花顺投顾团队爆发奖列表 */
    public static final Double11Redis THS_ADVISER_TEAM_OUTBREAK_AWARD_LIST = new Double11Redis("activity:double11:thsAdviserTeamOutbreakAward:",3600 * 24 * 30);
    /** 同花顺投顾团队奖励更新时间 */
    public static final Double11Redis THS_ADVISER_TEAM_AWARD_UPDATE_TIME = new Double11Redis("activity:double11:thsAdviserTeamAwardUpdateTime:",3600 * 24 * 30);
    /** 决策销售推动奖列表 */
    public static final Double11Redis DECISION_SALE_PROMOTION_AWARD_LIST  = new Double11Redis("activity:double11:decisionSalePromotionAward:",3600 * 24 * 30);
    /** 决策销售传播奖列表 */
    public static final Double11Redis DECISION_SALE_COMMUNICATION_AWARD_LIST  = new Double11Redis("activity:double11:decisionSaleCommunicationAward:",3600 * 24 * 30);
    /** 决策销售奖励更新时间 */
    public static final Double11Redis DECISION_SALE_AWARD_UPDATE_TIME = new Double11Redis("activity:double11:decisionSaleAwardUpdateTime:",3600 * 24 * 30);

    private String redisPrefix;
    private int timeoutSeconds;

    public Double11Redis(String redisPrefix, int timeoutSeconds) {
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
