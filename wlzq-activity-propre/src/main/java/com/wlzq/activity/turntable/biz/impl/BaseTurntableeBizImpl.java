package com.wlzq.activity.turntable.biz.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.turntable.biz.BaseTurntableeBiz;
import com.wlzq.activity.turntable.dao.TurntableeDao;
import com.wlzq.activity.turntable.dao.TurntableePrizeDao;
import com.wlzq.activity.turntable.dto.TurntableeHitDto;
import com.wlzq.activity.turntable.model.Turntablee;
import com.wlzq.activity.turntable.model.TurntableeDice;
import com.wlzq.activity.turntable.model.TurntableeParam;
import com.wlzq.activity.turntable.model.TurntableeParamConfigEnum;
import com.wlzq.activity.turntable.model.TurntableePrize;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccAccountOpeninfo;
import com.wlzq.common.model.account.PointRecord;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.PointBiz;
import com.wlzq.remote.service.common.base.FsdpBiz;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

/**
 * 大转盘业务接口实现
 * @author louie
 *
 */
@Service
public class BaseTurntableeBizImpl implements BaseTurntableeBiz {
	
	private Logger logger = LoggerFactory.getLogger(BaseTurntableeBizImpl.class);
	
	/** 总抽奖限制数*/
	@Autowired
	private	TurntableeDao turntableDao;
	@Autowired
	private	TurntableePrizeDao prizeDao;
	@Autowired
	private	ActPrizeBiz actPrizeBiz;
	@Autowired
	private	PointBiz pointBiz;
	@Autowired
	private ActivityBaseBiz activityBaseBiz;
	@Autowired
	private FsdpBiz fsdpBiz;
   
