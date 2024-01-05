package com.wlzq.activity;

import com.wlzq.core.exception.BizException;

/**
 * @author wlzq
 */
public class ActivityBizException extends BizException{

	private static final long serialVersionUID = 1435432332L;

	/** 活动不存在 */
	public static final ActivityBizException ACTIVITY_NOT_EXIST = new ActivityBizException(3000000,"活动不存在");
	public static final ActivityBizException ACTIVITY_NOT_EXIST_MESSAGE = new ActivityBizException(3000000,"{0}");
	/** 签到活动：当天已签到 */
	public static final ActivityBizException ALREADY_CHECK_IN = new ActivityBizException(3000001,"今日已签到");
	/** 签到活动：补签卡不足 */
	public static final ActivityBizException CHECK_IN_NO_OPPORTUNITY = new ActivityBizException(3000002,"补签卡不足");
	/** 签到活动：补签卡数已领完 */
	public static final ActivityBizException CHECK_IN_OPPORTUNITY_ALREADY_RECIEVE = new ActivityBizException(3000003,"补签卡数已领完 ");
	/** 答题活动：奖励金额已超上限 */
	public static final ActivityBizException ANSWER_BONUS_AMOUNT_OVER = new ActivityBizException(3000004,"奖励金额已超上限 ");
	/** 答题活动：答题超时 */
	public static final ActivityBizException ANSWER_TIME_OUT = new ActivityBizException(3000005,"答题超时 ");
	/** 答题活动：邀请自己 */
	public static final ActivityBizException ANSWER_INVITE_SELF = new ActivityBizException(3000006,"怎么可以输入你自己的邀请码，你好坏坏哦 ");
	/** 答题活动：邀请对像非首次答题 */
	public static final ActivityBizException ANSWER_INVITE_NOT_FIRST = new ActivityBizException(3000007,"您非首次参与答题，未能获取瓜分卡 ");
	/** 答题活动：每日最多可使用3张瓜分卡 */
	public static final ActivityBizException ANSWER_CARD_USE_COUNT = new ActivityBizException(3000008,"每日最多可使用3张瓜分卡");
	/** 答题活动：提现需要验证账户 */
	public static final ActivityBizException ANSWER_WITHDRAW_NEED_ACCOUNT = new ActivityBizException(3000009,"微信验证资金账号即可提现");
	/** 答题活动：邀请不存在 */
	public static final ActivityBizException ANSWER_INVITE_NOT_EXIST = new ActivityBizException(3000010,"");
	/** 答题活动：有审核不通过的提现 */
	public static final ActivityBizException ANSWER_HAS_CHECK_NOT_PASS = new ActivityBizException(3000011,"有审核不通过的提现");
	/** 未登录 */
	public static final ActivityBizException ACTIVITY_NOT_LOGIN = new ActivityBizException(30000012, "未登录");
	/** 数据库异常 */
	public static final ActivityBizException ACTIVITY_DATABASE_ERROR = new ActivityBizException(30000013, "数据库异常");
	/** 参数不能为空 */
	public static final ActivityBizException ACTIVITY_PARAMS_NOTNULL = new ActivityBizException(30000014, "参数不能为空");
	/** 参数异常 */
	public static final ActivityBizException ACTIVITY_PARAMS_EXCEPTION = new ActivityBizException(30000014, "参数异常:{0}");
	/** 参数格式不正确 */
	public static final ActivityBizException ACTIVITY_PARAMS_FORMAT_ERROR = new ActivityBizException(30000015, "参数格式不正确");
	/** 存在敏感词 */
	public static final ActivityBizException ACTIVITY_SENSITIVE_WORDS = new ActivityBizException(30000016, "存在敏感词");
	/** 没有统计数据 */
	public static final ActivityBizException ACTIVITY_NO_STATISTICS = new ActivityBizException(30000017, "没有统计数据");
	/** 未关注公众号 */
	public static final ActivityBizException ACTIVITY_NOT_FOLLOW = new ActivityBizException(30000018, "未关注公众号");
	/** 查无该微信用户 */
	public static final ActivityBizException ACTIVITY_WECHATUSER_NOTFOUND = new ActivityBizException(30000019, "查无该微信用户");
	/** 查无相关数据 */
	public static final ActivityBizException ACTIVITY_DATABASE_DATANOTFOUND = new ActivityBizException(30000020, "查无相关数据");

