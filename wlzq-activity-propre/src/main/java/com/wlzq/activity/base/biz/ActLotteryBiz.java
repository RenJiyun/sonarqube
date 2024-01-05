package com.wlzq.activity.base.biz;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.impl.GgqzdRandomPrizeBizImpl;
import com.wlzq.activity.base.biz.impl.OldRandomPrizeBizImpl;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.festival38.biz.impl.Festival38BizImp;
import com.wlzq.activity.lottery.dto.*;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.wlzq.activity.base.dao.ActLotteryDao;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.base.dto.WinDto;
import com.wlzq.activity.base.model.ActLottery;
import com.wlzq.activity.base.model.ActLotteryEnum;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


/**
 * 抽奖记录业务类
 * @author
 * @version 1.0
 */
@Service
@Slf4j
public class ActLotteryBiz extends ActivityBaseBiz{
	@Autowired
	private ActLotteryDao lotteryDao;
	@Autowired
	private ActPrizeBiz actPrizeBiz;
	@Autowired
	private ActPrizeDao actPrizeDao;
	@Autowired
	private	CouponBiz couponBiz;
	@Autowired
	private ActGoodsFlowDao actGoodsFlowDao;
	@Autowired
	private ActPrizeTypeBiz actPrizeTypeBiz;
	@Autowired
	private OldRandomPrizeBizImpl oldRandomPrizeBiz;
	@Autowired
	private GgqzdRandomPrizeBizImpl ggqzdRandomPrizeBiz;

	private Logger logger = LoggerFactory.getLogger(ActLotteryBiz.class);

	private static String PRIZE_REDISKEYSTR_2022 = "COUPON.INVEST.OLDCUS.2022";
	/**2023年直播抽奖1**/
	public final static String ACTIVITY_202338_LOTTERY1 = "ACTIVITY.202338.RICH.WOMEN.LOTTERY1";
	/**2023年直播抽奖2**/
	public final static String ACTIVITY_202338_LOTTERY2 = "ACTIVITY.202338.RICH.WOMEN.LOTTERY2";



	/**
	 * 保存抽奖信息
	 * @param type 类型，1：微信分享
	 * @param activity 活动编码
	 * @param userId
	 * @param openId
	 * @param customerId
	 * @return
	 */
	public Long saveLottery(String activity,String userId,String openId,String customerId,Integer isHit,
			Long prizeId,Integer hasRecieve,String recieveCode) {
		ActLottery lottery = new ActLottery();
		lottery.setActivity(activity);
		lottery.setUserId(userId);
		lottery.setOpenid(openId);
		lottery.setCustomerId(customerId);
		lottery.setIsHit(isHit);
		lottery.setPrize(prizeId);
		lottery.setHasRecieve(hasRecieve);
		lottery.setRecieveCode(recieveCode);
		lottery.setCreateTime(new Date());
		lotteryDao.insert(lottery);
		return lottery.getId();
	}

	/**
	 * 领取奖励
	 * @param lottery
	 * @return
	 */
	public int recievePrize(ActLottery lottery ) {
		lottery.setHasRecieve(CodeConstant.CODE_YES);
		return lotteryDao.update(lottery);
	}

	public ActLottery getLottery(Long lotteryId) {
		ActLottery lottery = lotteryDao.get(lotteryId.toString());
		return lottery;
	}

	public ActLottery getLotteryByRecieveCode(String recieveCode) {
		ActLottery lottery = lotteryDao.findByRecieveCode(recieveCode);
		return lottery;
	}

	/**
	 * 查询抽奖次数
	 * @param activityCode
	 * @param userId
	 * @param openId TODO
	 * @param timeFrom
	 * @param timeTo
	 * @return
	 */
	public int lotteryCount(String activityCode,String userId,String openId,Date timeFrom, Date timeTo) {
		if(ObjectUtils.isEmptyOrNull(activityCode)) return 0;
		if(ObjectUtils.isEmptyOrNull(userId)) return 0;
		ActLottery lottery = new ActLottery();
		lottery.setActivity(activityCode);
		lottery.setUserId(userId);
		lottery.setOpenid(openId);
		lottery.setTimeStart(timeFrom);
		lottery.setTimeEnd(timeTo);
		return  lotteryDao.findCount(lottery);
	}

