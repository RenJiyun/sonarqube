package com.wlzq.activity.lottery.service.app;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActLotteryBiz;
import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActLotteryDao;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActLottery;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.lottery.dto.*;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.lottery.biz.LotteryBiz;
import com.wlzq.activity.springfestival.dto.LotteryPreviewDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * 
 * @author louie
 * @version 2020-12-21
 */
@Service("activity.lottery")
public class LotteryService {

	@Autowired
	private LotteryBiz lotteryBiz;
	@Autowired
	private ActLotteryBiz actLotteryBiz;
	@Autowired
	private ActivityBaseBiz activityBaseBiz;
	@Autowired
	private ActLotteryDao actLotteryDao;
	@Autowired
	private ActPrizeTypeBiz actPrizeTypeBiz;
	@Autowired
	private ActPrizeDao actPrizeDao;
	@Autowired
	private ActGoodsFlowDao actGoodsFlowDao;


	/** 用于按序抽出卡牌 */
	private final static String[] CARD_LOTTERY_2023818 = {
		"CARD.2023818.03", "CARD.2023818.04", "CARD.2023818.06",
		"CARD.2023818.03", "CARD.2023818.05", "CARD.2023818.04",
		"CARD.2023818.02", "CARD.2023818.04", "CARD.2023818.01",
		"CARD.2023818.02", "CARD.2023818.03", "CARD.2023818.01",
		"CARD.2023818.02", "CARD.2023818.03", "CARD.2023818.01",
		"CARD.2023818.01", "CARD.2023818.02", "CARD.2023818.03",
		"CARD.2023818.02", "CARD.2023818.01", "CARD.2023818.05",
		"CARD.2023818.05", "CARD.2023818.02", "CARD.2023818.06",
		"CARD.2023818.01", "CARD.2023818.05", "CARD.2023818.02",
		"CARD.2023818.04", "CARD.2023818.05", "CARD.2023818.03",
		"CARD.2023818.05", "CARD.2023818.06", "CARD.2023818.01",
		"CARD.2023818.02", "CARD.2023818.06", "CARD.2023818.03"
	};

	/** 虚拟卡牌字典中的值 */
	private final static int ACT_PRIZE_TYPE_CARD = 16;

	@Signature(true)
//	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto lotteypreview (RequestParams params, AccTokenUser user,Customer customer) {
		String userId = user == null?null:user.getUserId();
		String openId = user == null?null:user.getOpenid();
		String customerId = customer == null ? null : customer.getCustomerId();
		StatusObjDto<LotteryPreviewDto> result = lotteryBiz.lotteryPreview(userId, openId, customerId);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
	
