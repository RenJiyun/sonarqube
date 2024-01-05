package com.wlzq.activity.turntable51.biz.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.turntable51.biz.TurntableBiz;
import com.wlzq.activity.turntable51.dao.TurntableDao;
import com.wlzq.activity.turntable51.dto.TurntableHitDto;
import com.wlzq.activity.turntable51.model.Turntable;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.model.account.PointRecord;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.PointBiz;
import com.wlzq.remote.service.utils.RemoteUtils;

/**
 * 大转盘业务接口实现
 * @author louie
 *
 */
@Service
public class TurntableBizImpl extends ActivityBaseBiz implements TurntableBiz {
	
	private Logger logger = LoggerFactory.getLogger(TurntableBizImpl.class);
	/** 活动编码*/
	private static final String ACTIVITY_CODE="ACTIVITY.TURNTABLE51";
	/** 免费抽奖奖品 redis key*/
	private static final String LOTTERY_FREE_KEY = "free";
	/** 消耗积分抽奖奖品 redis key*/
	private static final String LOTTERY_POINT_KEY = "point";
	/** 免费抽奖50积分中奖概率*/
	private static final Double FREE_HIT_RATIO =  0.2;
	/** 消耗积分抽奖非积分奖品中奖概率*/
	private static final Double POINT_OTHER_RATIO = 0.3;
	/** 消耗积分抽奖50积分中奖概率*/
	private static final Double POINT_50_RATIO = 0.5;
	/** 消耗积分抽奖100积分中奖概率*/
	private static final Double POINT_100_RATIO = 0.2;
	/** 免费抽奖中奖积分*/
	private static final Integer FREE_HIT_POINT = 50;
	/** 每次抽奖消耗积分*/
	private static final Integer POINT_LOTTERY_USE = 50;
	/** 免费抽奖机会数*/
	private static final Integer FREE_LOTTERY_COUNT = 3;
	/** 总抽奖限制数*/
	private static final Integer LOTTERY_LIMIT_TIMES = 10;
	@Autowired
	private	TurntableDao turntableDao;
	@Autowired
	private	ActPrizeBiz actPrizeBiz;
	@Autowired
	private	PointBiz pointBiz;
   
