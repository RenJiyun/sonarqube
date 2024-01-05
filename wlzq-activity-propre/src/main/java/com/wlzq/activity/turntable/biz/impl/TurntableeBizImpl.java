package com.wlzq.activity.turntable.biz.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.turntable.biz.TurntableeBiz;
import com.wlzq.activity.turntable.dao.TurntableeDao;
import com.wlzq.activity.turntable.dao.TurntableePrizeDao;
import com.wlzq.activity.turntable.dto.TurntableeHitDto;
import com.wlzq.activity.turntable.dto.TurntableePrizeDto;
import com.wlzq.activity.turntable.model.Turntablee;
import com.wlzq.activity.turntable.model.TurntableePrize;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.PointRecord;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.PointBiz;

/**
 * 大转盘业务接口实现
 * @author louie
 *
 */
@Service
public class TurntableeBizImpl extends ActivityBaseBiz implements TurntableeBiz {
	
	private Logger logger = LoggerFactory.getLogger(TurntableeBizImpl.class);
	/** 活动编码*/
	private static final String ACTIVITY_CODE="ACTIVITY.TURNTABLE";
	/** 免费抽奖奖品 redis key*/
	private static final String LOTTERY_FREE_KEY = "free";
	/** 消耗积分抽奖奖品 redis key*/
	private static final String LOTTERY_POINT_KEY = "point";
	/** 免费抽奖50积分中奖概率*/
	private static final Double FREE_HIT_RATIO =  0.2;
	/** 消耗积分Level2中奖概率*/
	private static final Double POINT_LEVEL2_RATIO = 0.2;
	/** 消耗积分50积分中奖概率*/
	private static final Double POINT_50_RATIO = 0.1;
	/** 消耗积分100积分中奖概率*/
	private static final Double POINT_100_RATIO = 0.1;
	/** 免费抽奖中奖积分*/
	private static final Integer FREE_HIT_POINT = 50;
	/** 每次抽奖消耗积分*/
	private static final Integer POINT_LOTTERY_USE = 100;
	/** 免费抽奖机会数*/
	private static final Integer FREE_LOTTERY_COUNT = 3;
	/** 总抽奖限制数*/
	private static final Integer LOTTERY_LIMIT_TIMES = 10;
	@Autowired
	private	TurntableeDao turntableDao;
	@Autowired
	private	TurntableePrizeDao prizeDao;
	@Autowired
	private	ActPrizeBiz actPrizeBiz;
	@Autowired
	private	PointBiz pointBiz;
   