	@Transactional
	public StatusObjDto<TurntableeHitDto> turn(String userId,String customerId, String mobile, String activity, Long timestamp){
		if(ObjectUtils.isEmptyOrNull(activity)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activity");
		}
		if(ObjectUtils.isEmptyOrNull(userId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		if(ObjectUtils.isEmptyOrNull(mobile)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
		}
		if(ObjectUtils.isEmptyOrNull(timestamp)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("timestamp");
		}
		if(timestamp.toString().length() != 13) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("时间戳精确到毫秒");
		}
		StatusDto isValidAct = activityBaseBiz.isValid(activity);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<TurntableeHitDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		/**检查时间间隔是否有效**/
		checkTime(userId,timestamp);
		/**已抽奖次数**/
		Integer times = getLotteryTimes(userId);
		/**是否为客户**/
		boolean isCustomer = isCustomerLottery(customerId, mobile);
		/**获取大转盘参数**/
		TurntableeParam turntableeParam = geTurntableeParam(activity);
		if (turntableeParam == null) {
			throw ActivityBizException.ACT_TURNTABLE_CONFIG_ERROR;
		}
		/**超过当日最大抽奖次数 **/
		if(turntableeParam.getLotteryLimitTimes() != null && times >= turntableeParam.getLotteryLimitTimes()){
			throw ActivityBizException.TURNTABLE_REACH_LOTTERY_TIMES.format(turntableeParam.getLotteryLimitTimes());
		}
		Integer freeLotteryCount = turntableeParam.getFreeLotteryCount();
		/**当前是否为免费抽奖**/
		Integer isFree = freeLotteryCount.compareTo(times) > 0 ? 1 : 0;
		/**非免费，检查积分，增加积分消耗记录**/
		if (CodeConstant.CODE_NO.equals(isFree)) {
			checkPoint(userId, turntableeParam.getPointLotteryUse());
			pointBiz.addPoint(userId, Long.valueOf(turntableeParam.getPointLotteryUse()), PointRecord.SOURCE_TURNTABLE, 
					PointRecord.FLOW_PLUS, "大转盘抽奖消耗", "");
		}
		TurntableePrize turnPrize = null;
		
		/**奖池模型-初始化奖品再抽奖**/
		if (TurntableeParam.TURN_MODEL_BALLPOOL.equals(turntableeParam.getModelType())) {
			initPrizes(turntableeParam);
			turnPrize = ballLottey(turntableeParam.getRedisKey(), userId);
		}
		if (TurntableeParam.TURN_MODEL_DICE.equals(turntableeParam.getModelType())) {
			turnPrize = diceLottery(userId, isCustomer, turntableeParam);
		}
		
		/**校验是否能获得奖品**/
		if (turnPrize != null) {
			List<ActPrize> hasPrizes = Lists.newArrayList();
			if (TurntableeParam.UNIQUE_BY_ACTIVITY.equals(turntableeParam.getUniqueType())) {
				hasPrizes = actPrizeBiz.findPrize(activity, null, userId, null, null, null, null);
			}
			if (TurntableeParam.UNIQUE_BY_PRIZETYPE.equals(turntableeParam.getUniqueType())) {
				hasPrizes = actPrizeBiz.findPrize(activity, null, userId, null,turnPrize.getPrizeCode(),  null, null);
			}
			if (!hasPrizes.isEmpty()) {
				turnPrize = null;
			}
		}
		
		
		/**更新奖品信息**/
		if (turnPrize != null) {
			ActPrize prize = actPrizeBiz.giveOutPrize(activity, "", turnPrize.getPrizeId(), turnPrize.getPrizeCode(), userId, null, customerId, mobile);
			if(prize == null) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
			}
//			actPrizeBiz.updatePrize(userId, null, customerId, turnPrize.getPrizeId(), ActPrize.STATUS_SEND, mobile, null);
			if (TurntableeParam.TURN_MODEL_BALLPOOL.equals(turntableeParam.getModelType())) {
				ActivityRedis.ACT_TURNTABLE_PRIZES.sremove(turntableeParam.getRedisKey(), "1" + turnPrize.getPrizeId());
			}
		}
		
		Integer isHit = turnPrize == null ? CodeConstant.CODE_NO : CodeConstant.CODE_YES;
		Integer usePoint = isFree.equals(0)?turntableeParam.getPointLotteryUse():0;
		/**添加抽奖记录**/
		int recordId = addTurntableeLotteryRecord(userId, isHit, isFree, usePoint);
		/**若中奖，添加中奖记录**/
		if(isHit.equals(CodeConstant.CODE_YES)) {
			turnPrize.setUserId(userId);
			turnPrize.setTurntableId(Long.valueOf(recordId));
			turnPrize.setCreateTime(new Date());
			prizeDao.insert(turnPrize);
			/**若为积分，账户增加积分**/
			if(turnPrize.getType().equals(ActPrize.TYPE_POINT)) {
				pointBiz.addPoint(userId, turnPrize.getWorth().longValue(), PointRecord.SOURCE_TURNTABLE, PointRecord.FLOW_ADD, "大转盘抽奖获取", "");
			}
		}
		
		times = addLotteryTimes(userId);
		
		/**设置返回**/
		TurntableeHitDto hitDto = new TurntableeHitDto();
		Integer freeCount = turntableeParam.getFreeLotteryCount() - times;
		freeCount = freeCount >= 0 ? freeCount : 0;
		hitDto.setFreeCount(freeCount);
		Integer status = CodeConstant.CODE_YES.equals(isHit) ? TurntableeHitDto.STATUS_HIT : TurntableeHitDto.STATUS_NOT_HIT;
		hitDto.setStatus(status);
		hitDto.setUsePoint(usePoint);
		if (turnPrize != null) {
			hitDto.setType(turnPrize.getType());
			if (ActPrize.TYPE_POINT.equals(turnPrize.getType()) || ActPrize.TYPE_CARD_PASSWORD.equals(turnPrize.getType())) {
				hitDto.setWorth(turnPrize.getWorth().intValue());
			}
			hitDto.setPrizeName(turnPrize.getPrizeName());
			hitDto.setTime(turnPrize.getTime());
		}
		return new StatusObjDto<TurntableeHitDto>(true,hitDto,StatusDto.SUCCESS,"");
	}

//	@Override
//	public StatusObjDto<List<UserPrizeDto>> userPrizes(String userId, Integer start, Integer end) {
//		if(ObjectUtils.isEmptyOrNull(userId)) {
//			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
//		}
//		
//		List<UserPrizeDto> prizes = prizeDao.findUserPrizes(userId, start, end);
//		//积分情况处理
//		for(UserPrizeDto prize:prizes) {
//			prize.setSource(UserPrizeDto.SOURCE_TURNTABLE);
//			if(prize.getType().equals(ActPrize.TYPE_POINT)) {
//				prize.setStatus(ActPrize.STATUS_SEND);
//			}
//		}
//		
//		return new StatusObjDto<List<UserPrizeDto>>(true,prizes,StatusDto.SUCCESS,"");
//	}
//
//	@Override
//	public StatusObjDto<List<TurntableePrizeDto>> prizes(Integer start, Integer end) {
//		List<TurntableePrizeDto> prizes = prizeDao.findPrizes(activity, start, end);
//		//手机与积分情况处理
//		for(TurntableePrizeDto prize:prizes) {
//			if(prize.getType().equals(ActPrize.TYPE_POINT)) {
//				prize.setPrizeName(prize.getWorth()+"积分");
//			}
//		}
//		return new  StatusObjDto<List<TurntableePrizeDto>>(true,prizes,StatusDto.SUCCESS,"");
//	}
//
//	@Override
//	public StatusObjDto<Integer> findNotUsePrizeCount(String userId,String activity) {
//		Integer count = prizeDao.findNotUseCount(userId, activity);
//		return new StatusObjDto<Integer>(true,count,StatusDto.SUCCESS,"");
//	}
	
