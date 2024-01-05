package com.wlzq.activity.checkin.biz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.checkin.biz.CheckInBiz;
import com.wlzq.activity.checkin.dao.CheckInDao;
import com.wlzq.activity.checkin.dao.CheckInOpportunityDao;
import com.wlzq.activity.checkin.dao.CheckInPrizeDao;
import com.wlzq.activity.checkin.dao.CheckInStatusDao;
import com.wlzq.activity.checkin.dto.CheckInDto;
import com.wlzq.activity.checkin.dto.CheckInPrizeDto;
import com.wlzq.activity.checkin.model.CheckIn;
import com.wlzq.activity.checkin.model.CheckInOpportunity;
import com.wlzq.activity.checkin.model.CheckInPrize;
import com.wlzq.activity.checkin.model.CheckInStatistic;
import com.wlzq.activity.checkin.model.CheckInStatus;
import com.wlzq.activity.redeem.biz.RedeemBiz;
import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;

/**
 * 签到活动业务接口实现
 * @author louie
 *
 */
@Service
public class CheckInBizImpl extends ActivityBaseBiz implements CheckInBiz {
	/** 签到活动总人数基数 */
	private static final Integer COUNT_BASE = 1000;
	/** 签到活动编码 */
	private static final String ACTIVITY_CODE = "ACTIVITY.CHECKIN";
	/** 3个月Level2兑换码奖品类型编码 */
	private static final String CHECKIN_PRIZE_REDEEM_TYPE_CODE = "REDEEM.CHECKIN.PRIZE";
	/** 1个月Level2兑换码奖品类型编码 */
	private static final String CHECKIN_PRIZE_REDEEM_TYPE_CODE_1M = "REDEEM.CHECKIN.PRIZE1M";
	/** 获奖的连续签到次数 */
	private static final Integer TIMES_FOR_PRIZE = 5;
	
    @Autowired
    private  CheckInDao checkInDao;

    @Autowired
    private  CheckInPrizeDao checkInPrizeDao;

    @Autowired
    private  CheckInStatusDao checkInStatusDao;

    @Autowired
    private  CheckInOpportunityDao checkInOpportunityDao;

    @Autowired
    private  RedeemBiz redeemBiz;
    
	@Override
	public StatusObjDto<CheckInDto> status(String openid) {
		if(ObjectUtils.isEmptyOrNull(openid)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openid");
		}
		
		String redisKey = openid;
		Date currentTime = new Date();
		Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(currentTime);
		Date l6dayFirstDate = DateUtils.getDayStart(DateUtils.addDay(firstDayOfMonth, -6));
		CheckInStatus checkInStatus = checkInStatusDao.findByOpenId(openid);
		Integer continuousCount = checkInStatus == null || checkInStatus.getContinuousCount() == null?
				0:checkInStatus.getContinuousCount().intValue();
		
		Date dayEnd = DateUtils.getLastDayOfMonth(currentTime);
		CheckInDto checkInDto = (CheckInDto) ActivityRedis.ACT_CHECKIN_STATUS.get(redisKey);
		if(checkInDto != null) { //缓存有，直接返回
			//累计人数设置
			Long checkInCount = checkInStatusDao.checkInCount();
			checkInDto.setCount(COUNT_BASE+checkInCount.intValue());
			String month = DateUtils.formate(new Date(),"yyyyMM");
			if(!month.equals(checkInDto.getCurrentMonth())) {
				 //更新日历与签到记录
				setCalendar(checkInDto);
				setCheckInRecord(openid,checkInDto);
				
				//还有多少次连续签到获奖
			    checkInDto.setToPrizeCount(TIMES_FOR_PRIZE - continuousCount);
				
				ActivityRedis.ACT_CHECKIN_STATUS.set(redisKey,checkInDto);
			}
			//本月是否已获奖设置
			List<CheckInPrize> prizes = checkInPrizeDao.findPrize(openid, firstDayOfMonth, dayEnd);
			Integer hasPrize = prizes.size() > 0?1:0;
			checkInDto.setHasPrize(hasPrize);
		    checkInDto.setCurrentTime(new Date());
			return new StatusObjDto<CheckInDto>(true,checkInDto,0,"");
		}
		
		checkInDto = new CheckInDto();
		//连续签到次数设置
		checkInDto.setContinuousCount(continuousCount);
		//累计人数设置
		Long checkInCount = checkInStatusDao.checkInCount();
		checkInDto.setCount(COUNT_BASE+checkInCount.intValue());
		//补签机会数设置
		List<CheckInOpportunity> opportunites = checkInOpportunityDao.findOpportunities(openid);
		checkInDto.setOpportunites(opportunites.size());
		
		//日历设置
		setCalendar(checkInDto);
		
		//签到记录设置
		List<CheckIn> checkInList = checkInDao.findByOpenId(openid,l6dayFirstDate,dayEnd);
		checkInDto.setCheckInList(checkInList);
		
		//本月是否已获奖设置
		List<CheckInPrize> prizes = checkInPrizeDao.findPrize(openid, firstDayOfMonth, dayEnd);
		Integer hasPrize = prizes.size() > 0?1:0;
		checkInDto.setHasPrize(hasPrize);
		//还有多少次连续签到获奖
		if(hasPrize.equals(1)) {
			 checkInDto.setToPrizeCount(-1);
		}else {
			 checkInDto.setToPrizeCount(TIMES_FOR_PRIZE - continuousCount);
		}
		
		//设置缓存
		ActivityRedis.ACT_CHECKIN_STATUS.set(redisKey,checkInDto);
		
	    return new StatusObjDto<CheckInDto>(true,checkInDto,0,"");
	}
	