	@Transactional
	public StatusObjDto<TurntableeHitDto> turn(String userId,Long timestamp){
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
			return new StatusObjDto<TurntableeHitDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		//检查时间间隔是否有效
		checkTime(userId,timestamp);
		
		Integer times = addLotteryTimes(userId);
		
		//初始化奖品
		initPrizes();
		
		TurntableeHitDto hitDto = new TurntableeHitDto();
		
		Turntablee params = new Turntablee();
		params.setUserId(userId);
		Date now = new Date();
		params.setCreateTimeFrom(DateUtils.getDayStart(now));
		params.setCreateTimeTo(now);

		Integer isFree = 1;
		TurntableePrize turnPrize = new TurntableePrize();
		if(times < FREE_LOTTERY_COUNT) {
			hitDto.setStatus(TurntableeHitDto.STATUS_NOT_HIT);
		}else if(times == FREE_LOTTERY_COUNT) {
			if(freeLotteryPrize()) {
				hitDto.setStatus(TurntableeHitDto.STATUS_HIT);
				hitDto.setType(TurntableeHitDto.TYPE_POINT_50);
				hitDto.setWorth(FREE_HIT_POINT);
				hitDto.setPrizeName(FREE_HIT_POINT+"积分");
				turnPrize.setType(ActPrize.TYPE_POINT);
				turnPrize.setWorth(Double.valueOf(FREE_HIT_POINT));
			}else {
				hitDto.setStatus(TurntableeHitDto.STATUS_NOT_HIT);
			}
		}else if(times > LOTTERY_LIMIT_TIMES){
			throw ActivityBizException.TURNTABLE_REACH_LOTTERY_TIMES.format(LOTTERY_LIMIT_TIMES);
		}else {//使用积分抽奖
			isFree = 0;
			//查询可使用积分
			StatusObjDto<Long> pointStatus = pointBiz.getPoint(userId,null);
			if(!pointStatus.isOk()) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("获取积分信息异常");
			}
			Long totalPoint = pointStatus.getObj();
			if(totalPoint < POINT_LOTTERY_USE) {
				hitDto.setStatus(TurntableeHitDto.STATUS_NO_POINT);
				hitDto.setUsePoint(0);
				return new StatusObjDto<TurntableeHitDto>(true,hitDto,StatusDto.SUCCESS,"积分不足");
				//throw ActivityBizException.GUESS_POINT_INSUFFICIENT;
			}
			String prizeStr = pointLotteryPrize();
			String[] prizeInfo = prizeStr.split("-");
			String isHit = prizeInfo[0];
			if(isHit.equals("0")) {
				hitDto.setStatus(TurntableeHitDto.STATUS_NOT_HIT);
			}else {
				Integer type = Integer.valueOf(prizeInfo[1]);
				hitDto.setType(type);
				hitDto.setStatus(TurntableeHitDto.STATUS_HIT);
				if(type.equals(ActPrize.TYPE_REDEEM)) { //兑换码
					Long prizeId = Long.valueOf(prizeInfo[2]);
					ActPrize prize = actPrizeBiz.findPrize(prizeId);
					if(prize == null) {
						logger.info("奖品不存在，ID："+prizeId+",userId:"+userId);
						return new StatusObjDto<TurntableeHitDto>(false,hitDto,StatusDto.FAIL_COMMON,"奖品不存在");
					}
					if(!prize.getStatus().equals(ActPrize.STATUS_NOT_SEND)) {
						logger.info("奖品已被使用，ID："+prizeId+",userId:"+userId+",usedId:"+prize.getUserId());
						return new StatusObjDto<TurntableeHitDto>(false,hitDto,StatusDto.FAIL_COMMON,"奖品不存在");
					}
					Integer worth = prize.getWorth() != null?prize.getWorth().intValue():null;
					hitDto.setWorth(worth);
					hitDto.setPrizeName(prize.getName());
					Integer prizeType = prize.getTime().equals(1)?TurntableeHitDto.TYPE_LEVEL2_1:
						prize.getTime().equals(3)?TurntableeHitDto.TYPE_LEVEL2_3:0;
					hitDto.setType(prizeType);
					//设置奖品信息
					BeanUtils.copyProperties(prize, turnPrize);
					turnPrize.setPrizeCode(prize.getCode());
					turnPrize.setPrizeId(prize.getId());
					//更新奖品信息
					actPrizeBiz.updatePrize(userId, prize.getId(), ActPrize.STATUS_SEND);
				}else {
					Double worth =  type.equals(2)?50.0:100.0;
					hitDto.setWorth(worth.intValue());
					Integer prizeType = type.equals(2)?TurntableeHitDto.TYPE_POINT_50:TurntableeHitDto.TYPE_POINT_100;
					hitDto.setType(prizeType);
					hitDto.setPrizeName(worth.intValue()+"积分");
					turnPrize.setWorth(worth);
					turnPrize.setType(ActPrize.TYPE_POINT);
				}
				//移除中奖奖品
				removePointPrize(prizeStr);
			}
			//用户扣减积分
			pointBiz.addPoint(userId, Long.valueOf(POINT_LOTTERY_USE), PointRecord.SOURCE_TURNTABLE, 
					PointRecord.FLOW_PLUS, "大转盘抽奖消耗", "");
		}
		
		Integer freeCount = FREE_LOTTERY_COUNT - times;
		freeCount = freeCount >= 0 ? freeCount : 0;
		hitDto.setFreeCount(freeCount);