	@Transactional
	public StatusObjDto<TurntableHitDto> turn(String userId,String openId,String customerId,Long timestamp){
		if(ObjectUtils.isEmptyOrNull(userId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		if(ObjectUtils.isEmptyOrNull(timestamp)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("timestamp");
		}
		if(timestamp.toString().length() != 13) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("时间戳精确到毫秒");
		}
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<TurntableHitDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		//检查时间间隔是否有效
		checkTime(userId,timestamp);
		
		Integer times = addLotteryTimes(userId);
		
		//初始化奖品
		initPrizes();
		
		TurntableHitDto hitDto = new TurntableHitDto();
		hitDto.setStatus(TurntableHitDto.STATUS_NOT_HIT);
		
		Turntable params = new Turntable();
		params.setUserId(userId);
		Date now = new Date();
		params.setCreateTimeFrom(DateUtils.getDayStart(now));
		params.setCreateTimeTo(now);

		Integer isFree = 1;
		if(times > LOTTERY_LIMIT_TIMES){
			throw ActivityBizException.TURNTABLE_REACH_LOTTERY_TIMES.format(LOTTERY_LIMIT_TIMES);
		}else {//抽奖
			if(times > 3) {
				isFree = 0;
				StatusObjDto<Long> pointStatus = pointBiz.getPoint(userId,null);
				if(!pointStatus.isOk()) {
					throw BizException.COMMON_CUSTOMIZE_ERROR.format("获取积分信息异常");
				}
				Long totalPoint = pointStatus.getObj();
				if(totalPoint < POINT_LOTTERY_USE) {
					hitDto.setStatus(TurntableHitDto.STATUS_NO_POINT);
					hitDto.setUsePoint(0);
					return new StatusObjDto<TurntableHitDto>(true,hitDto,StatusDto.SUCCESS,"积分不足");
					//throw ActivityBizException.GUESS_POINT_INSUFFICIENT;
				}
			}
			Long prizeId = pointLotteryPrize();
			if(ObjectUtils.isNotEmptyOrNull(prizeId)) {
				ActPrize prize = actPrizeBiz.findPrize(prizeId);
				Integer type = prize.getType();
				hitDto.setType(type);
				hitDto.setStatus(TurntableHitDto.STATUS_HIT);
				if(prize == null) {
					logger.info("奖品不存在，ID："+prizeId+",userId:"+userId);
					return new StatusObjDto<TurntableHitDto>(false,hitDto,StatusDto.FAIL_COMMON,"奖品不存在");
				}
				if(!prize.getStatus().equals(ActPrize.STATUS_NOT_SEND)) {
					logger.info("奖品已被 发出，ID："+prizeId+",userId:"+userId+",usedId:"+prize.getUserId());
					return new StatusObjDto<TurntableHitDto>(false,hitDto,StatusDto.FAIL_COMMON,"奖品不存在");
				}
				hitDto.setStatus(TurntableHitDto.STATUS_HIT);
				Integer worth = prize.getWorth() != null?prize.getWorth().intValue():null;
				hitDto.setWorth(worth);
				hitDto.setPrizeName(prize.getName());
				hitDto.setType(type);
				hitDto.setTime(prize.getTime());
				//设置奖品信息
				Integer upStatus = ActPrize.STATUS_SEND;
				if(type.equals(ActPrize.TYPE_COUPON) && ObjectUtils.isEmptyOrNull(customerId)) {
					upStatus = ActPrize.STATUS_OCCUPY;
				}else if(type.equals(ActPrize.TYPE_COUPON) && ObjectUtils.isNotEmptyOrNull(customerId)) {
					Map<String, Object> busparams = Maps.newHashMap();
					busparams.put("userId", userId);
					busparams.put("customerId", customerId);
					busparams.put("code", prize.getRedeemCode());
					ResultDto recieveCouponResult = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
					if(!recieveCouponResult.getCode().equals(ResultDto.SUCCESS)) {
						logger.error("领取优惠券失败，暂设置为占用状态，"+JsonUtils.object2JSON(recieveCouponResult));
						upStatus = ActPrize.STATUS_OCCUPY;
					}
				}
				//更新奖品信息
				actPrizeBiz.updatePrize(userId, openId, customerId,prize.getId(),upStatus, "", null);
				System.out.println(JsonUtils.object2JSON(prize));
				//移除中奖奖品
				removePointPrize(prizeId);
			}
		}
		if(times > 3) {
			//用户扣减积分
			pointBiz.addPoint(userId, Long.valueOf(POINT_LOTTERY_USE), PointRecord.SOURCE_TURNTABLE, 
					PointRecord.FLOW_PLUS, "大转盘抽奖消耗", "");
		}
		
		Integer freeCount = FREE_LOTTERY_COUNT - times;
		freeCount = freeCount >= 0 ? freeCount : 0;
		hitDto.setFreeCount(freeCount);

		//添加抽奖记录
		Turntable record = new Turntable(); //抽奖记录
		record.setIsFree(isFree);
		Integer status = hitDto.getStatus();
		Integer isHit = status.equals(TurntableHitDto.STATUS_HIT)?1:0;
		record.setIsHit(isHit);
		Integer usePoint = isFree.equals(0)?POINT_LOTTERY_USE:0;
		record.setUsePoint(usePoint);
		record.setUserId(userId);
		record.setCreateTime(new Date());
		turntableDao.insert(record);
		//若中奖，添加中奖记录
		if(status.equals(TurntableHitDto.STATUS_HIT)) {
			//若为积分，账户增加积分
			if(hitDto.getType().equals(ActPrize.TYPE_POINT)) {
				pointBiz.addPoint(userId, hitDto.getWorth().longValue(), PointRecord.SOURCE_TURNTABLE_51, 
						PointRecord.FLOW_ADD, "51大转盘抽奖获取", "");
			}
		}
		
		hitDto.setUsePoint(usePoint);
		return new StatusObjDto<TurntableHitDto>(true,hitDto,StatusDto.SUCCESS,"");
	}


	private void initPrizes() {
		boolean hasInit = ActivityRedis.ACT_TURNTABLE51_PRIZES.exists(LOTTERY_POINT_KEY);
		if(hasInit) return;
		logger.info("初始化大转盘奖品.............................");
		//免费抽奖第三次抽奖中奖奖品初始化
//		Double freeHitCountD = FREE_HIT_RATIO*100;
//		Integer freeHitCount = freeHitCountD.intValue();
//		for(Integer i = 0;i < 100;i++) {
//			String isHit = i < freeHitCount?"1":"0";
//			//使用首位标识是否中奖
//			String value = isHit + "-" + i;
//			ActivityRedis.ACT_TURNTABLE51_PRIZES.sadd(LOTTERY_FREE_KEY, value);
//		}
		
		//积分抽奖第三次抽奖中奖奖品初始化
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(ACTIVITY_CODE);
		if(prizes == null) {
			logger.info("大转盘奖品未配置.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("奖品未配置");
		}
	
		//添加Level2兑换码奖品
		for(int i = 0;i < prizes.size();i++) {
			ActivityRedis.ACT_TURNTABLE51_PRIZES.sadd(LOTTERY_POINT_KEY, prizes.get(i));
		}

		logger.info("初始化大转盘奖品完毕.............................,total"+prizes.size());
	}
	
	private boolean freeLotteryPrize() {
		String point50Prize = (String) ActivityRedis.ACT_TURNTABLE51_PRIZES.sRandomMember(LOTTERY_FREE_KEY);
		String[] prizes = point50Prize.split("-");
		
		return prizes[0].equals("1");
	}

	private Long pointLotteryPrize() {
		Long prize = (Long) ActivityRedis.ACT_TURNTABLE51_PRIZES.sRandomMember(LOTTERY_POINT_KEY);
		return prize;
	}
	
	private void removePointPrize(Long prize) {
		ActivityRedis.ACT_TURNTABLE51_PRIZES.sremove(LOTTERY_POINT_KEY, prize);
	}
	
	/**
	 * 增加抽奖次数
	 * @param userId
	 * @return 当前次数
	 */
	private Integer addLotteryTimes(String userId) {
		String date = DateUtils.formate(new Date());
		String timesKey = userId + date;
		Integer times = (Integer)ActivityRedis.ACT_TURNTABLE51_LOTTERY_TIMES.get(timesKey);
		Integer currentTimes = 1;
		if(times == null) {
			ActivityRedis.ACT_TURNTABLE51_LOTTERY_TIMES.set(timesKey,currentTimes);
		}else {
			currentTimes = times + 1;
			ActivityRedis.ACT_TURNTABLE51_LOTTERY_TIMES.set(timesKey,currentTimes);
		}
		
		return currentTimes;
	}
	
	/**
	 * 检查抽奖时间间隔，间隔1.5秒之内为非法抽奖
	 * @param userId
	 * @param timestamp
	 */
	private void checkTime(String userId,Long timestamp) {
		Long lastTime = (Long) ActivityRedis.ACT_TURNTABLE51_LOTTERY_TIMESTAMP.get(userId);
		if(lastTime != null) {
			Long timespan = timestamp - lastTime;
			if(timespan <= 0) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("无效抽奖");
			}
			if(timespan < 1500) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("抽奖太快，待会再来");
			}
		}
		ActivityRedis.ACT_TURNTABLE51_LOTTERY_TIMESTAMP.set(userId, timestamp);
	}

}