	public static final ActivityBizException ACTIVITY_NOT_OPEN = new ActivityBizException(30000021, "不在活动时间");
	/** 参数不能为空 */
	public static final ActivityBizException INTERNSHIP_PARAMS_NOTNULL = new ActivityBizException(30010001, "参数不能为空");
	/** 参数格式不正确 */
	public static final ActivityBizException INTERNSHIP_PARAMS_FORMAT_ERROR = new ActivityBizException(30010002, "参数格式不正确");
	/** 未登录 */
	public static final ActivityBizException INTERNSHIP_NOT_LOGIN = new ActivityBizException(30010003, "未登录");
	/** 查无该微信用户 */
	public static final ActivityBizException INTERNSHIP_WECHATUSER_NOTFOUND = new ActivityBizException(30010004, "查无该微信用户");
	/** 重复报名 */
	public static final ActivityBizException INTERNSHIP_SIGNUP_DUPLICATE = new ActivityBizException(30010005, "重复报名");
	/** 没有报名 */
	public static final ActivityBizException INTERNSHIP_SIGNUP_NOTFOUND = new ActivityBizException(30010006, "没找到报名信息");
	/** 没有统计数据 */
	public static final ActivityBizException INTERNSHIP_NO_STATISTICS = new ActivityBizException(30010007, "没有统计数据");
	/** 数据库异常 */
	public static final ActivityBizException INTERNSHIP_DATABASE_ERROR = new ActivityBizException(30010008, "数据库异常");
	/** 存在敏感词 */
	public static final ActivityBizException INTERNSHIP_SENSITIVE_WORDS = new ActivityBizException(30010009, "存在敏感词");
	/** app新用户不存在 */
	public static final ActivityBizException RECIEVE_APP_NEW_USER_NOT_EXIST = new ActivityBizException(30020001, "app新用户不存在");
	/** app新用户不在活动时间注册 */
	public static final ActivityBizException RECIEVE_APP_NOT_NEW_USER = new ActivityBizException(30020002, "不为app新用户");
	/** 手机号码已领取过Level2 */
	public static final ActivityBizException RECIEVE_ALREADY_RECIEVE = new ActivityBizException(30020003, "{0}手机号码已领取过");
	/** 手机号码已领取过Level2 */
	public static final ActivityBizException RECIEVE_OVER_TIME = new ActivityBizException(30020004, "{0}手机号码超过领取有效期");

	/** 猜涨跌最小押注积分 */
	public static final ActivityBizException GUESS_MIN_POINT = new ActivityBizException(30030001, "最小押注{0}积分");
	/** 猜涨跌最高押注积分 */
	public static final ActivityBizException GUESS_MAX_POINT = new ActivityBizException(30030002, "最高押注{0}积分");
	/** 猜涨跌积分余额不足 */
	public static final ActivityBizException GUESS_POINT_INSUFFICIENT = new ActivityBizException(30030003, "积分余额不足");
	/** 猜涨跌当日重复押注 */
	public static final ActivityBizException GUESS_TRADE_DAY_REPEAT = new ActivityBizException(30030004, "重复押注");
	/** 大转盘限抽奖次数 */
	public static final ActivityBizException TURNTABLE_REACH_LOTTERY_TIMES = new ActivityBizException(30040001, "每天限抽奖{0}次");
	/** 发送短信失败 */
	public static final ActivityBizException SEND_SMS_MESSAGE_FAILED = new ActivityBizException(30040002,"发送短信失败");
	/** 修改状态失败 */
	public static final ActivityBizException CHANGE_STATUS_FAILED = new ActivityBizException(30040003,"修改状态失败");
	/** 金交会大转盘已抽过奖 */
	public static final ActivityBizException EXPO_TURNTABLE_HAS_LOTTERY = new ActivityBizException(30050001, "已抽过奖");
	/** 金交会大转盘未留言 */
	public static final ActivityBizException EXPO_TURNTABLE_NOT_LEFT_MESSAGE = new ActivityBizException(30050002, "未留言");

	/** 作品投票点赞数超当日上限 */
	public static final ActivityBizException VOTE_WORKS_LIKE_OVER_LIMIT = new ActivityBizException(30060001, "已达今日点赞数上限");

	/** 未关注公众号 */
	public static final ActivityBizException VOTE_WORKS_LIKE_NOT_SUB = new ActivityBizException(30060002, "未关注公众号");
	/** 点赞太快 */
	public static final ActivityBizException VOTE_WORKS_LIKE_FAST = new ActivityBizException(30060003, "您点赞太快");