		//添加抽奖记录
		Turntablee record = new Turntablee(); //抽奖记录
		record.setIsFree(isFree);
		Integer status = hitDto.getStatus();
		Integer isHit = status.equals(TurntableeHitDto.STATUS_HIT)?1:0;
		record.setIsHit(isHit);
		Integer usePoint = isFree.equals(0)?POINT_LOTTERY_USE:0;
		record.setUsePoint(usePoint);
		record.setUserId(userId);
		record.setCreateTime(new Date());
		turntableDao.insert(record);
		//若中奖，添加中奖记录
		if(status.equals(TurntableeHitDto.STATUS_HIT)) {
			turnPrize.setUserId(userId);
			turnPrize.setTurntableId(record.getId());
			turnPrize.setCreateTime(new Date());
			prizeDao.insert(turnPrize);
			//若为积分，账户增加积分
			if(turnPrize.getType().equals(ActPrize.TYPE_POINT)) {
				pointBiz.addPoint(userId, turnPrize.getWorth().longValue(), PointRecord.SOURCE_TURNTABLE, 
						PointRecord.FLOW_ADD, "大转盘抽奖获取", "");
			}
		}
		
		hitDto.setUsePoint(usePoint);
		return new StatusObjDto<TurntableeHitDto>(true,hitDto,StatusDto.SUCCESS,"");
	}

	@Override
	public StatusObjDto<List<UserPrizeDto>> userPrizes(String activity, String userId, Integer start, Integer end) {
		if(ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		
		List<UserPrizeDto> prizes = prizeDao.findUserPrizes(userId, activity, start, end);
		//积分情况处理
		for(UserPrizeDto prize:prizes) {
			prize.setSource(UserPrizeDto.SOURCE_TURNTABLE);
			if(prize.getType().equals(ActPrize.TYPE_POINT)) {
				prize.setStatus(ActPrize.STATUS_SEND);
			}
		}
		
		return new StatusObjDto<List<UserPrizeDto>>(true,prizes,StatusDto.SUCCESS,"");
	}

	@Override
	public StatusObjDto<List<TurntableePrizeDto>> prizes(String activity, Integer start, Integer end) {
		List<TurntableePrizeDto> prizes = prizeDao.findPrizes(activity, start, end);
		//手机与积分情况处理
		for(TurntableePrizeDto prize:prizes) {
			if(prize.getType().equals(ActPrize.TYPE_POINT)) {
				prize.setPrizeName(prize.getWorth()+"积分");
			}
		}
		return new  StatusObjDto<List<TurntableePrizeDto>>(true,prizes,StatusDto.SUCCESS,"");
	}

	@Override
	public StatusObjDto<Integer> findNotUsePrizeCount(String userId, String activity) {
		Integer count = prizeDao.findNotUseCount(userId, activity);
		return new StatusObjDto<Integer>(true,count,StatusDto.SUCCESS,"");
	}
	
	private void initPrizes() {
		boolean hasInit = ActivityRedis.ACT_TURNTABLE_PRIZES.exists(LOTTERY_POINT_KEY);
		if(hasInit) return;
		logger.info("初始化大转盘奖品.............................");
		//免费抽奖第三次抽奖中奖奖品初始化
		Double freeHitCountD = FREE_HIT_RATIO*100;
		Integer freeHitCount = freeHitCountD.intValue();
		for(Integer i = 0;i < 100;i++) {
			String isHit = i < freeHitCount?"1":"0";
			//使用首位标识是否中奖
			String value = isHit + "-" + i;
			ActivityRedis.ACT_TURNTABLE_PRIZES.sadd(LOTTERY_FREE_KEY, value);
		}
		
		//积分抽奖第三次抽奖中奖奖品初始化
		List<Long> level2Prizes = actPrizeBiz.findAvailablePrizes(ACTIVITY_CODE);
		if(level2Prizes == null) {
			logger.info("大转盘奖品未配置.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("奖品未配置");
		}
		if(level2Prizes.size() == 0) {
			logger.info("无可用的大转盘奖品.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("无可用奖品");
		}
		//50积分数量计算
		Double totalD = level2Prizes.size()/POINT_LEVEL2_RATIO;
		Long total = totalD.longValue();
		Double point50CountD = total*POINT_50_RATIO;
		Long point50Count = point50CountD.longValue();
		Double point100CountD = total*POINT_100_RATIO;
		Long point100Count = point100CountD.longValue();
		Long emptyCount = total - level2Prizes.size() - point50Count - point100Count;
		
		//奖品，第一位表示是否中奖（0：否，1：是），第二位表示中奖类型（1：Level2兑换码，2：积分50，3：积分100），
		//第三位表示奖品ID
		String prize = "1-1-";
		//添加Level2兑换码奖品
		for(int i = 0;i < level2Prizes.size();i++) {
			String level2Prize = prize + level2Prizes.get(i);
			ActivityRedis.ACT_TURNTABLE_PRIZES.sadd(LOTTERY_POINT_KEY, level2Prize);
		}
		//添加50积分奖品
		prize = "1-2-";
		for(int i = 0;i < point50Count;i++) {
			String point50Prize = prize + i;
			ActivityRedis.ACT_TURNTABLE_PRIZES.sadd(LOTTERY_POINT_KEY, point50Prize);
		}
		//添加100积分奖品
		prize = "1-3-";
		for(int i = 0;i < point100Count;i++) {
			String point100Prize = prize + i;
			ActivityRedis.ACT_TURNTABLE_PRIZES.sadd(LOTTERY_POINT_KEY, point100Prize);
		}
		//添加空奖品
		prize = "0-0-";
		for(int i = 0;i < emptyCount;i++) {
			String emptyPrize = prize + i;
			ActivityRedis.ACT_TURNTABLE_PRIZES.sadd(LOTTERY_POINT_KEY, emptyPrize);
		}

		logger.info("初始化大转盘奖品完毕.............................,total"+total+",level2:"+level2Prizes.size()+",point50Count:"+point50Count+",point100Count:"+point100Count+",emptyCount:"+emptyCount);
	}
	
	private boolean freeLotteryPrize() {
		String point50Prize = (String) ActivityRedis.ACT_TURNTABLE_PRIZES.sRandomMember(LOTTERY_FREE_KEY);
		String[] prizes = point50Prize.split("-");
		
		return prizes[0].equals("1");
	}

	private String pointLotteryPrize() {
		String prize = (String) ActivityRedis.ACT_TURNTABLE_PRIZES.sRandomMember(LOTTERY_POINT_KEY);
		return prize;
	}
	
	private void removePointPrize(String prize) {
		ActivityRedis.ACT_TURNTABLE_PRIZES.sremove(LOTTERY_POINT_KEY, prize);
	}
	
	/**
	 * 增加抽奖次数
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

	@Override
	public StatusObjDto<Integer> share(String userId) {
		String date = DateUtils.formate(new Date());
		String timesKey = userId + date;
		Integer sharetimes = (Integer)ActivityRedis.ACT_TURNTABLE_SHARE_TIMES.get(timesKey);
		Integer lotterytimes = (Integer)ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMES.get(timesKey);
		/**当日未抽奖**/
		if (lotterytimes == null) {
			lotterytimes = 0;
		}
		Integer freeCount = 0;
		/**当日未分享**/
		if(sharetimes == null) {
			if (lotterytimes == 0) {
				lotterytimes = lotterytimes - 1;
			} else {
				lotterytimes = 0;
			}
			ActivityRedis.ACT_TURNTABLE_SHARE_TIMES.set(timesKey,1);
			ActivityRedis.ACT_TURNTABLE_LOTTERY_TIMES.set(timesKey,lotterytimes);
			freeCount = 1;
		}
		return new StatusObjDto<>(true, freeCount, CodeConstant.CODE_YES, "");
	}

}