	@Transactional
	@Override
	public StatusObjDto<CheckInPrizeDto> checkIn(String userId,String openid, Integer type,String fillDate) {
		//活动有效性检查
		StatusDto validActivity = isValid(ACTIVITY_CODE);
		if(!validActivity.isOk()) {
			return new StatusObjDto<CheckInPrizeDto>(false,validActivity.getCode(),validActivity.getMsg());
		}
		//参数检查
		if(ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		if(ObjectUtils.isEmptyOrNull(openid)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openid");
		}
		if(ObjectUtils.isEmptyOrNull(type)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("type");
		}
		Date checkInTime = null; 
		if(type.equals(2)) {
			if(ObjectUtils.isEmptyOrNull(fillDate)) {
				throw  BizException.COMMON_PARAMS_NOT_NULL.format("fillDate");
			}
			checkInTime = DateUtils.parseDate(fillDate, "yyyy-MM-dd");
			if(checkInTime == null) {
				throw  BizException.COMMON_PARAMS_IS_ILLICIT.format("fillDate");
			}
			Date todayEndTime = DateUtils.getDayEnd(new Date());
			if(checkInTime.getTime() > todayEndTime.getTime()) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("只能补签今天前的数据");
			}
		}
		List<CheckInOpportunity> opportunites = null;
		//查询签到当天是否已签到
		checkInTime = checkInTime == null?new Date():checkInTime;
		Date[] timeStartEnd = DateUtils.getDayStartAndEndDates(checkInTime);
		if(type.equals(2)) { //查询当月是否有补签机会
			opportunites = checkInOpportunityDao.findOpportunities(openid); 
			if(opportunites.size() == 0) {
				throw ActivityBizException.CHECK_IN_NO_OPPORTUNITY;
			}
		}
		List<CheckIn> todayCheckIns = checkInDao.findByTime(openid, timeStartEnd[0], timeStartEnd[1]);
		if(todayCheckIns.size() > 0) {
			throw ActivityBizException.ALREADY_CHECK_IN;
		}
		
		CheckIn checkIn = new CheckIn();
		checkIn.setUserId(userId);
		checkIn.setOpenid(openid);
		checkIn.setType(type);
		checkIn.setCheckInTime(checkInTime);
		checkIn.setCreateTime(new Date());
		checkInDao.insert(checkIn);
		
		//更新签到情况总次数与连续签到次数
		CheckInStatus checkInStatus = checkInStatusDao.findByOpenId(openid);
		if(checkInStatus == null) { //若不存在，创建签到情况记录
			checkInStatus = new CheckInStatus();
			checkInStatus.setContinuousCount(0l);
			checkInStatus.setTotalCount(0l);
			checkInStatus.setUserId(userId);
			checkInStatus.setOpenid(openid);
			checkInStatus.setCreateTime(new Date());
			checkInStatus.setContinuousCountDate(DateUtils.addDay(new Date(),-1));
			checkInStatusDao.insert(checkInStatus);
		}
		Integer continuousCount = 1;
		Date continuousStartTime ;
		Date continuousEndTime ;
		String checkInTimeStr = DateUtils.formate(checkInTime);
		if(type.equals(1)) { //正常签到计算连续签到天数
			Date continuosCountDate = checkInStatus.getContinuousCountDate();
			Date conCountDAdd = DateUtils.addDay(continuosCountDate, 1);
			String comCheckInTimeStr = DateUtils.formate(conCountDAdd);
			if(checkInTimeStr.equals(comCheckInTimeStr)) { //连续次数加1
				checkInStatus.setContinuousCount(checkInStatus.getContinuousCount()+1);
			}else {//连续次数置为1
				checkInStatus.setContinuousCount(1l);
			}
			checkInStatus.setContinuousCountDate(checkInTime);
			continuousCount = checkInStatus.getContinuousCount().intValue();
			
			//如果连续打卡超过TIMES_FOR_PRIZE=5天，则中奖签到开始时间取5天前即可
			if(continuousCount > TIMES_FOR_PRIZE) {
				continuousStartTime = DateUtils.addDay(checkInTime, -(TIMES_FOR_PRIZE - 1));
			}else {
				continuousStartTime = DateUtils.addDay(checkInTime, -(continuousCount - 1));
			}
			
			continuousEndTime = checkInTime;
		}else { //补签时计算连续签到天数
			CheckInStatistic statistic = getContinuousForFill(openid,checkInTime);
			continuousCount = statistic.getContinuousCount(); 
			continuousStartTime = DateUtils.parseDate(statistic.getStartTime(), "yyyy-MM-dd");
			continuousEndTime =  DateUtils.parseDate(statistic.getEndTime(), "yyyy-MM-dd");
			//若统计结束日期与当前日期相同，更新连续签到数
			String today = DateUtils.formate(new Date());
			if(statistic.getEndTime().equals(today)) {
				checkInStatus.setContinuousCount(continuousCount.longValue());
				checkInStatus.setContinuousCountDate(continuousEndTime);
			}
		}
		checkInStatus.setTotalCount(checkInStatus.getTotalCount()+1);

		CheckInPrizeDto prize = getCheckInPrize(openid, userId, continuousCount, continuousStartTime, continuousEndTime);

		checkInStatus.setUpdateTime(new Date());
		checkInStatusDao.update(checkInStatus);
		
		if(type.equals(2) && opportunites.size() > 0) { //若为补签，更新补签卡为已使用
			CheckInOpportunity opport = opportunites.get(0);
			opport.setStatus(1);
			opport.setUseTime(checkIn.getCreateTime());
			checkInOpportunityDao.update(opport);
		}
		
		//更新缓存
		String redisKey = openid;
		CheckInDto checkInDto = (CheckInDto) ActivityRedis.ACT_CHECKIN_STATUS.get(redisKey);
		if(checkInDto != null) { // 更新缓存连续签到数与签到记录
			checkInDto.setContinuousCount(checkInStatus.getContinuousCount().intValue());
			List<CheckIn> checkInList = checkInDto.getCheckInList();
			checkInList.add(checkIn);
			if(type.equals(2)) { //若为补签，更新缓存补签卡数量 
				checkInDto.setOpportunites(checkInDto.getOpportunites() - 1);
			}
			//是否获奖与还有多少次签到获奖更新
			Date[] startEndTime = DateUtils.getMonthStartAndEndDates(new Date());
			List<CheckInPrize> prizes = checkInPrizeDao.findPrize(openid, startEndTime[0], startEndTime[1]);
			Integer hasPrize = prizes.size() > 0?1:0;
			checkInDto.setHasPrize(hasPrize);
			checkInDto.setToPrizeCount(TIMES_FOR_PRIZE - checkInStatus.getContinuousCount().intValue());
			
			ActivityRedis.ACT_CHECKIN_STATUS.set(redisKey,checkInDto);
		}
		
		return new StatusObjDto<CheckInPrizeDto>(true,prize,0,"");
	}