	/** 双十二奖品，今日已领完 */
	public static final ActivityBizException DOUBLE_RECIEVE_COMPLETE = new ActivityBizException(30070001, "今日已领完");
	/** 双十二活动，已领取过该奖品 */
	public static final ActivityBizException DOUBLE_RECIEVE_ALREADY = new ActivityBizException(30070002, "已领取过该券");
	/** 双十二活动，限领一次该券 */
	public static final ActivityBizException DOUBLE_LOTTERY_HIT_ONCE = new ActivityBizException(30070003, "限领一次该券");

	/** 双旦,未关注公众号 */
	public static final ActivityBizException XMAS_NOT_SUB = new ActivityBizException(30080001, "未关注公众号");
	/** 双旦,未验证资金账号 */
	public static final ActivityBizException XMAS_NOT_CERTIFICATION = new ActivityBizException(30080002, "未验证资金账号");
	/** 双旦,不是在活动时间验证资金账号 */
	public static final ActivityBizException XMAS_NOT_ACT_TIME_CERTIFICATION = new ActivityBizException(30080003, "非活动时间验证的资金账号");
	/** 双旦,限抽奖两次 */
	public static final ActivityBizException XMAS_LOTTERY_LIMIT_TWO = new ActivityBizException(30080004, "限抽奖两次");

	/** 新春,未验证资金账号 */
	public static final ActivityBizException NEWYEAR_NOT_CERTIFICATION = new ActivityBizException(30090001, "未验证资金账号");

	/** 818,日期参数为空 */
	public static final ActivityBizException FINSECTION_EMPTY_DATE = new ActivityBizException(30100001, "活动日期不合法");

	/** 818,活动阶段为空 */
	public static final ActivityBizException FINSECTION_EMPTY_STEP = new ActivityBizException(30100002, "当前时间不在活动阶段");

	/** 818,参数错误 */
	public static final ActivityBizException FINSECTION_PARAMS_ILLEG = new ActivityBizException(30100003, "参数非法");

	/** 818,队伍已组队成功 */
	public static final ActivityBizException FINSECTION_TEAM_SUCCES = new ActivityBizException(30100004, "队伍已组队成功");

	/** 818,队伍已失效 */
	public static final ActivityBizException FINSECTION_TEAM_FAIL = new ActivityBizException(30100005, "队伍已失效");

	public static final ActivityBizException FINSECTION_HAS_COUPON = new ActivityBizException(30100006, "已获得优惠券，无法重复参加活动");

	public static final ActivityBizException ACT_EMPTY_PRIZE = new ActivityBizException(30100007, "奖品已领完");

	public static final ActivityBizException FINSECTION_HAS_TEAM = new ActivityBizException(30100008, "已在队伍中");

	public static final ActivityBizException FINSECTION_CARD_HAS_LIGHT = new ActivityBizException(30100009, "金卡已点亮");

	public static final ActivityBizException FINSECTION_DOUBLE_LIGHT = new ActivityBizException(30100010, "不能重复点亮");

	public static final ActivityBizException FINSECTION_LIGHT_SELF = new ActivityBizException(30100011, "不能点亮自己的卡片");

	public static final ActivityBizException FINSECTION_ZERO_LOTTERY_COUNT = new ActivityBizException(30100012, "没有抽奖机会");

	public static final ActivityBizException FINSECTION_EMPTY_MOBILE = new ActivityBizException(30100013, "请输入手机号");

	public static final ActivityBizException ACT_ILL_OPENDATE = new ActivityBizException(30100014, "{0}");
	/**妇女节活动**/
	public static final ActivityBizException ACT_ONLY_WOMEN = new ActivityBizException(30100015, "本活动仅限女性客户参加");

	/**任务不存在**/
	public static final ActivityBizException ACT_TASK_NOT_EXIST = new ActivityBizException(30100016, "任务不存在");

	/**产品不存在**/
	public static final ActivityBizException ACT_PRODUCT_NOT_EXIST = new ActivityBizException(30100017, "产品不存在");

	/**小于起购金额**/
	public static final ActivityBizException ACT_ORDER_NOT_MIN_BUY = new ActivityBizException(30100018, "小于起购金额");

	/**可使用体验金不足**/
	public static final ActivityBizException ACT_ORDER_NOT_ENOUGH_GOLG = new ActivityBizException(30100019, "可使用体验金不足");

