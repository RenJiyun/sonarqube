package com.wlzq.activity.l2recieve.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class L2RecieveRedis  extends RedisFacadeAbstract {
	/**Level领取状态,key：mobile,value:领取状态，示例： 1-0，2-0。4位数，代表4种类型，每位一种类型的领取状态，1位：新开户，2位：新增有效户，3位：新开信用账户，4位：邀请好友*/
	public static final L2RecieveRedis RECIEVE_STATUS = new L2RecieveRedis("activity:l2recieve:rstatus:",24*3600);
	
	/**邀请新用户Level领取状态,key：mobile（被邀请人手机号）,value:领取状态*/
	public static final L2RecieveRedis INVITE_RECIEVE_STATUS = new L2RecieveRedis("activity:l2recieve:irstatus:",24*3600);
	
	private String redisPrefix;
	private int timeoutSeconds;

	private L2RecieveRedis(String redisPrefix) {
		this.redisPrefix = redisPrefix;
	}

	private L2RecieveRedis(String redisPrefix,int timeoutSeconds) {
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