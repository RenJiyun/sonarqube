package com.wlzq.activity;

import cn.hutool.core.date.DatePattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ActivityConstant {

	//作品投票活动编码
	//public static final String VOTEWORKS_ACTIVITYCODE = new String("ACTIVITY.NATIONALDAY.2019.WORKSVOTE");
	//2021年优秀员工投票活动
	public static final String VOTEWORKS_ACTIVITYCODE = new String("ACTIVITY.STAFFVOTE.2021");
	//作品投票空奖品数，空奖品数 / (奖品数 + 空奖品数) = 中奖率
	public static final Integer VOTEWORKS_NOPRIZESCOUNT = new Integer(2000);
	
	public static final String VOTEWORKS_SOURCETEXT = new String("国庆70周年活动");
	//818理财节阶段活动提醒代码
	public static final String FINSECTION_HOTLINE_CALL = new String("ACT.818.2019.HOTLINE.CALL");
	//818理财节阶段活动提醒代码
	public static final String FINSECTION_HOTLINE_CALL_NAME = new String("818理财节VIP热线点击");
	//818理财节阶段活动提醒代码
	public static final String FINSECTION_STEP_REMIND = new String("ACT.818.2019.STEP.REMIND");
	//818理财节阶段活动提醒代码
	public static final String FINSECTION_STEP_REMIND_NAME = new String("818理财节活动开始提醒");
	//818理财节小集合预约
	public static final String FINSECTION_COLLECTION_APPOINTMENT = new String("ACTIVITY.818.2019.COLLECTION.APPOINTMENT");
	//818理财节小集合预约
	public static final String FINSECTION_COLLECTION_APPOINTMENT_NAME = new String("818理财节小集合产品咨询预约");
	//818理财节短信模板
	public static final String FINSECTION_SMS_TEMPLATE = new String("ACTIVITY.818.2019.SMS.TEMPLATE");
	//818活动编码前缀
	public static final String FINSECTION_CODE_PRE = new String("ACTIVITY.818.2019.");


	public static final String ACT_21818_PROMOTIONS = "ACTIVITY.2021818.PROMOTIONS";
	public static final String ACT_21818_COUPON_JD100 = "COUPON.INVEST.JD100.2021818PROM";

	/** 2021 818 理财 开通条件单任务*/
	public static final String TASK_ACT_21818_TJD = "TASK.ACT.818.2021.TJD";
	public static final String TASK_ACT_21818_LEVEL2_BUY = "TASK.ACT.818.2021.LEVEL2.BUY";
	public static final String TASK_ACT_21818_ARTICLE_LIKE = "TASK.ACT.818.2021.ARTICLE.LIKE";
	public static final String TASK_ACT_21818_LOOK_ARTICLE = "TASK_ACT_21818_LOOK_ARTICLE";//不存在的任务

	public static final BigDecimal EXP_GOLD_21818_ORDER_MAX_EVERY_ONE = new BigDecimal("20000");
	public static final BigDecimal EXP_GOLD_21818_ORDER_MAX_EVERY_DAY = new BigDecimal("5000000");

	public static final String EXP_GOLD_21818_ORDER_MAX_EVERY_DAY_CONFIG = "EXP_GOLD_21818_ORDER_MAX_EVERY_DAY";
	public static final String ACT_API_WITH_DRAW_FLAG = "act.api.with.draw.flag";

	/** 提现截止日 */
	public static final LocalDateTime ACT_21818_RED_PACK_END_DATE = LocalDateTime.parse("2021-08-31 23:59:59", DatePattern.NORM_DATETIME_FORMATTER);




}