	/**
	 * 抽球模型，配置大转盘奖品
	 * @param turntableeParam
	 */
	private void initPrizes(TurntableeParam turntableeParam) {
		if (turntableeParam == null) {
			throw ActivityBizException.ACT_TURNTABLE_CONFIG_ERROR;
		}
		String redisKey = turntableeParam.getRedisKey();
		if (ObjectUtils.isEmptyOrNull(redisKey)) {
			throw ActivityBizException.ACT_TURNTABLE_CONFIG_ERROR;
		}
		boolean hasInit = ActivityRedis.ACT_TURNTABLE_PRIZES.exists(redisKey);
		if(hasInit) return;
		logger.info("初始化大转盘奖品.............................");
		
		/**中奖奖品初始化**/
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(turntableeParam.getActivity());
		if(prizes == null) {
			logger.info("大转盘奖品未配置.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("奖品未配置");
		}
		if(prizes.size() == 0) {
			logger.info("无可用的大转盘奖品.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("无可用奖品");
		}
		
		/**奖品，第一位表示是否中奖（0：否，1：是），第二位表示奖品ID**/
		int total = 0;
		String prizePrefix = "1-";
		/**奖品**/
		for(int i = 0;i < prizes.size();i++) {
			String prize = prizePrefix + prizes.get(i);
			ActivityRedis.ACT_TURNTABLE_PRIZES.sadd(redisKey, prize);
			total++;
		}
		/**添加空奖品**/
		prizePrefix = "0-";
		int emptyCount = 0;
		if (turntableeParam.getEmptyPrizeCount() != null) {
			for(int i = 0;i < turntableeParam.getEmptyPrizeCount();i++) {
				String emptyPrize = prizePrefix + i;
				ActivityRedis.ACT_TURNTABLE_PRIZES.sadd(redisKey, emptyPrize);
				total++;
				emptyCount++;
			}
		}
		logger.info("初始化大转盘奖品完毕.............................,total"+total+",prize:"+prizes.size()+",emptyCount:"+emptyCount);
	}
	
	/**
	 * 获取抽奖次数
	 * @param userId
	 * @return 当前次数
	 */
	private Integer getLotteryTimes(String userId) {
		String date = DateUtils.formate(new Date());
		String timesKey = userId + date;
		Integer times = (Integer)ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMES.get(timesKey);
		if(times == null) {
			times = 0;
		}
		return times;
	}
	
	/**
	 * 获取抽奖次数
	 * @param userId
	 * @return 当前次数
	 */
	private Integer addLotteryTimes(String userId) {
		String date = DateUtils.formate(new Date());
		String timesKey = userId + date;
		Integer times = (Integer)ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMES.get(timesKey);
		Integer currentTimes = 1;
		if(times == null) {
			ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMES.set(timesKey,currentTimes);
		}else {
			currentTimes = times + 1;
			ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMES.set(timesKey,currentTimes);
		}
		
		return currentTimes;
	}
	
	/**
	 * 检查抽奖时间间隔，间隔1.5秒之内为非法抽奖
	 * @param userId
	 * @param timestamp
	 */
	private void checkTime(String userId,Long timestamp) {
		Long lastTime = (Long) ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMESTAMP.get(userId);
		if(lastTime != null) {
			Long timespan = timestamp - lastTime;
			if(timespan <= 0) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("无效抽奖");
			}
			if(timespan < 1500) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("抽奖太快，待会再来");
			}
		}
		ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMESTAMP.set(userId, timestamp);
	}

	/**由活动编码获取配置**/
	private TurntableeParam geTurntableeParam(String activity) {
		TurntableeParamConfigEnum configEnum = TurntableeParamConfigEnum.getByActivity(activity);
		if (configEnum == null) {
			throw ActivityBizException.ACT_TURNTABLE_CONFIG_ERROR;
		}
		
		String turntableeParamStr = AppConfigUtils.get(configEnum.getConfigKey());
		if (ObjectUtils.isEmptyOrNull(turntableeParamStr)) {
			throw ActivityBizException.ACT_TURNTABLE_CONFIG_ERROR;
		}
		TurntableeParam turntableeParam = JSONObject.parseObject(turntableeParamStr, TurntableeParam.class);
		if (turntableeParam == null) {
			throw ActivityBizException.ACT_TURNTABLE_CONFIG_ERROR;
		}
		return turntableeParam;
	}
	