	/**
	 * 查询抽奖次数
	 * @param activityCode
	 * @param customerId
	 * @param timeFrom
	 * @param timeTo
	 * @return
	 */
	public int customerLotteryCount(String activityCode,String customerId,Date timeFrom,Date timeTo) {
		if(ObjectUtils.isEmptyOrNull(activityCode)) return 0;
		if(ObjectUtils.isEmptyOrNull(customerId)) return 0;
		ActLottery lottery = new ActLottery();
		lottery.setActivity(activityCode);
		lottery.setCustomerId(customerId);
		lottery.setTimeStart(timeFrom);
		lottery.setTimeEnd(timeTo);
		return  lotteryDao.findCount(lottery);
	}

	public StatusObjDto<List<WinDto>> prizes(String activityCode, Integer start,Integer end){
		List<WinDto> prizes = lotteryDao.findPrizes(activityCode, start, end);
		return new  StatusObjDto<List<WinDto>>(true,prizes,StatusDto.SUCCESS,"");
	}


	private static final Map<String, String> RETVIS_TO_APPCONFIG_KEY = new HashMap<>();
	static {
		RETVIS_TO_APPCONFIG_KEY.put("ACTIVITY.OLDCUS.RETURNVISIT2023", "oldcus.returnvisit.prize");
		RETVIS_TO_APPCONFIG_KEY.put("ACTIVITY.OLDCUS.RETURNVISIT2024", "oldcus.returnvisit.prize.2024");
	}
	/**
	 * 固定概率抽奖，适用于用户
	 * @param list
	 * @param activityCode
	 * @param userId
	 * @param openId
	 * @param customerId
	 * @param mobile
	 * @param basePrize
	 * @return
	 */
	public StatusObjDto<LotteryDto> lottery(List<ActLotteryEnum> list, String activityCode, String userId,
											String openId, String customerId, String mobile, ActLotteryEnum basePrize) {
		StatusDto isValidAct = isValid(activityCode);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<LotteryDto>(true, LotteryDto.getThanks());
		}


