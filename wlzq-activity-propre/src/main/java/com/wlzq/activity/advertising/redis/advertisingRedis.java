package com.wlzq.activity.advertising.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class advertisingRedis extends RedisFacadeAbstract {
	/**活动信息,key：编码,value:活动信息*/
	public static final advertisingRedis ACT_ACTIVITY_INFO = new advertisingRedis("advertising:advertising:info:",30*24*3600);


	private String redisPrefix;
	private int timeoutSeconds;

	private advertisingRedis(String redisPrefix) {
		this.redisPrefix = redisPrefix;
	}

	private advertisingRedis(String redisPrefix, int timeoutSeconds) {
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