package com.wlzq.activity.utils;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

public class ActivityRedis  extends RedisFacadeAbstract {

	/**活动信息,key：编码,value:活动信息*/
	public static final ActivityRedis ACT_ACTIVITY_INFO = new ActivityRedis("activity:activity:info:",30*24*3600);
	
	/**活动用户信息 */
	public static final ActivityRedis USER_INFO = new ActivityRedis("activity:user:info");
	/** 天添利活动员工分享码信息 */
	public static final ActivityRedis TTL_SHARE_CODE_INFO = new ActivityRedis("activity:tiantianli:staffinfo:");
	
	/**吐槽抽奖奖品 */
	public static final ActivityRedis APP_COMPLAIN_PRIZES = new ActivityRedis("activity:appcomplain:prizes:");
	/**签到活动情况,key:userid,value:om.wlzq.activity.dto.checkin.CheckInDto*/
	public static final ActivityRedis ACT_CHECKIN_STATUS = new ActivityRedis("activity:checkin:status:");
	/**答题有奖活动题目列表,key:code(连续答题编号),value:com.wlzq.activity.dto.answer.QuestionsMemDto*/
	public static final ActivityRedis ACT_ANSWER_QUESTIONS = new ActivityRedis("activity:answer:questions:",102);
	/**答题有奖活动题目,key:答题编号+答题序号,value:答案编号*/
	public static final ActivityRedis ACT_ANSWER_ANSWER = new ActivityRedis("activity:answer:answer:",17);
	
	/**默认答题有奖活动奖品,key:,value:*/
	public static final ActivityRedis ACT_ANSWER_PRIZES = new ActivityRedis("activity:answer:prizes:",200*24*3600);
	
	/**818答题有奖活动奖品,key:级别范围,value:级别列表，prizeLevel+"_"+sequence*/
	public static final ActivityRedis ACT_ANSWER_PRIZES_818 = new ActivityRedis("activity:answer:prizes818:",200*24*3600);
	
	/**大转盘活动抽奖奖品,key：userpoint(消耗积分)或free(免费),value:奖品ID*/
	public static final ActivityRedis ACT_TURNTABLE_PRIZES = new ActivityRedis("activity:turntable:prizes:",200*24*3600);
	/**大转盘活动免费抽奖计数,key：userid+date,value:抽奖数*/
	public static final ActivityRedis ACT_TURNTABLE_LOTTERY_TIMES = new ActivityRedis("activity:turntable:lotterytimes:",24*3600);
	/**大转盘活动免费抽奖计数,key：userid+date,value:分享数*/
	public static final ActivityRedis ACT_TURNTABLE_SHARE_TIMES = new ActivityRedis("activity:turntable:sharetimes:",24*3600);
	/**大转盘活动抽奖时间,key：userid,value:时间戳*/
	public static final ActivityRedis ACT_TURNTABLE_LOTTERY_TIMESTAMP = new ActivityRedis("activity:turntable:timestamp:",10*3600);
	
	/**金交会大转盘活动抽奖记录,key：openid,value:1*/
	public static final ActivityRedis ACT_EXPO_TURNTABLE_LOTTERY = new ActivityRedis("activity:expoturntable:lottery:",6*3600);
	
	/**金交会大转盘活动抽奖奖品,key：all,value:奖品ID*/
	public static final ActivityRedis ACT_EXPO_TURNTABLE_PRIZES = new ActivityRedis("activity:expoturntable:prizes:",6*3600);
	
	/**作品投票热度增加时间,key：openid,value:timestamp*/
	public static final ActivityRedis ACT_WORKS_VOTE_HOT_TIME = new ActivityRedis("activity:worksvote:dohottime:",10);

	/**作品投票点赞时间,key：openid,value:timestamp*/
	public static final ActivityRedis ACT_WORKS_VOTE_LIKE_TIME = new ActivityRedis("activity:worksvote:doliketime:",10);

	/**作品投票点赞次数,key：openid+日期,value:次数*/
	public static final ActivityRedis ACT_WORKS_VOTE_LIKE_COUNT = new ActivityRedis("activity:worksvote:likecount:",24*3600);
	
	/**投票活动抽奖奖品,key：all,value:是否中奖前缀(0:否，1：是)+"-"+奖品ID*/
	public static final ActivityRedis ACT_WORKS_VOTE_PRIZES = new ActivityRedis("activity:worksvote:prizes:",7*24*3600);
	/**投票活动是否弹窗状态,key：openid+date,value: 0:否，1：是*/
	public static final ActivityRedis ACT_WORKS_VOTE_POP = new ActivityRedis("activity:worksvote:pop:",24*3600);

