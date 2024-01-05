package com.wlzq.activity.brokeragemeeting.utils;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class BrokeargemeetingRedis extends RedisFacadeAbstract {

	/**问卷 */
	public static final BrokeargemeetingRedis ACT_WECHATUSER = new BrokeargemeetingRedis("activity:sign:wechatuser:", 2*24*3600);
	
	private String redisPrefix;
	private int timeoutSeconds;

	private BrokeargemeetingRedis(String redisPrefix) {
		this.redisPrefix = redisPrefix;
	}

	private BrokeargemeetingRedis(String redisPrefix, int timeoutSeconds) {
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