		//老客回访采用缓存方式（将所有奖品放入一个缓存随机获取）
		List<Long> allPrizeList = Lists.newArrayList();
		/* 奖池里面要放哪些奖品，放在中台配置管理 */
		String configKey = RETVIS_TO_APPCONFIG_KEY.get(activityCode);
		List<String> oldcusPrize = AppConfigUtils.getList(configKey, ",");
		for (String prizeTypeCode : oldcusPrize) {
			List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, prizeTypeCode);
			allPrizeList.addAll(prizeList);
		}

		/* 多个奖品初始化同一个奖池时，奖品编码都用activityCode，方便中台统一清理缓存 */
		String prizeTypeCode = activityCode;

		initPrizes(activityCode, prizeTypeCode, allPrizeList);

		//新增抽奖记录
		ActLottery lottery = new ActLottery();
		lottery.setActivity(activityCode);
		lottery.setUserId(userId);
		lottery.setOpenid(openId);
		lottery.setCustomerId(customerId);
		lottery.setIsHit(1);
		lottery.setCreateTime(new Date());

		//奖品已派完
		ActPrize exitPrize = actPrizeBiz.findOneAvailablePrize(activityCode, prizeTypeCode);
		if (exitPrize == null) {
			lottery.setIsHit(0);
			lotteryDao.insert(lottery);
			return new StatusObjDto<LotteryDto>(true, LotteryDto.getThanks());
		}

		ActPrize prize = null;
		try {
			prize = actPrizeBiz.giveOutPrize(activityCode, "", null, prizeTypeCode, userId, openId, customerId, mobile);
			if(prize == null) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw BizException.NETWORK_ERROR;
		}

		LotteryDto dto = new LotteryDto();
		lottery.setPrize(prize.getId());
		lottery.setHasRecieve(prize.getStatus().compareTo(ActPrize.STATUS_SEND) == 0 ? 1 : 0);
		lottery.setRecieveCode(prize.getRedeemCode());
		lotteryDao.insert(lottery);

		if (!LotteryDto.STATUS_NOT_HIT.equals(lottery.getIsHit())) {
			dto = parseFromPrize(prize);
		}

		return new StatusObjDto<LotteryDto>(true, dto, StatusDto.SUCCESS, "");
	}

	private LotteryDto parseFromPrize(ActPrize prize) {
		LotteryDto dto = new LotteryDto();
		if (prize == null) {
			return dto;
		}
		dto.setPrizeName(prize.getName());
		dto.setRecieveCode(prize.getRedeemCode());
		dto.setStatus(LotteryDto.STATUS_HIT);
		dto.setType(prize.getType());
		dto.setWorth(prize.getWorth());
		dto.setRedEnvelopeAmt(prize.getRedEnvelopeAmt());
		dto.setRedEnvelopeUrl(prize.getRedEnvelopeUrl());

		dto.setCardNo(prize.getCardNo());
		dto.setTime(prize.getTime());
		dto.setCardPassword(prize.getCardPassword());
		StatusObjDto<CouponInfo> couponStatus = couponBiz.couponInfo(null, prize.getRedeemCode());
		if (couponStatus.getCode().equals(StatusDto.SUCCESS)) {
			CouponInfo coupon = couponStatus.getObj();
			dto.setDiscount(coupon.getDiscount());
			dto.setDescription(coupon.getDescription());
			dto.setOpenUrl(coupon.getOpenUrl());
			dto.setCouponType(coupon.getType());
		}
		if (Objects.equals(prize.getHit(),0)) {
			dto.setStatus(LotteryDto.STATUS_NOT_HIT);
		}
		return dto;
	}
	
	/**
	 * 抽奖次数统计
	 * @param activity
	 * @param userId
	 * @param openId
	 * @param customerId
	 * @param threeInOne
	 * @return
	 */
	public int lotteryCount (String activity, String userId, String openId, String customerId, Integer threeInOne, Date timeStart, Date timeEnd) {
		ActLottery lotteryEntity = new ActLottery();
		lotteryEntity.setUserId(userId);
		lotteryEntity.setOpenid(openId);
		lotteryEntity.setActivity(activity);
		lotteryEntity.setCustomerId(customerId);
		lotteryEntity.setTimeStart(timeStart);
		lotteryEntity.setTimeEnd(timeEnd);
		lotteryEntity.setThreeInOne(threeInOne);
		int lotteryCount = lotteryDao.findCount(lotteryEntity);
		return lotteryCount;
	}

	/**
	 * 初始化奖品
	 * @param activity
	 * @param prizeRedis
	 * @param redisPrizeKey
	 * @param emptyPrizeCount
	 * @param ignoreCache
	 */
	public void initPrizes(String activity, ActivityRedis prizeRedis, String redisPrizeKey, Integer emptyPrizeCount, boolean ignoreCache) {
		if(!ignoreCache) {
			boolean hasInit = prizeRedis.exists(redisPrizeKey);
			if(hasInit) return;
		}
		prizeRedis.del(redisPrizeKey);
		logger.info("初始化奖品.............................");

		//中奖奖品初始化
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(activity);

		if(prizes.size() == 0) {
			logger.info("无可用的奖品.............................");
		}

		//奖品，第一位表示是否中奖（0：否，1：是）,第二表示奖品ID
		//第三位表示奖品ID
		String statusPrefix = "1-";
		//添加奖品
		for(int i = 0;i < prizes.size();i++) {
			String prize = statusPrefix + prizes.get(i);
			prizeRedis.sadd(redisPrizeKey, prize);
		}
		//添加空奖品
	    statusPrefix = "0-";
		for(int i = 0;i < emptyPrizeCount;i++) {
			String emptyPrize = statusPrefix + i;
			prizeRedis.sadd(redisPrizeKey, emptyPrize);
		}
		Integer total = prizes.size() + emptyPrizeCount;
		logger.info("初始化奖品完毕.............................,total"+total+",prizes:"+prizes.size()+",empty:"+emptyPrizeCount);
	}

	/**
	 * 从缓存抽奖
	 * @param prizeRedis
	 * @param redisPrizeKey
	 * @param unExpectPrizeCodes TODO
	 * @return
	 */
	public LotteryDto lottery (ActivityRedis prizeRedis, String redisPrizeKey, List<String> unExpectPrizeCodes) {
		LotteryDto lotteryDto = new LotteryDto();
		//抽奖
		String prizeStr = (String) prizeRedis.sRandomMember(redisPrizeKey);
		/**奖品已抽完**/
		if (ObjectUtils.isEmptyOrNull(prizeStr)) {
			lotteryDto.setStatus(CodeConstant.CODE_NO);
			return lotteryDto;
		}

		String[] prizes = prizeStr.split("-");
		Integer status = Integer.valueOf(prizes[0]);
		Long prizeId = Long.valueOf(prizes[1]);
		lotteryDto.setStatus(status);
		/**不中奖**/
		if (CodeConstant.CODE_NO.equals(status)) {
			lotteryDto.setStatus(CodeConstant.CODE_NO);
			return lotteryDto;
		}

		/**奖品已发出**/
		ActPrize prize = actPrizeBiz.findPrize(prizeId);
		if(prize == null || !prize.getStatus().equals(ActPrize.STATUS_NOT_SEND)) {
			lotteryDto.setStatus(CodeConstant.CODE_NO);
			return lotteryDto;
		}
		/**非预期奖品**/
		if (unExpectPrizeCodes != null && unExpectPrizeCodes.size() > 0 && unExpectPrizeCodes.contains(prize.getCode())) {
			lotteryDto.setStatus(CodeConstant.CODE_NO);
			return lotteryDto;
		}
		/**删除该奖品缓存**/
		ActivityRedis.ACT_WORKS_VOTE_PRIZES.sremove(redisPrizeKey, prizeStr);
		if (prize != null) {
			lotteryDto.setStatus(1);
			lotteryDto.setPrizeId(prize.getId());
			lotteryDto.setCardNo(prize.getCardNo());
			lotteryDto.setCardPassword(prize.getCardPassword());
			lotteryDto.setPrizeName(prize.getName());
			lotteryDto.setWorth(prize.getWorth());
			lotteryDto.setRecieveCode(prize.getRedeemCode());
		}
		return lotteryDto;
	}

	/**
	 * 抽球模型，配置大转盘奖品
	 *
	 * @param turntableeParam
	 */
	private void initPrizes(String activityCode, String prizeTypeCode, List<Long> prizes) {

		String redisKey = activityCode + ":" + prizeTypeCode;
		boolean hasInit = ActivityRedis.ACT_ACTVITY_PRIZE.exists(redisKey);
		if (hasInit)
			return;
		logger.info("初始化大转盘奖品.............................");

		/** 中奖奖品初始化 **/
		if (prizes == null) {
			logger.info("活动奖品未配置.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动奖品未配置");
		}
		if (prizes.size() == 0) {
			logger.info("无可用的活动奖品.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("无可用奖品");
		}

		/** 奖品 **/
		int total = 0;
		for (int i = 0; i < prizes.size(); i++) {
			ActivityRedis.ACT_ACTVITY_PRIZE.sadd(redisKey, prizes.get(i));
			total++;
		}
		logger.info("初始化大转盘奖品完毕.............................,total" + total + ",prize:" + prizes.size());
	}
	


	/**
	 * 查询抽奖列表
	 */
	public StatusObjDto<List<CouponRecieveStatusDto>> getPrizeStatus(String activityCode, String userId, String openId, String customerId, String mobile) {
		if (StringUtils.isEmpty(userId)) {
			throw BizException.NOT_LOGIN_ERROR;
		}
		/*2023年财女节抽奖，根据userId获取奖品**/
		List<CouponRecieveStatusDto> list = Lists.newArrayList();
		if (ActLotteryBiz.ACTIVITY_202338_LOTTERY1.equals(activityCode) || ActLotteryBiz.ACTIVITY_202338_LOTTERY2.equals(activityCode)) {
			List<ActPrize> actPrizes = actPrizeDao.findLotteryActPrize(activityCode, userId);
			if (!CollectionUtils.isEmpty(actPrizes)) {
				for (ActPrize actPrize : actPrizes) {
					list.add(getCouponRecieveStatusDto(actPrize));
				}
			}
		}
		return new StatusObjDto<>(true, list, StatusDto.SUCCESS, "");
	}


	public StatusObjDto<CouponRecieveStatusDto> lottery38(List<ActLotteryEnum> list, String activityCode, String userId,
											String openId, String customerId, String mobile, ActLotteryEnum basePrize) {
		StatusDto isValidAct = isValid(activityCode);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<>(true, CouponRecieveStatusDto.NOT_HIT);
		}

		//获取客户号
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			customerId = getCustomerByMobile(mobile);
		}

		/*检查是否已获奖**/
		StatusObjDto<List<CouponRecieveStatusDto>> statusObjDto = getPrizeStatus(activityCode, userId, null, null, null);
		if (!statusObjDto.isOk()) {
			return new StatusObjDto<>(false, CouponRecieveStatusDto.NOT_HIT, statusObjDto.getCode(), statusObjDto.getMsg());
		}
		if (statusObjDto.isOk() && !statusObjDto.getObj().isEmpty()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("您已参与过抽奖，不可重复参与");
		}

		/*2023-38财女节使用固定概率抽奖**/
		/*随机数抽取奖品**/
		Double random = Math.random();
		ActLotteryEnum lotteryEnum = ActLotteryEnum.getActLottery(list, random);
		String prizeTypeCode = lotteryEnum.getPrizeTypeCode();
		logger.info("{}本次随机数为：{}抽奖中奖奖品：{}", userId, random, prizeTypeCode);

		/* 查询剩余可用奖品**/
		List<Long> allPrizeList = actPrizeDao.findAvailablePrizesByType(activityCode,prizeTypeCode);
		if (allPrizeList.isEmpty()) {
			//新增抽奖记录
			ActLottery lottery = new ActLottery();
			lottery.setActivity(activityCode);
			lottery.setUserId(userId);
			lottery.setOpenid(openId);
			lottery.setCustomerId(customerId);
			lottery.setCreateTime(new Date());
			lottery.setIsHit(0);
			lotteryDao.insert(lottery);
			return new StatusObjDto<>(true, CouponRecieveStatusDto.NOT_HIT);
		}

		initPrizes(activityCode, prizeTypeCode, allPrizeList);

		//新增抽奖记录
		ActLottery lottery = new ActLottery();
		lottery.setActivity(activityCode);
		lottery.setUserId(userId);
		lottery.setOpenid(openId);
		lottery.setCustomerId(customerId);
		lottery.setIsHit(1);
		lottery.setCreateTime(new Date());

		//奖品已派完
		ActPrize exitPrize = actPrizeBiz.findOneAvailablePrize(activityCode, prizeTypeCode);
		if (exitPrize == null) {
			lottery.setIsHit(0);
			lotteryDao.insert(lottery);
			return new StatusObjDto<>(true, CouponRecieveStatusDto.NOT_HIT);
		}

		ActPrize prize;
		try {
			prize = actPrizeBiz.giveOutPrize(activityCode, "", null, prizeTypeCode, userId, openId, customerId, mobile);
			if(prize == null) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw BizException.NETWORK_ERROR;
		}

		lottery.setPrize(prize.getId());
		lottery.setHasRecieve(prize.getStatus().compareTo(ActPrize.STATUS_SEND) == 0 ? 1 : 0);
		lottery.setRecieveCode(prize.getRedeemCode());
		lotteryDao.insert(lottery);

		CouponRecieveStatusDto statusDto = getCouponRecieveStatusDto(prize);
		return new StatusObjDto<>(true, statusDto, StatusDto.SUCCESS, "");
	}

	private CouponRecieveStatusDto getCouponRecieveStatusDto(ActPrize prize) {
		if (Objects.equals(prize.getHit(),0)) {
			return CouponRecieveStatusDto.NOT_HIT;
		}
		//prize 转  CouponRecieveStatusDto
		CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
		CouponInfo coupon = new CouponInfo();
		if(ActPrizeBiz.isCoupon(prize.getType())){
			coupon = couponBiz.couponInfo(null, prize.getRedeemCode()).getObj();
		}
		MyPrizeDto myPrizeDto = Festival38BizImp.buildMyPrizeDto(prize, coupon);
		myPrizeDto.setWorth(coupon.getAmount());
		myPrizeDto.setName(coupon.getName());
		myPrizeDto.setTime(coupon.getTime());
		statusDto.setPrize(myPrizeDto);
		statusDto.setPrizeType(prize.getCode());
		statusDto.setStatus(ActPrize.STATUS_SEND);
		return statusDto;
	}


	/**
	 * 抽奖
	 * 抽奖机会由任务流水决定
	 */
	@Transactional(rollbackFor = Exception.class)
	public StatusObjDto<LotteryResDto> commonLottery(LotteryReqDto reqDto) {
		String activityCode = reqDto.getActivityCode();
		String mobile = reqDto.getMobile();
		String userId = reqDto.getUserId();
		String customerId = reqDto.getCustomerId();
		String openId = reqDto.getOpenId();

		if (StringUtils.isBlank(mobile)) {
			throw BizException.USER_NOT_BIND_MOBILE;
		}

		StatusDto isValidAct = isValid(activityCode);
		if (!isValidAct.isOk()) {
			return new StatusObjDto<>(true, LotteryResDto.NOT_HIT);
		}


		// 获取该活动下的任务流水, 用于计算剩余抽奖次数
		List<ActGoodsFlow> actGoodsFlows = remainLottery(activityCode, mobile);
		if (CollectionUtils.isEmpty(actGoodsFlows)) {
			log.info("剩余抽奖次数为0");
			return new StatusObjDto<>(true, LotteryResDto.NOT_HIT);
		}

		Map<String, Object> randomContext = new HashMap<>();
		randomContext.put("activityCode", activityCode);
		randomContext.put("userId", userId);
		randomContext.put("customerId", customerId);

		// 根据不同的活动, 调用不同的抽奖逻辑
		RandomPrizeBiz randomPrizeBiz = ActivityBiz.ACTIVITY_GGQZD.equals(activityCode) ? ggqzdRandomPrizeBiz : oldRandomPrizeBiz;

		RandomPrizeBiz.RandomResult randomResult = randomPrizeBiz.randomPrize(randomContext);

		// 若未中奖, 则直接返回
		if (CollectionUtil.isEmpty(randomResult.getPrizeIds())) {
			ActLottery lottery = new ActLottery();
			lottery.setActivity(activityCode);
			lottery.setUserId(userId);
			lottery.setOpenid(openId);
			lottery.setCustomerId(customerId);
			lottery.setCreateTime(new Date());
			lottery.setIsHit(0);
			lotteryDao.insert(lottery);
			updateUserTaskStatus(actGoodsFlows);
			return new StatusObjDto<>(true, LotteryResDto.NOT_HIT);
		}

		String prizeTypeCode = randomResult.getPrizeTypeCode();
		List<Long> allPrizeList = randomResult.getPrizeIds();
		ActLotteryEnum lotteryEnum = randomResult.getActLotteryEnum();

		// 初始化奖品池
		initPrizes(activityCode, prizeTypeCode, allPrizeList);

		//新增抽奖记录
		ActLottery lottery = new ActLottery();
		lottery.setActivity(activityCode);
		lottery.setUserId(userId);
		lottery.setOpenid(openId);
		lottery.setCustomerId(customerId);
		lottery.setIsHit(1);
		lottery.setCreateTime(new Date());

		ActPrize exitPrize = actPrizeBiz.findOneAvailablePrize(activityCode, prizeTypeCode);
		//奖品已派完
		if (exitPrize == null) {
			lottery.setIsHit(0);
			lotteryDao.insert(lottery);
			updateUserTaskStatus(actGoodsFlows);
			return new StatusObjDto<>(true, LotteryResDto.NOT_HIT);
		}

		ActPrize prize;
		try {
			prize = actPrizeBiz.giveOutPrizeNew(activityCode, "", null, prizeTypeCode, userId, openId, customerId, mobile);
			if(prize == null) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw BizException.NETWORK_ERROR;
		}

		lottery.setPrize(prize.getId());
		lottery.setHasRecieve(prize.getStatus().compareTo(ActPrize.STATUS_SEND) == 0 ? 1 : 0);
		lottery.setRecieveCode(prize.getRedeemCode());
		updateUserTaskStatus(actGoodsFlows);
		lotteryDao.insert(lottery);

		LotteryResDto resDto = new LotteryResDto()
				.setStatus(LotteryResDto.RECEIVED)
				.setHit(CodeConstant.CODE_YES)
				.setPrizeType(prizeTypeCode)
				.setPrizeName(lotteryEnum.getPrizeName())
				.setAmount(lotteryEnum.getAmount());
		return new StatusObjDto<>(true, resDto, StatusDto.SUCCESS, "");
	}


	/**
	 * 抽奖
	 *
	 * @param lotteryModelSupplier 随机抽奖模型, 用于产生一个随机奖项
	 * @param isFree               是否免费抽奖
	 * @param actGoodsFlows        剩余抽奖次数: 抽奖次数目前在业务上由任务的完成数决定
	 */
	@Transactional(rollbackFor = Exception.class)
	public StatusObjDto<LotteryResDto> commonLottery(String activityCode, AccTokenUser user, Customer customer,
													 Supplier<Object> lotteryModelSupplier, boolean isFree,
													 List<ActGoodsFlow> actGoodsFlows, int remainToday, String remark) {

		if (remainToday <= 0) {
			// 若没有剩余抽奖次数，则返回未中奖
			return new StatusObjDto<>(true, LotteryResDto.USED_UP);
		}

		// 随机抽奖模型返回 null 时，表示不中奖
		Object maybePrize = lotteryModelSupplier.get();

		if (maybePrize == null) {
			return new StatusObjDto<>(true, LotteryResDto.NOT_HIT);
		}

		ActPrize prize = null;
		if (maybePrize instanceof ActPrize) {
			// 这种情况下, 奖品由外部生成, 一般用于表示奖品可以无限抽
			prize = (ActPrize) maybePrize;
		} else {
			String prizeTypeCode = (String) maybePrize;
			ActPrizeType prizeType = actPrizeTypeBiz.getPrizeType(prizeTypeCode);
			if (prizeType == null) {
				throw ActivityBizException.ACT_PRIZE_TYPE_NOT_EXIST;
			}
			actPrizeTypeBiz.initPrizes(activityCode, prizeType);
			prize = actPrizeTypeBiz.getOneAvailablePrize(activityCode, prizeType);
		}

		ActLottery lottery = new ActLottery();
		lottery.setActivity(activityCode);
		lottery.setUserId(user.getUserId());
		lottery.setOpenid(user.getOpenid());
		lottery.setCustomerId(customer != null ? customer.getCustomerId() : null);
		lottery.setCreateTime(new Date());

		// 奖品已经全部领取完, 则返回不中奖
		if (prize == null) {
			lottery.setIsHit(0);
			lotteryDao.insert(lottery);
			// 消耗抽奖次数
			if (!isFree) {
				updateUserTaskStatus(actGoodsFlows);
			}
			return new StatusObjDto<>(true, LotteryResDto.NOT_HIT);
		} else {
			lottery.setIsHit(1);
		}

		// 抽中奖品, 进行发奖
		try {
			prize = actPrizeBiz.giveOutPrize(activityCode, prize, user, customer, remark);
			if(prize == null) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw BizException.NETWORK_ERROR;
		}

		lottery.setPrize(prize.getId());
		lottery.setHasRecieve(prize.getStatus().compareTo(ActPrize.STATUS_SEND) == 0 ? 1 : 0);
		lottery.setRecieveCode(prize.getRedeemCode());
		if (!isFree) {
			updateUserTaskStatus(actGoodsFlows);
		}
		lotteryDao.insert(lottery);

		LotteryResDto resDto = new LotteryResDto()
				.setStatus(LotteryResDto.RECEIVED)
				.setHit(CodeConstant.CODE_YES)
				.setPrizeType(prize.getCode())
				.setPrizeName(prize.getName())
				.setPrizeCode(prize.getRedeemCode())
				.setPrizeId(prize.getId());

		return new StatusObjDto<>(true, resDto, StatusDto.SUCCESS, "");
	}



	public List<ActGoodsFlow> remainLottery(String activityCode, String mobile){
		ActGoodsFlow entity = new ActGoodsFlow().setActivityCode(activityCode).setMobile(mobile)
				.setFlag(ActGoodsFlow.FLOW_FLAG_GET);
		return actGoodsFlowDao.findList(entity);
	}

	/**
	 * 更新用户的任务抽奖状态
	 */
	public void updateUserTaskStatus(List<ActGoodsFlow> goodsFlows){
		if (!CollectionUtils.isEmpty(goodsFlows)) {
			List<ActGoodsFlow> list = goodsFlows.stream().sorted(Comparator.comparing(ActGoodsFlow::getCreateTime)).collect(Collectors.toList());
			ActGoodsFlow actGoodsFlow = list.get(0);

			// 对于flag的计算不同的活动有不同的策略
			updateActGoodsFlowFlag(actGoodsFlow);

			actGoodsFlow.setUpdateTime(new Date());
			actGoodsFlowDao.update(actGoodsFlow);
		}
	}

	private void updateActGoodsFlowFlag(ActGoodsFlow actGoodsFlow) {
		if (ActivityBiz.ACTIVITY_GGQZD.equals(actGoodsFlow.getActivityCode())) {
			if (actGoodsFlow.getUsedGoodsQuantity() == null || actGoodsFlow.getUsedGoodsQuantity().compareTo(0.0) <= 0) {
				actGoodsFlow.setUsedGoodsQuantity(1.0);
			} else {
				actGoodsFlow.setUsedGoodsQuantity(actGoodsFlow.getUsedGoodsQuantity() + 1.0);
			}
			// 只有消耗完所有的任务奖品数量, 该任务才设置为已消耗
			if (actGoodsFlow.getUsedGoodsQuantity().compareTo(actGoodsFlow.getGoodsQuantity()) >= 0) {
				actGoodsFlow.setFlag(ActGoodsFlow.FLOW_FLAG_CONSUME);
			}
		} else {
			actGoodsFlow.setFlag(ActGoodsFlow.FLOW_FLAG_CONSUME);
		}
	}


	public StatusObjDto<LotteryQueryResDto> queryLotteryInfo(String activityCode, String mobile){
		LotteryQueryResDto resDto = new LotteryQueryResDto();
		//如果手机号为空，剩余次数就返回0，今天就返回未完成任务。
		if (StringUtils.isBlank(mobile)) {
			resDto.setRemainTimes(0).setTaskStatus(CodeConstant.CODE_NO);
			return new StatusObjDto<>(true, resDto, StatusDto.SUCCESS, "");
		}

		// 填充抽奖活动次数信息: 累计可抽奖次数, 剩余次数等
		populateLotteryTimesInfo(activityCode, mobile, resDto);

		//查询今天是否完成任务
		Date now = new Date();
		Date beginOfDay = DateUtil.beginOfDay(now);
		Date endOfDay = DateUtil.endOfDay(now);
		ActGoodsFlow entity = new ActGoodsFlow().setActivityCode(activityCode).setMobile(mobile)
				.setCreateTimeStart(beginOfDay).setCreateTimeEnd(endOfDay);
		List<ActGoodsFlow> list = actGoodsFlowDao.list(entity);
		if (!CollectionUtils.isEmpty(list)) {
			resDto.setTaskStatus(CodeConstant.CODE_YES);
		}else{
			resDto.setTaskStatus(CodeConstant.CODE_NO);
		}
		return new StatusObjDto<>(true, resDto, StatusDto.SUCCESS, "");
	}

	private void populateLotteryTimesInfo(String activityCode, String mobile, LotteryQueryResDto resDto) {
		ActGoodsFlow actGoodsFlowQry = new ActGoodsFlow();
		actGoodsFlowQry.setActivityCode(activityCode);
		actGoodsFlowQry.setMobile(mobile);
		List<ActGoodsFlow> actGoodsFlowList = actGoodsFlowDao.findList(actGoodsFlowQry);
		if (CollectionUtils.isEmpty(actGoodsFlowList)) {
			resDto.setTotalTimes(0);
			resDto.setRemainTimes(0);
		} else {
			Map<Integer, List<ActGoodsFlow>> groupByFlag = actGoodsFlowList.stream()
					.collect(Collectors.groupingBy(ActGoodsFlow::getFlag));

			// 每个活动基于自己的规则计算抽奖次数相关信息
			if (ActivityBiz.ACTIVITY_GGQZD.equals(activityCode)) {
				Double totalTimes = actGoodsFlowList.stream().map(ActGoodsFlow::getGoodsQuantity)
						.reduce(Double::sum).orElse(0.0);
				resDto.setTotalTimes(totalTimes.intValue());
				List<ActGoodsFlow> notConsumedList = groupByFlag.get(ActGoodsFlow.FLOW_FLAG_GET);
				if (CollectionUtil.isNotEmpty(notConsumedList)) {
					Double remainTimes = notConsumedList.stream()
							.map(e -> e.getGoodsQuantity() - (e.getUsedGoodsQuantity() == null ? 0.0 : e.getUsedGoodsQuantity()))
							.reduce(Double::sum).orElse(0.0);
					resDto.setRemainTimes(remainTimes.intValue());
				} else {
					resDto.setRemainTimes(0);
				}
			} else {
				// 目前所有其他的活动任务都是一次任务记录贡献一次抽奖次数
				List<ActGoodsFlow> notConsumedList = groupByFlag.get(ActGoodsFlow.FLOW_FLAG_GET);
				resDto.setTotalTimes(actGoodsFlowList.size());
				resDto.setRemainTimes(CollectionUtil.isEmpty(notConsumedList) ? 0 : notConsumedList.size());
			}
		}
	}


	public StatusObjDto<PrizeResDto> queryTopPrize(String activityCode) {
		PrizeResDto resDto = null;
		List<String> topPrizeCodes = ActLotteryEnum.TOP_PRIZE_CODES_LOTTERY_PZB;

		ActPrize actPrize = actPrizeDao.queryLastPrizes(activityCode, topPrizeCodes);
		if (actPrize != null) {
			String mobile = getDesensitizeMobile(actPrize.getMobile());
			resDto = new PrizeResDto();
			resDto.setPrizeCode(actPrize.getCode())
					.setUpdateTime(actPrize.getUpdateTime())
					.setMobile(mobile);
		}
		return new StatusObjDto<>(true, resDto, StatusDto.SUCCESS, "");
	}


	public String getDesensitizeMobile(String mobile) {
		if (StringUtils.isNotBlank(mobile)) {
			int length = mobile.length();
			if (length >=11) {
				mobile = mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
			}else if (length >=4) {
				mobile = mobile.substring(length-4,length);
			}else{
				mobile = "";
			}
		}
		return mobile;
	}
}