	/**双十二活动抽奖次数,key：userid+date,value: 次数*/
	public static final ActivityRedis ACT_DOUBLETWELVE_LOTTER_COUNT = new ActivityRedis("activity:doubletwelve:lotcount:",24*3600);
	/**双十二活动抽奖奖品,key：prizes,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_DOUBLETWELVE_LOTTER_PRIZES = new ActivityRedis("activity:doubletwelve:lotprizes:",15*24*3600);
	/**双十二活动抽奖谢谢参与奖品数,key：prizes,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_DOUBLETWELVE_LOTTER_EMPTYPRIZES = new ActivityRedis("activity:doubletwelve:lotemptyprizes:",15*24*3600);
	/**双十二活动抽奖领奖编号,key：领奖编号,value: 抽奖记录id*/
	public static final ActivityRedis ACT_DOUBLETWELVE_PRIZE_RECIEVE_CODE = new ActivityRedis("activity:doubletwelve:prizerecievecode:",24*3600);
	/**双十二活动领奖数量,key：类型，1：优惠券，2：兑换码,value: 奖品id*/
	public static final ActivityRedis ACT_DOUBLETWELVE_PRIZE_RECIEVE_PRIZES = new ActivityRedis("activity:doubletwelve:prizesrecieve:",10*24*3600);
	/**双十二活动每天领奖数量,key：date+type（类型，1：优惠券，2：兑换码）,value: 已领取奖品数*/
	public static final ActivityRedis ACT_DOUBLETWELVE_PRIZE_RECIEVE_COUNT = new ActivityRedis("activity:doubletwelve:prizesrecieved:",24*3600);
	

	/**双旦活动抽奖次数,key：customerid,value: 次数*/
	public static final ActivityRedis ACT_XMAS_LOTTER_COUNT = new ActivityRedis("activity:xmas:lotcount:",15*24*3600);
	/**双旦活动	优惠券类抽奖奖品,key：prizeTypeCode,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_XMAS_COUPON_LOTTER_PRIZES = new ActivityRedis("activity:xmas:couponlotprizes:",30*24*3600);
	/**双旦活动	优惠券类奖品每天发出数量,key：date,value: int*/
	public static final ActivityRedis ACT_XMAS_COUPON_SEND_COUNT = new ActivityRedis("activity:xmas:couponsendcount:",30*24*3600);
	/**双旦活动非优惠券类抽奖奖品,key：prizes,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_XMAS_OTHER_LOTTER_PRIZES = new ActivityRedis("activity:xmas:otherlotprizes:",30*24*3600);
	/**双旦活动抽奖谢谢参与奖品数,key：prizes,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_XMAS_LOTTER_EMPTYPRIZES = new ActivityRedis("activity:xmas:otherlotemptyprizes:",30*24*3600);
	/**双旦砸的金蛋编号,key：userId,value: List<Integer>*/
	public static final ActivityRedis ACT_XMAS_LOTTER_EGGS_NOS = new ActivityRedis("activity:xmas:lotemptyeggenos:",30*24*3600);
	
	/**新春活动优先奖池奖品,key：prizeTypeCode,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_NEWYEAR_PRIORITY_LOTTER_PRIZES = new ActivityRedis("activity:newyear:priorityprizes:",30*24*3600);
	/**新春活动	优惠券类奖品每天发出数量,key：prizetypecode+date,value: int*/
	public static final ActivityRedis ACT_NEWYEAR_COUPON_SEND_COUNT = new ActivityRedis("activity:newyear:prioritysendcount:",30*24*3600);
	/**新春活动概率中奖类抽奖奖品,key：prizes,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_NEWYEAR_PROBABILITY_LOTTER_PRIZES = new ActivityRedis("activity:newyear:probabilityprizes:",30*24*3600);
	/**新春活动抽奖谢谢参与奖品数,key：prizes,value: 是否中奖+奖品id*/
	public static final ActivityRedis ACT_NEWYEAR_PROBABILITY_LOTTER_EMPTYPRIZES = new ActivityRedis("activity:newyear:probabilityemptyprizes:",30*24*3600);
	/**新春活动抽奖时间,key：userid,value:时间戳*/
	public static final ActivityRedis ACT_NEWYEAR_LOTTERY_TIMESTAMP = new ActivityRedis("activity:turntable:timestamp:",10*3600);

	/**2020春节活动抽奖奖品,key：all,value:是否中奖前缀(0:否，1：是)+"-"+奖品ID*/
	public static final ActivityRedis ACT_SPRINGFESTIVAL_2020_PRIZES = new ActivityRedis("activity:springfestival:prizes:",9*24*3600);
	/**51大转盘活动抽奖奖品,key：userpoint(消耗积分)或free(免费),value:奖品ID*/
	public static final ActivityRedis ACT_TURNTABLE51_PRIZES = new ActivityRedis("activity:turntable51:prizes:",7*24*3600);
	/**51大转盘活动免费抽奖计数,key：userid+date,value:抽奖数*/
	public static final ActivityRedis ACT_TURNTABLE51_LOTTERY_TIMES = new ActivityRedis("activity:turntable51:lotterytimes:",24*3600);
	/**51大转盘活动抽奖时间,key：userid,value:时间戳*/
	public static final ActivityRedis ACT_TURNTABLE51_LOTTERY_TIMESTAMP = new ActivityRedis("activity:turntable51:timestamp:",10*3600);
	/**2021元旦砸金蛋活动*/
	public static final ActivityRedis ACT_NEWYEARSDAY_2021_PRIZES = new ActivityRedis("activity:newyearsday:prizes:",48*3600);
	
	/**活动奖品, key:activtyCode:prizeCode, value:奖品ID*/
	public static final ActivityRedis ACT_ACTVITY_PRIZE = new ActivityRedis("activity:prizes:",200*24*3600);

	/*奖品每日限量领取, 计时器*/
	public static final ActivityRedis ACT_PRIZES_TIMER = new ActivityRedis("activity:prizes:timer:",24*3600);
	
	private String redisPrefix;
	private int timeoutSeconds;

	private ActivityRedis(String redisPrefix) {
		this.redisPrefix = redisPrefix;
	}
	
	private ActivityRedis(String redisPrefix,int timeoutSeconds) {
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