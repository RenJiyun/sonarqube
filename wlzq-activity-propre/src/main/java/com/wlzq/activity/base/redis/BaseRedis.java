package com.wlzq.activity.base.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class BaseRedis  extends RedisFacadeAbstract {
	/**活动信息,key：编码,value:活动信息*/
	public static final BaseRedis ACT_ACTIVITY_INFO = new BaseRedis("activity:activity:info:",30*24*3600);
	/**奖品类型信息,key：奖品编码,value:奖品类型信息*/
	public static final BaseRedis ACT_PRIZETYPE_INFO = new BaseRedis("activity:prizetype:info:",30*24*3600);

	/**是否有新资讯数据 */
	public static final BaseRedis INFO_DATA_NEW_STATUS = new BaseRedis("base:data:hasnew:",-1);

	/**e万通首页资讯信息  */
	public static final BaseRedis INFORMATION = new BaseRedis("info:information:information:",-1);

	/**e万通首页资讯信息  */
	public static final BaseRedis ACT_FINORDER_BUYSUCCESS_CUSID = new BaseRedis("financing:order:buysuccess:cusid:",-1);

	private String redisPrefix;
	private int timeoutSeconds;

	private BaseRedis(String redisPrefix) {
		this.redisPrefix = redisPrefix;
	}

	private BaseRedis(String redisPrefix,int timeoutSeconds) {
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