	/**判断是否客户抽奖**/
	private boolean isCustomerLottery(String customerId, String mobile) {
		if (ObjectUtils.isNotEmptyOrNull(customerId)) {
			return true;
		}
		/**客户号为空，则查询手机号是否有开户信息**/
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			StatusObjDto<List<AccAccountOpeninfo>> openInfoDto = fsdpBiz.khkhxx("", "", mobile);
			if (openInfoDto.isOk() && openInfoDto.getObj() != null && !openInfoDto.getObj().isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private int addTurntableeLotteryRecord(String userId, Integer isHit, Integer isFree, Integer usePoint) {
		Turntablee record = new Turntablee(); //抽奖记录
		record.setIsFree(isFree);
		record.setIsHit(isHit);
		record.setUsePoint(usePoint);
		record.setUserId(userId);
		record.setCreateTime(new Date());
		turntableDao.insert(record);
		return Integer.valueOf(record.getId().toString());
	}
	
	private void checkPoint(String userId, Integer pointLotteryUse) {
		//查询可使用积分
		StatusObjDto<Long> pointStatus = pointBiz.getPoint(userId,null);
		if(!pointStatus.isOk()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("获取积分信息异常");
		}
		Long totalPoint = pointStatus.getObj();
		if(pointLotteryUse != null && totalPoint != null && totalPoint < pointLotteryUse) {
			throw ActivityBizException.GUESS_POINT_INSUFFICIENT;
		}
	}
	
	/**球抽奖**/
	private TurntableePrize ballLottey(String redisKey, String userId) {
		TurntableePrize turnPrize = new TurntableePrize();
		String prizeStr = (String) ActivityRedis.ACT_TURNTABLE_PRIZES.sRandomMember(redisKey);
		String[] prizeInfo = prizeStr.split("-");
		String hitPreFix = prizeInfo[0];
		/**未中奖，直接返回**/
		if(hitPreFix.equals("0")) {
			ActivityRedis.ACT_TURNTABLE_PRIZES.sremove(redisKey, prizeStr);
			return null;
		}else {
			Long prizeId = Long.valueOf(prizeInfo[1]);
			ActPrize prize = actPrizeBiz.findPrize(prizeId);
			if(prize == null) {
				logger.info("奖品不存在，ID："+prizeId+",userId:"+userId);
				throw ActivityBizException.ACT_PRIZE_NOT_EXIST;
			}
			if(!prize.getStatus().equals(ActPrize.STATUS_NOT_SEND)) {
				logger.info("奖品已被使用，ID："+prizeId+",userId:"+userId+",usedId:"+prize.getUserId());
				throw ActivityBizException.ACT_PRIZE_SENDED;
			}
			/**设置奖品信息**/
			BeanUtils.copyProperties(prize, turnPrize);
			turnPrize.setPrizeCode(prize.getCode());
			turnPrize.setPrizeId(prize.getId());
			turnPrize.setPrizeName(prize.getName());
			if(prize.getType().equals(ActPrize.TYPE_POINT)) {
				turnPrize.setPrizeName(prize.getWorth()+"积分");
			}
		}
		return turnPrize;
	}
	
	private TurntableePrize diceLottery(String userId, Boolean isCustomer, TurntableeParam param) {
		TurntableePrize turnPrize = new TurntableePrize();
		List<TurntableeDice> list = param.getBaseDices();
		if (isCustomer) {
			list = param.getCustomerDices();
		}
		if (list == null || list.isEmpty()) {
			throw ActivityBizException.ACT_TURNTABLE_CONFIG_ERROR;
		}
		String prizeTypeCode = null;
		Double random = Math.random();
		TurntableeDice dice = TurntableeDice.getActLottery(list, random);				
		if (dice == null) {
			logger.info("无匹配概率区间");
			return null;
		}
		prizeTypeCode = dice.getPrizeTypeCode();
		ActPrize prize = actPrizeBiz.findOneAvailablePrize(param.getActivity(), prizeTypeCode);
		if(prize == null) {
			logger.info("奖品不存在，prizeTypeCode："+ prizeTypeCode);
			return null;
		}
		if(!prize.getStatus().equals(ActPrize.STATUS_NOT_SEND)) {
			logger.info("奖品已被使用，ID："+prize.getId()+",usedId:"+prize.getUserId());
			return null;
		}
		BeanUtils.copyProperties(prize, turnPrize);
		turnPrize.setPrizeCode(prize.getCode());
		turnPrize.setPrizeId(prize.getId());
		turnPrize.setPrizeName(prize.getName());
		if(prize.getType().equals(ActPrize.TYPE_POINT)) {
			turnPrize.setPrizeName(prize.getWorth()+"积分");
		}
		return turnPrize;
	}
}