	/**小于最小提现金额**/
	public static final ActivityBizException ACT_NOT_MIN_WITHDRAW = new ActivityBizException(30100020, "小于最小提现金额");

	/**任务状态异常，禁止提现**/
	public static final ActivityBizException ACT_ILL_TASK = new ActivityBizException(30100021, "任务状态异常，禁止提现");

	/**超过余额，无法提现**/
	public static final ActivityBizException ACT_BEYONG_BALANCE = new ActivityBizException(30100022, "超过余额，无法提现");

	/**本活动仅限新客参加**/
	public static final ActivityBizException ACT_ONLY_NEWCUST = new ActivityBizException(30100023, "本活动仅限新客参加");

	/**今日投票机会已用完，请明天再来**/
	public static final ActivityBizException ACT_DAILY_VOTE_LIMIT = new ActivityBizException(30100024, "今日投票机会已用完，请明天再来");

	/**当前非投票时间**/
	public static final ActivityBizException ACT_NOT_VOTE_TIME = new ActivityBizException(30100025, "当前非投票时间");

	/**当前非投票时间**/
	public static final ActivityBizException ACT_TEAM_NOT_FOUND = new ActivityBizException(30100026, "查询不到队伍，请确认队伍编号或队长名");

	/**大转盘配置异常**/
	public static final ActivityBizException ACT_TURNTABLE_CONFIG_ERROR = new ActivityBizException(30100027, "大转盘配置异常");

	/**奖品不存在**/
	public static final ActivityBizException ACT_PRIZE_NOT_EXIST = new ActivityBizException(30100028, "奖品不存在");

	/**奖品已发出**/
	public static final ActivityBizException ACT_PRIZE_SENDED = new ActivityBizException(30100029, "奖品已发出");

	/**超出每人每日购买上限**/
	public static final ActivityBizException ACT_BEYOND_ORDER_MAX_EVERY_ONE = new ActivityBizException(30100030, "超出每人每日购买上限:{0}");
	/**超出产品每天购买额度**/
	public static final ActivityBizException ACT_BEYOND_ORDER_MAX_EVERY_DAY = new ActivityBizException(30100031, "超出产品每天购买额度");

	/**ETF课程及投顾产品联合推广活动 仅限购买课程的用户领取**/
	public static final ActivityBizException ACT_ETF_ONLY_PAID = new ActivityBizException(30100032, "本活动仅限购买课程的用户领取");

	/**不符合活动条件**/
	public static final ActivityBizException ACT_CUS_NOT_VALID = new ActivityBizException(30110001, "很抱歉，您不符合活动条件");

	/**入金金额不达标**/
	public static final ActivityBizException ACT_RJMONEY_NOT_VALID = new ActivityBizException(30110002, "很抱歉，您的入金金额未达标");

	/**已订阅**/
	public static final ActivityBizException ACT_SUBSCRIBE_OK = new ActivityBizException(30110003, "已订阅");

	/**已入金登记**/
	public static final ActivityBizException ACT_RJMONEY_ADD_OK = new ActivityBizException(30110004, "已入金登记");

	/**已领取福袋**/
	public static final ActivityBizException ACT_FD_RECIEVE_OK = new ActivityBizException(30110005, "已领取福袋");

	/**已领取直播奖品**/
	public static final ActivityBizException ACT_ZB_RECIEVE_OK = new ActivityBizException(30110006, "已领取直播奖品");

	/**未查询到入金信息**/
	public static final ActivityBizException ACT_CUS_NOT_FOUND = new ActivityBizException(30110007, "未查询到入金信息");

	/**无可用奖品**/
	public static final ActivityBizException ACT_PRIZE_NOT_VALID = new ActivityBizException(30110008, "无可用奖品");

	/**获得一张祝福卡**/
	public static final ActivityBizException ACT_ZB_GET_ZFK = new ActivityBizException(30110009, "获得一张祝福卡");

	/**活动火爆，请稍后再试**/
	public static final ActivityBizException ACT_EXCEPTION = new ActivityBizException(30110010, "活动火爆，请稍后再试");

	public static final ActivityBizException ACT_ACCOUNT_EXCEP = new ActivityBizException(30110011, "您参与活动的数据异常，请联系客服95322咨询。");