	@Override
	public StatusObjDto<Integer> getOpportunity(String userId, String openid) {
		//参数检查
		if(ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		if(ObjectUtils.isEmptyOrNull(openid)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openid");
		}
		
		Date[] startEndTime = DateUtils.getMonthStartAndEndDates(new Date());
		List<CheckInOpportunity> opportunities = checkInOpportunityDao.findByTime(openid,startEndTime[0],startEndTime[1],null);
		if(opportunities.size() > 0) {
			throw ActivityBizException.CHECK_IN_OPPORTUNITY_ALREADY_RECIEVE;
		}
		
		CheckInOpportunity opportunity = new CheckInOpportunity();
		opportunity.setUserId(userId);
		opportunity.setOpenid(openid);
		opportunity.setStatus(0);
		opportunity.setCreateTime(new Date());
		checkInOpportunityDao.insert(opportunity);
		
		String redisKey = openid;
		CheckInDto checkInDto = (CheckInDto) ActivityRedis.ACT_CHECKIN_STATUS.get(redisKey);
		if(checkInDto != null) { // 设置缓存补签机会数
			checkInDto.setOpportunites(checkInDto.getOpportunites()+1);
			 ActivityRedis.ACT_CHECKIN_STATUS.set(redisKey,checkInDto);
		}
		
		//查询补签卡总次数
		List<CheckInOpportunity> oppor = checkInOpportunityDao.findOpportunities(openid);
		
		Integer total = oppor.size();
		return new StatusObjDto<Integer>(true,total,0,"");
	}

	@Override
	public StatusObjDto<List<CheckInPrizeDto>> getPrize(String openid) {
		//参数检查
		if(ObjectUtils.isEmptyOrNull(openid)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openid");
		}
		CheckInPrize params = new CheckInPrize();
		params.setOpenid(openid);
		List<CheckInPrize> prizes = checkInPrizeDao.findList(params);
		List<CheckInPrizeDto> returnPrizes = new ArrayList<CheckInPrizeDto>();
		for(int i = 0;i < prizes.size();i++) {
			CheckInPrize prize = prizes.get(i);
			CheckInPrizeDto rPrize = new CheckInPrizeDto();
			rPrize.setDescription(prize.getRemark());
			rPrize.setRedeem(prize.getPrize());
			rPrize.setStatus(1);
			//设置兑换码有效期
			StatusObjDto<Redeem> redeemStatus = redeemBiz.findRedeemByCode(prize.getPrize());
			if(redeemStatus.isOk()) {
				Redeem redeem = redeemStatus.getObj();
				Integer timeType = redeem.getValidityType();
				if(timeType.equals(1)) {//时间范围
					rPrize.setExpireDate(redeem.getValidityDateTo());
				}else if(timeType.equals(2)) { //有效天数
					Date deadlineDay = DateUtils.addDay(redeem.getOutTime(), redeem.getValidityDay());
					rPrize.setExpireDate(DateUtils.getDayEnd(deadlineDay));
				}
			}
			
			returnPrizes.add(rPrize);
		}
		
		return  new StatusObjDto<List<CheckInPrizeDto>>(true,returnPrizes,0,"");
	}
	
	@Override
	public StatusObjDto<Integer> hasCheckIn(String openid,Date checkInDate) {
		if(ObjectUtils.isEmptyOrNull(openid)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openid");
		}
		checkInDate = ObjectUtils.isEmptyOrNull(checkInDate)?new Date():checkInDate;
		Date[] timeRange = DateUtils.getDayStartAndEndDates(checkInDate);
		List<CheckIn> todayCheckIns = checkInDao.findByTime(openid, timeRange[0], timeRange[1]);
		
		Integer checkStatus = todayCheckIns.size() > 0?1:0;
		return new StatusObjDto<Integer>(true,checkStatus,0,"");
	}

	/**
	 * 获取可用的兑换码
	 * @param typeCode 兑换码类型编码
	 * @return
	 */
    private Redeem getRedeem(String typeCode) {
    	StatusObjDto<Redeem> result = redeemBiz.findAvailable(typeCode);
    	if(!result.isOk()) {
    		throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
    	}
    	return result.getObj();
    }

    /**
     * 设置签到日历
     * @param checkInDto
     */
    private void setCalendar(CheckInDto checkInDto) {
    	Date currentTime = new Date();
		Integer curMonthDays = DateUtils.getDaysOfMonth();
		Integer calendarDays = curMonthDays + 6;
		checkInDto.setCurrentTime(currentTime);
		checkInDto.setDays(calendarDays);
		Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(currentTime);
		
		//日历
		List<Date> calendar = new ArrayList<Date>();
		Date l6daysFirst = DateUtils.addDay(firstDayOfMonth, -6);
		
		for(int i = 0;i <  6; i++) {
			Date date = DateUtils.addDay(l6daysFirst, i);
			calendar.add(date);
		}
		for(int i = 0 ;i < curMonthDays;i++) {
			Date date = DateUtils.addDay(firstDayOfMonth, i);
			calendar.add(date);
		}
		checkInDto.setFirstDayOfWeek(DateUtils.getIntWeekOfDate(l6daysFirst));
		checkInDto.setCalendar(calendar);
		String month = DateUtils.formate(new Date(),"yyyyMM");
		checkInDto.setCurrentMonth(month);
    }
    
    /**
     * 设置签到记录
     * @param openid
     * @param checkInDto
     */
    private void setCheckInRecord(String openid,CheckInDto checkInDto) {
    	Date currentTime = new Date();
		Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(currentTime);
		Date l6dayFirstDate = DateUtils.getDayStart(DateUtils.addDay(firstDayOfMonth, -6));
		Date dayEnd = DateUtils.getDayEnd(currentTime);
		List<CheckIn> checkInList = checkInDao.findByOpenId(openid,l6dayFirstDate,dayEnd);
		checkInDto.setCheckInList(checkInList);
    }
    
    /**
     * 查询当月补签卡
     * @param openid
     * @param date
     * @param status
     * @return
     */
    private List<CheckInOpportunity>  getCurrentMonthOpportunities(String openid,Date date,Integer status) {
    	date = date == null?new Date():date;
		Date[] startEndTime = DateUtils.getMonthStartAndEndDates(date);
		List<CheckInOpportunity> opportunities = checkInOpportunityDao.findByTime(openid,startEndTime[0],startEndTime[1],status);
		
		return opportunities;
    }
    
    /**
     * 获取补签所在连续签到情况 
     * @param openid
     * @param checkInDate
     * @return
     */
    private CheckInStatistic getContinuousForFill(String openid,Date checkInTime) {
    	Date currentTime = new Date();
		Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(currentTime);
		Date l6daysFirst = DateUtils.addDay(firstDayOfMonth, -6);
		String dayStart = DateUtils.formate(l6daysFirst,"yyyy-MM-dd");
		CheckInStatistic statistics = checkInDao.findContinuousCount(openid,dayStart,checkInTime);
		return statistics;
    }
    
    /**
     * 添加奖品
     * @param openid
     * @param userId
     * @param continuousStartTime
     * @return
     */
    private CheckInPrize addPrize(String openid,String userId,Date continuousStartTime) {
    	Date endTime = DateUtils.addDay(continuousStartTime, TIMES_FOR_PRIZE - 1);
		Redeem redeem = null;
		String remark = "Level-2增强行情产品使用权3个月（价值24元）";
		redeem = getRedeem(CHECKIN_PRIZE_REDEEM_TYPE_CODE_1M);
		remark = "Level-2增强行情产品使用权1个月";
		CheckInPrize cPrize = new CheckInPrize();
		cPrize.setOpenid(openid);
		cPrize.setUserId(userId);
		cPrize.setPrize(redeem.getCode());
		cPrize.setPrizeType(1);
		cPrize.setStartTime(continuousStartTime);
		cPrize.setEndTime(endTime);
		cPrize.setCreateTime(new Date());
		cPrize.setRemark(remark);
		checkInPrizeDao.insert(cPrize);
		
		Integer timeType = redeem.getValidityType();
		if(timeType.equals(1)) {//时间范围
			cPrize.setExpireDate(redeem.getValidityDateTo());
		}else if(timeType.equals(2)) { //有效天数
			Date deadlineDay = DateUtils.addDay(redeem.getOutTime(), redeem.getValidityDay());
			cPrize.setExpireDate(DateUtils.getDayEnd(deadlineDay));
		}
		return cPrize;
    }
    
    /**
     * 获取奖品逻辑
     * @param openid
     * @param userId
     * @param continuousCount
     * @param continuousStartTime
     * @param continuousEndTime
     * @return
     */
    private CheckInPrizeDto getCheckInPrize(String openid,String userId,Integer continuousCount,Date continuousStartTime,Date continuousEndTime) {
    	CheckInPrizeDto prize = null;
    	//若连续次数满足条件，设置奖品
		if(continuousCount >= TIMES_FOR_PRIZE) {
			String startMonth = DateUtils.formate(continuousStartTime,"yyyyMM");
			String endMonth = DateUtils.formate(continuousEndTime,"yyyyMM");
			if(startMonth.equals(endMonth)) {//连续签到不跨月
				//查询连续签到TIMES_FOR_PRIZE最后一天所在月奖品
				//Date endTime = DateUtils.addDay(continuousStartTime, 6);
				Date[] startEnd = DateUtils.getMonthStartAndEndDates(continuousStartTime);
				List<CheckInPrize> prizes = checkInPrizeDao.findPrize(openid, startEnd[0], startEnd[1]);
				if(prizes.size() == 0) { //若所在月未获奖，设置奖品
					CheckInPrize cPrize = addPrize(openid,userId,continuousStartTime);
					prize = getPrizeDto(cPrize);
				}
			}else {//连续签到跨月
				Date[] startEndLastMonth = DateUtils.getMonthStartAndEndDates(continuousStartTime);
				Date[] startEndThisMonth = DateUtils.getMonthStartAndEndDates(continuousEndTime);
				List<CheckInPrize> prizesLastMonth = checkInPrizeDao.findPrize(openid, startEndLastMonth[0], startEndLastMonth[1]);
				List<CheckInPrize> prizesThisMonth = checkInPrizeDao.findPrize(openid, startEndThisMonth[0], startEndThisMonth[1]);
				int prizesCountL = prizesLastMonth.size();
				int prizesCountT = prizesThisMonth.size();
				
				if(prizesCountL == 0 && prizesCountT == 0) {//两月都没获奖
					CheckInPrize cPrize = addPrize(openid,userId,continuousStartTime);
					prize = getPrizeDto(cPrize);
				}else if(prizesCountL > 0 && prizesCountT == 0){ //第一月获奖，第二月没获奖，奖品计算到第二月
					//上月获奖结束时间
					Date prizeEndTimeL = prizesLastMonth.size() > 0? prizesLastMonth.get(0).getEndTime():startEndLastMonth[0];
					if(continuousStartTime.getTime() > prizeEndTimeL.getTime()) { //连续签到开始时间大于获奖结束时间
						CheckInPrize cPrize = addPrize(openid,userId,continuousStartTime);
						prize = getPrizeDto(cPrize);
					}else {//连续签到开始时间小于获奖结束时间
						int prizeContinuousDays = DateUtils.daysBetween(prizeEndTimeL, continuousEndTime);
						if(prizeContinuousDays >= TIMES_FOR_PRIZE) { //连续签到天数大于等于7，获奖
							CheckInPrize cPrize = addPrize(openid,userId,DateUtils.addDay(prizeEndTimeL, 1));
							prize = getPrizeDto(cPrize);
						}
					}
				}else if(prizesCountL == 0 && prizesCountT > 0){ //第一月没获奖，第二月获奖，奖品计算到第一月
					//计算上月连续签到天数
					int prizeContinuousDays = DateUtils.daysBetween(continuousStartTime, DateUtils.getLastDayOfMonth(continuousStartTime));
					if(prizeContinuousDays >= TIMES_FOR_PRIZE) { //连续签到天数大于等于7，获奖
						CheckInPrize cPrize = addPrize(openid,userId,DateUtils.addDay(continuousStartTime, 1));
						prize = getPrizeDto(cPrize);
					}
				}
			}
		}
		if(prize == null) {
			prize = new  CheckInPrizeDto();
			prize.setStatus(0);
		}
		return prize;
    }
    
    /**
     * 设置奖品Dto
     * @param cPrize
     * @return
     */
    private CheckInPrizeDto getPrizeDto(CheckInPrize cPrize) {
    	CheckInPrizeDto prize = new CheckInPrizeDto();
    	prize.setDescription(cPrize.getRemark());
		prize.setRedeem(cPrize.getPrize());
		prize.setStatus(1);
		prize.setExpireDate(cPrize.getExpireDate());
		return prize;
    }
}