	@Signature(true)
//	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto lottery (RequestParams params, AccTokenUser user,Customer customer) {
		String userId = user == null?null:user.getUserId();
		String openId = user == null?null:user.getOpenid();
		String customerId = customer == null ? null : customer.getCustomerId();
		String mobile = user == null?null:user.getMobile();
		StatusObjDto<LotteryDto> result = lotteryBiz.lottery(userId, openId, customerId, mobile);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}



	/**
	 * 抽奖
	 *
	 * @param activityCode | 活动编码 |  | required
	 * @return com.wlzq.activity.lottery.dto.LotteryResDto
	 * @cate 12连刮视频月卡活动
	 */
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto dolottery (RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = params.getString("activityCode");
		String userId = user == null ? null : user.getUserId();
		String openId = user == null ? null : user.getOpenid();
		String customerId = customer == null ? null : customer.getCustomerId();
		String mobile = user == null ? null : user.getMobile();

		LotteryReqDto reqDto = new LotteryReqDto().setActivityCode(activityCode)
				.setUserId(userId).setOpenId(openId).setCustomerId(customerId).setMobile(mobile);
		StatusObjDto<LotteryResDto> result = actLotteryBiz.commonLottery(reqDto);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}


	/**
	 * 卡牌抽奖
	 *
	 * @param activityCode | 活动编码 |  | required
	 * @param remark 	 | 备注 (此次活动用于记录抽中卡牌的宫格位置, 意义由前端赋予) |  | non-required
	 * @return com.wlzq.activity.lottery.dto.LotteryResDto
	 * @cate 2023818理财活动
	 */
	@Signature(true)
	@MustLogin(true)
	public ResultDto cardlottery (RequestParams params, AccTokenUser user, Customer customer) {
		String activityCode = params.getString("activityCode");
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}

		Activity activity = activityBaseBiz.findActivity(activityCode);
		StatusDto actValidResult = activityBaseBiz.isValid(activity);
		if (!actValidResult.isOk()) {
			throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(actValidResult.getMsg());
		}

		// 该备注信息仅用于前端记录用户是在哪个宫格位置抽中的对应的卡, 后端仅存储, 不用于业务逻辑
		String remark = params.getString("remark");

		int lotteryCountToday = getLotteryCountToday(activity, user);

		// 每日免费赠送一次抽奖机会, 无需完成任务
		boolean isFree = lotteryCountToday == 0;

		// 获取今日可用于抽奖的任务记录
		List<ActGoodsFlow> actGoodsFlowList = getActGoodsFlowsToday(activity, user);
		// todo
		// 将抽奖模型的代码抽取到别的类, 以便扩展
		StatusObjDto<LotteryResDto> result = actLotteryBiz.commonLottery(
				activityCode, user, customer, getLotteryModelSupplier(activity, lotteryCountToday), isFree,
				actGoodsFlowList, remainToday(activity, user), remark);

		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}

	private List<ActGoodsFlow> getActGoodsFlowsToday(Activity activity, AccTokenUser user) {
		Date now = new Date();
		Date todayStart = DateUtils.getDayStart(now);
		Date todayEnd = DateUtils.getDayEnd(now);
		ActGoodsFlow qryActGoodsFlow = new ActGoodsFlow()
				.setActivityCode(activity.getCode())
				.setMobile(user.getMobile())
				.setFlag(ActGoodsFlow.FLOW_FLAG_GET)
				.setCreateTimeStart(todayStart)
				.setCreateTimeEnd(todayEnd);

		List<ActGoodsFlow> actGoodsFlowList = actGoodsFlowDao.findList(qryActGoodsFlow);
		return actGoodsFlowList;
	}

	private List<ActGoodsFlow> getActGoodsFlowsTodayTotal(Activity activity, AccTokenUser user) {
		Date now = new Date();
		Date todayStart = DateUtils.getDayStart(now);
		Date todayEnd = DateUtils.getDayEnd(now);
		ActGoodsFlow qryActGoodsFlow = new ActGoodsFlow()
				.setActivityCode(activity.getCode())
				.setMobile(user.getMobile())
				.setCreateTimeStart(todayStart)
				.setCreateTimeEnd(todayEnd);

		List<ActGoodsFlow> actGoodsFlowList = actGoodsFlowDao.findList(qryActGoodsFlow);
		return actGoodsFlowList;
	}

	private Supplier<Object> getLotteryModelSupplier(Activity activity, int lotteryCountToday) {
		return () -> {
			Date now = new Date();
			int days = DateUtils.daysBetween(activity.getDateFrom(), now);
			String prizeTypeCode = CARD_LOTTERY_2023818[(days * 3 + lotteryCountToday) % CARD_LOTTERY_2023818.length];
			ActPrizeType prizeType = actPrizeTypeBiz.getPrizeType(prizeTypeCode);
			if (prizeType == null) {
				throw ActivityBizException.ACT_PRIZE_TYPE_NOT_EXIST;
			}
			// 根据该奖品类型编码直接生成奖品, 并入库, 模拟奖品数量无限的情形

			ActPrize prize = new ActPrize();
			prize.setActivityCode(activity.getCode());
			prize.setType(ACT_PRIZE_TYPE_CARD);
			prize.setStatus(ActPrize.STATUS_NOT_SEND);
			prize.setCode(prizeType.getCode());
			prize.setCreateTime(now);
			prize.setIsDeleted(0);
			actPrizeDao.insert(prize);
			return prize;
		};
	}

	/**
	 * 获取今日抽奖次数
	 */
	private int getLotteryCountToday(Activity activity, AccTokenUser user) {
		Date now = new Date();
		Date todayStart = DateUtils.getDayStart(now);
		Date todayEnd = DateUtils.getDayEnd(now);
		ActLottery qryActLottery = new ActLottery();
		qryActLottery.setActivity(activity.getCode());
		qryActLottery.setUserId(user.getUserId());
		qryActLottery.setTimeStart(todayStart);
		qryActLottery.setTimeEnd(todayEnd);
		return actLotteryDao.findCount(qryActLottery);
	}


	/**
	 * 抽奖信息查询
	 *
	 * @param activityCode | 活动编码 |  | required
	 * @return com.wlzq.activity.lottery.dto.LotteryQueryResDto
	 * @cate 12连刮视频月卡活动
	 */
	@Signature(true)
	@MustLogin(true)
	public ResultDto querylotteryinfo(RequestParams params, AccTokenUser user) {
		String activityCode = params.getString("activityCode");

		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}

		String mobile = user == null?null:user.getMobile();

		StatusObjDto<LotteryQueryResDto> result = actLotteryBiz.queryLotteryInfo(activityCode, mobile);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

	@Signature(true)
	@MustLogin(true)
	public ResultDto querytopprize(RequestParams params, AccTokenUser user) {
		String activityCode = params.getString("activityCode");

		StatusObjDto<PrizeResDto> result = actLotteryBiz.queryTopPrize(activityCode);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}


	/**
	 * 返回用户抽奖信息
	 *
	 * @param activityCode | 活动编码 |  | required
	 * @return com.wlzq.activity.lottery.dto.LotteryInfoDto
	 * @cate 2023818理财活动
	 */
	@Signature(true)
	@MustLogin(true)
	public ResultDto lotteryinfo(RequestParams params, AccTokenUser user) {
		String activityCode = params.getString("activityCode");

		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}

		Activity activity = activityBaseBiz.findActivity(activityCode);
		StatusDto actValidResult = activityBaseBiz.isValid(activity);
		if (!actValidResult.isOk()) {
			throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(actValidResult.getMsg());
		}
		LotteryInfoDto lotteryInfoDto = new LotteryInfoDto().setTodayLeft(remainToday(activity, user));
		return new ResultDto(0,BeanUtils.beanToMap(lotteryInfoDto),"");
	}


	/**
	 * 获取今日剩余抽奖次数, 该方法仅能用于 2023818 财富节活动
	 */
	private int remainToday(Activity activity, AccTokenUser user) {
		List<ActGoodsFlow> actGoodsFlowList  = getActGoodsFlowsTodayTotal(activity, user);
		// 最大可增加的抽奖次数:
		int maxIncrement = Math.min(actGoodsFlowList.size(), 2);

		// 今日已抽奖的次数
		int lotteryCountToday = getLotteryCountToday(activity, user);

		return 1 + maxIncrement - lotteryCountToday;
	}

}