	/**已拆过礼盒**/
	public static final ActivityBizException ACT_GIFTBOX_RECIEVE_OK = new ActivityBizException(30110012, "已拆过礼盒");
	/**用户未绑定手机号**/
	public static final ActivityBizException ACT_GIFTBOX_NO_MOBILE = new ActivityBizException(30110013, "用户未绑定手机号");
	/**投票机会已用完*/
	public static final ActivityBizException ACT_STAFF_VOTE_LIMIT = new ActivityBizException(30111001, "投票机会已用完");

	public static final ActivityBizException ACT_CLICK_FAST = new ActivityBizException(3012001, "点击太快了");
	public static final ActivityBizException ACT_PRIZE_POINT_BOUND_MOBILE = new ActivityBizException(3012002, "客户号已与手机号{0}关联参与活动");
	public static final ActivityBizException ACT_PRIZE_POINT_NOTCONFIG = new ActivityBizException(3012003, "奖品积分字典未配置");
	public static final ActivityBizException ACT_PRIZE_POINT_INSUFFICIENT = new ActivityBizException(3012004, "积分不足");
	public static final ActivityBizException ACT_PRIZE_POINT_BOUND_CUSTOMERID = new ActivityBizException(3012005, "手机号已与客户号{0}关联参与活动");
	public static final ActivityBizException ACT_PRIZE_USED_DTCP028_14DAYS = new ActivityBizException(3012006, "已经体验过金股雷达14天规格");
	/**奖品类型不存在**/
	public static final ActivityBizException ACT_PRIZE_TYPE_NOT_EXIST = new ActivityBizException(3012007, "奖品类型不存在");


	/** 您已超出参与活动次数限制 */
	public static final ActivityBizException ACT_TASK_TIMES_LIMIT = new ActivityBizException(3012008, "您已超出参与活动次数限制");
	/** 没有完成该任务权限 */
	public static final ActivityBizException ACT_TASK_NO_AUTH = new ActivityBizException(3012009, "没有完成该任务权限");
	/** 已用于完成历史任务 */
	public static final ActivityBizException ACT_TASK_BIZ_CODE_HAS_USED = new ActivityBizException(3012010, "已用于完成历史任务");
	/** 今天已完成任务 */
	public static final ActivityBizException ACT_TASK_TODAY_HAS_DONE = new ActivityBizException(3012011, "今天已完成任务");
	/** 已完成任务 */
	public static final ActivityBizException ACT_TASK_HAS_DONE = new ActivityBizException(3012012, "已完成任务");
	public static final ActivityBizException ACT_TASK_HAS_NOT_DONE = new ActivityBizException(3012013, "任务未完成");
	public static final ActivityBizException ACT_PRIZE_POINT_NOT_SETTING = new ActivityBizException(3012014, "奖品积分未配置");
	public static final ActivityBizException ACT_TASK_NOT_SETTING = new ActivityBizException(3012015, "活动任务未配置");
	public static final ActivityBizException ACT_TASK_NOT_DONE = new ActivityBizException(3012016, "任务未完成:{0}");

	public static final ActivityBizException ACT_ORDER_EXIST = new ActivityBizException(3012017, "14规格投顾订单已存在");
	public static final ActivityBizException ACT_TASK_TODAY_ALL_HAS_DONE = new ActivityBizException(3012017, "今日活动任务已全部完成");
	public static final ActivityBizException ACT_TASK_ALL_HAS_DONE = new ActivityBizException(3012018, "活动任务已全部完成");
	public static final ActivityBizException PRIZE_PER_USER_LIMIT = new ActivityBizException(3012019, "奖品已超过每人领取上限");

	public static final ActivityBizException NOT_HAVE_GG_PERM = new ActivityBizException(3012020, "未开通港股通权限");
	public static final ActivityBizException STAFFNO_NOT_EXIT = new ActivityBizException(3012021,"员工账号不存在");


	/** 客户号已经领取过level2 */
	public static final ActivityBizException CUSTOMER_HAVE_RECEIVED = new ActivityBizException(3012022, "您的客户号已与手机号{0}绑定领取过此福利了哦~");
	/** 开通相关权限或业务后, T+1 才可以领取 */
	public static final ActivityBizException L2_RECEIVE_T1 = new ActivityBizException(3012023, "{0}");
	public static final ActivityBizException L2_RECEIVE_NOT_ACTIVITY_TIME = new ActivityBizException(3012024, "{0}");
	public static final ActivityBizException L2_RECEIVE_RJ_CONDITION = new ActivityBizException(3012025, "入金未达标");

	public ActivityBizException(int code, String msg) {
		super(code, msg);
	}



}
