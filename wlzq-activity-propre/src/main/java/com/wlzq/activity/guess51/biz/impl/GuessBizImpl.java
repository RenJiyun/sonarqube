package com.wlzq.activity.guess51.biz.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.TradeDateBiz;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.guess51.biz.GuessBiz;
import com.wlzq.activity.guess51.biz.GuessResultBiz;
import com.wlzq.activity.guess51.dao.GuessDao;
import com.wlzq.activity.guess51.dao.GuessIndexDao;
import com.wlzq.activity.guess51.dao.GuessLoginRecordDao;
import com.wlzq.activity.guess51.dao.GuessUserDao;
import com.wlzq.activity.guess51.dao.GuessWinDao;
import com.wlzq.activity.guess51.dto.AchievementDto;
import com.wlzq.activity.guess51.dto.GuessDto;
import com.wlzq.activity.guess51.dto.GuessInfoDto;
import com.wlzq.activity.guess51.dto.GuessPrizeDto;
import com.wlzq.activity.guess51.dto.GuessStatusDto;
import com.wlzq.activity.guess51.dto.WinRankDto;
import com.wlzq.activity.guess51.model.Guess;
import com.wlzq.activity.guess51.model.GuessIndex;
import com.wlzq.activity.guess51.model.GuessLoginRecord;
import com.wlzq.activity.guess51.model.GuessNo;
import com.wlzq.activity.guess51.model.GuessUser;
import com.wlzq.activity.guess51.model.GuessWin;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.PointRecord;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.PointBiz;
import com.wlzq.service.base.sd.common.SDResult;
import com.wlzq.service.base.sd.common.SDServiceType;
import com.wlzq.service.base.sd.utils.SDUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

/**
 * 猜涨跌业务接口实现
 * @author louie
 *
 */
@Service
public class GuessBizImpl extends ActivityBaseBiz implements GuessBiz {
	private Logger logger = LoggerFactory.getLogger(GuessBizImpl.class);
	private static final String CONFIG_GUESS_NOS = "activity.guess51.guessno";
	/** 活动编码*/
	private static final String ACTIVITY_CODE="ACTIVITY.GUESS51";
	/** 首次登录获取积分*/
//	private static final Integer FIRST_POINT = 100;
	/** 每日登录获取积分*/
	private static final Integer LOGIN_POINT = 10;
	/** 竞猜最少使用积分*/
	private static final Integer GUESS_MIN_POINT = 10;
	/** 竞猜最高使用积分*/
	private static final Integer GUESS_MAX_POINT = 100;
	@Autowired
	private	GuessUserDao guessUserDao;
	@Autowired
	private	GuessDao guessDao;
	@Autowired
	private	GuessWinDao winDao;
	@Autowired
	private	GuessLoginRecordDao firstLoginDao;
	@Autowired
	private	GuessIndexDao indexDao;
	@Autowired
	private	PointBiz pointBiz;
	@Autowired
	private	TradeDateBiz tradeDateBiz;
	@Autowired
	private	GuessResultBiz guessResultBiz;
	@Autowired
	private	ActPrizeBiz prizeBiz;
	
	@Transactional
	public StatusObjDto<GuessInfoDto> overview(AccTokenUser user){
		if(ObjectUtils.isEmptyOrNull(user)){
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("用户信息为空");
		}
		String userId = user.getUserId();
		GuessInfoDto overview = new GuessInfoDto();
		GuessUser existUser = guessUserDao.findByUserId(userId);
		if(existUser == null) { //增加首次登录积分
			pointBiz.addPoint(userId, Long.valueOf(LOGIN_POINT), PointRecord.SOURCE_GUESS_51, PointRecord.FLOW_ADD,
					"51猜涨跌首次登录", null);
			
			//增加猜跌用户信息
			GuessUser guessUser = new GuessUser();
			guessUser.setUserId(userId);
			guessUser.setOpenid(user.getOpenid());
			guessUser.setMobile(user.getMobile());
			guessUser.setCreateTime(new Date());
			guessUserDao.insert(guessUser);
			
			addLoginInfo(userId);
			
			overview.setIsLoginFirst(GuessInfoDto.YES);
			overview.setIsTodayLoginFirst(GuessInfoDto.YES);
			overview.setFirstPoint(LOGIN_POINT);
			overview.setLoginPoint(LOGIN_POINT);
		}else {
			//增加每日登录积分
			overview.setIsLoginFirst(GuessInfoDto.NO);
			//查询当天是否前三次登录
			GuessLoginRecord login = new GuessLoginRecord();
			login.setUserId(userId);
			Date now = new Date();
			login.setCreateTimeFrom(DateUtils.getDayStart(now));
			login.setCreateTimeTo(DateUtils.getDayEnd(now));
			List<GuessLoginRecord> logins = firstLoginDao.findLogin(login);
			if(logins.size() < 3) {
				pointBiz.addPoint(userId, Long.valueOf(LOGIN_POINT), PointRecord.SOURCE_GUESS_51, PointRecord.FLOW_ADD,
						"51猜涨跌每日登录", null);
				
				//增加每日首次登录信息
				addLoginInfo(userId);
				
				overview.setIsTodayLoginFirst(GuessInfoDto.YES);
				overview.setLoginPoint(LOGIN_POINT);
			}else {
				overview.setIsTodayLoginFirst(GuessInfoDto.NO);
			}
		}
		
		Date guessDate = getGuessDate();
		String guessDateStr = DateUtils.formate(guessDate);
		Guess param = new Guess();
		param.setGuessDate(guessDateStr);
		param.setUserId(userId);
		List<Guess> guesses = guessDao.findList(param);
		Integer hasGuess = guesses.size() > 0?1:0;
		overview.setHasGuess(hasGuess);
		
		return new StatusObjDto<GuessInfoDto>(true,overview,StatusDto.SUCCESS,"");
	}
	
	public StatusObjDto<GuessStatusDto> betStatus(){
		GuessNo guessNo = getGuessNo();
		
		GuessStatusDto status = guessDao.findGuessStatus(guessNo.getGuessDate(),guessNo.getGuessNo());
		if(status == null) { //无人竞猜
			status = new GuessStatusDto();
			status.setDownPoint(0l);
			status.setUpPoint(0l);
		}

		GuessStatusDto countStatus = guessDao.findGuessCount(guessNo.getGuessDate(),guessNo.getGuessNo());
		if(countStatus == null) {//无人竞猜
			countStatus = new GuessStatusDto();
			status.setUpRatio(0);
			status.setDownRatio(0);
		}else {
			Integer upCount = countStatus.getUpCount() == null?0:countStatus.getUpCount();
			Integer downCount = countStatus.getDownCount() == null?0:countStatus.getDownCount();
			Double upRatioD = Double.valueOf(upCount)/(upCount + downCount)*100;
			Integer upRatio = upRatioD.intValue();
			Integer downRatio = 100 - upRatio;
			status.setUpRatio(upRatio);
			status.setDownRatio(downRatio);
		}
		
		Long downPoint = status.getDownPoint() == null?0:status.getDownPoint();
		Long upPoint = status.getUpPoint() == null?0:status.getUpPoint();
		status.setDownPoint(downPoint);
		status.setUpPoint(upPoint);
		//计算实时赔率,
		//X积分猜涨，Y积分猜跌，总积分为X+Y，此时
		//押涨100（假设赢了），则赢100（100+X+Y）/(X+100)
		//押跌100（假设赢了），则赢100*（100+X+Y）/(Y+100)
		Long total = status.getDownPoint()+status.getUpPoint();
		Double upWinPointD = 100.0*(100+total)/(status.getUpPoint()+100);
		Double downWinPointD = 100.0*(100+total)/(status.getDownPoint()+100);
		
		status.setUpWinPoint(upWinPointD.longValue());
		status.setDownWinPoint(downWinPointD.longValue());
		
		String currentDateStr = DateUtils.formate(new Date());
		Integer isNextTradeDate = guessNo.getGuessDate().equals(currentDateStr)?0:1;
		status.setIsNextTradeDate(isNextTradeDate);
		status.setGuessNo(guessNo.getGuessNo());
		
		return new StatusObjDto<GuessStatusDto>(true,status,StatusDto.SUCCESS,"");
	}

	public StatusObjDto<GuessDto>  guess(String userId,Integer direction,Integer point){
		StatusDto activityStatus = isValid(ACTIVITY_CODE);
		if(!activityStatus.isOk()) {
			return new StatusObjDto<GuessDto>(true,activityStatus.getCode(),activityStatus.getMsg());
		}
		
		if(ObjectUtils.isEmptyOrNull(userId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		if(ObjectUtils.isEmptyOrNull(direction)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("direction");
		}
		if(direction < 0 || direction > 1) {
			throw BizException.COMMON_PARAMS_IS_ILLICIT.format("direction");
		}
		if(ObjectUtils.isEmptyOrNull(point)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("point");
		}
		if(point < GUESS_MIN_POINT) {
			throw ActivityBizException.GUESS_MIN_POINT.format(GUESS_MIN_POINT);
		}
		if(point > GUESS_MAX_POINT) {
			throw ActivityBizException.GUESS_MAX_POINT.format(GUESS_MAX_POINT);
		}
		
		GuessDto guessDto = new GuessDto();
		
		GuessNo guessNo = getGuessNo();
		//查询押注日期场次是否有押注信息
		Guess param = new Guess();
		param.setGuessDate(guessNo.getGuessDate());
		param.setUserId(userId);
		param.setGuessNo(guessNo.getGuessNo());
		List<Guess> guesses = guessDao.findList(param);
		if(guesses.size() > 0) { //不能重复押注同个交易日同个场次
			guessDto.setStatus(CodeConstant.CODE_NO);
			String currentDateStr = DateUtils.formate(new Date());
			Integer nextIsNextTradeDate = guessNo.getGuessDate().equals(currentDateStr)?0:1;
			Integer nextGuessNo = guessNo.getGuessNo() + 1;
			nextIsNextTradeDate = nextGuessNo.equals(4)?1:nextIsNextTradeDate;
			nextGuessNo = nextGuessNo.equals(4)?1:nextGuessNo;
			guessDto.setNextIsNextTradeDate(nextIsNextTradeDate);
			guessDto.setNextGuessNo(nextGuessNo);
			return new StatusObjDto<GuessDto>(true,guessDto,StatusDto.SUCCESS,"");
		}
		
		//查询可使用积分
		StatusObjDto<Long> pointStatus = pointBiz.getPoint(userId,null);
		if(!pointStatus.isOk()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("获取积分信息异常");
		}
		Long totalPoint = pointStatus.getObj();
		if(totalPoint < Long.valueOf(point)) {
			throw ActivityBizException.GUESS_POINT_INSUFFICIENT; 
		}
		
		Guess guess = new Guess();
		guess.setUserId(userId);
		guess.setDirection(direction);
		guess.setUsePoint(point);
		guess.setGuessDate(guessNo.getGuessDate());
		guess.setGuessNo(guessNo.getGuessNo());
		guess.setCreateTime(new Date());
		guessDao.insert(guess);
		
		//减少积分
		pointBiz.addPoint(userId, Long.valueOf(point), PointRecord.SOURCE_GUESS_51, PointRecord.FLOW_PLUS, "51猜涨跌押注", null);
		
		guessDto.setStatus(CodeConstant.CODE_YES);
		
		return new StatusObjDto<GuessDto>(true,guessDto,StatusDto.SUCCESS,"");
	}

	public StatusObjDto<List<Guess>> guessRecord(String userId,Integer start,Integer end){
		if(ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		List<Guess> guesses = guessDao.findPage(userId,start,end);
		for(Guess guess:guesses) {
			Integer status = ObjectUtils.isEmptyOrNull(guess.getWinPoint())?CodeConstant.CODE_NO:CodeConstant.CODE_YES;
			guess.setStatus(status);
		}
		
		return new StatusObjDto<List<Guess>>(true,guesses,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<List<WinRankDto>> winRanking(Date date,Integer start,Integer end) {
		
		GuessWin win = new GuessWin();
		List<WinRankDto> ranking = winDao.rankingList(win,start,end);
		
		if(ranking == null) {
			ranking = new ArrayList<WinRankDto>();
		}
		
		return new StatusObjDto<List<WinRankDto>>(true,ranking,StatusDto.SUCCESS,"");
	}
	
	@Override
	public AchievementDto  achievement(AccTokenUser user) {
		if(ObjectUtils.isEmptyOrNull(user)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("user");
		}
		String userId = user.getUserId();
		GuessUser guessUser = guessUserDao.findByUserId(user.getUserId());
		if(guessUser == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("竞猜用户不存在");
		}
		
		AchievementDto achievement = new AchievementDto();
		
		//头像与手机获取
		achievement.setPortrait(user.getPortrait());		
		String mobileLike = user.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
		achievement.setMobile(mobileLike);
		String nickName = ObjectUtils.isEmptyOrNull(user.getNickName())?mobileLike:user.getNickName();
		achievement.setNickName(nickName);
		//积分获取
		Long point = null;
		StatusObjDto<Long> pointStatus = pointBiz.getPoint(userId,null);
		if(!pointStatus.isOk()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("获取积分信息异常");
		}else {
			point = pointStatus.getObj();
		}
		achievement.setPoint(point);
		Long guessCount = guessDao.guessCount(userId);
		guessCount = guessCount == null?0:guessCount;
		achievement.setTotal(guessCount);
		
		//连胜与排序获取
		GuessWin win = new GuessWin();
		win.setUserId(userId);
		WinRankDto ranking = winDao.ranking(userId);
		Long winCount = ranking != null?ranking.getWinCount():0;
		Long order =  ranking != null?ranking.getOrder():0;
		achievement.setOrder(order);
		achievement.setWinCount(winCount);
		
		//打败用户比例计算
		if(ranking != null) {
			Long rCount = winDao.rankingCount();
			Long beatCount = rCount - order;
			Double ratio = Double.valueOf(beatCount)/rCount*100;
			DecimalFormat  df  = new DecimalFormat("#.0");  
			String ratioText = ratio == 0?"0":df.format(ratio);
			achievement.setBeatRatio(ratioText);
		}else {
			achievement.setBeatRatio("0");
		}
		
		return achievement;
	}


	public StatusObjDto<List<GuessPrizeDto>> prizes(Integer start,Integer end){
		List<ActPrize> prizes = prizeBiz.findPrize(ACTIVITY_CODE, "", "", "", "",ActPrize.STATUS_SEND, null);
		List<GuessPrizeDto> returnPrizes = Lists.newArrayList(); 
		int count = 10;
		for(int i = 0;i < prizes.size();i++) {
			if(i >= count ) break;
			ActPrize prize = prizes.get(i);
			GuessPrizeDto prizeDto = new GuessPrizeDto();
			prizeDto.setPrizeName(prize.getName());
			String nickName = prize.getNickName();
			nickName = ObjectUtils.isNotEmptyOrNull(nickName)?nickName:prize.getMobile().substring(0, 3) + "****" + prize.getMobile().substring(7, prize.getMobile().length());
			prizeDto.setNickName(nickName);
			long winCount = prize.getType().equals(ActPrize.TYPE_REDEEM)?5:prize.getCode().equals("PRIZE.GUESS51.JDK.10")?7:10;
			prizeDto.setWinCount(winCount);
			returnPrizes.add(prizeDto);
		}
		return new  StatusObjDto<List<GuessPrizeDto>>(true,returnPrizes,StatusDto.SUCCESS,"");
	}
	
	@Override
	public void getOpenIndex() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("stock_list","SH:000001");
		params.put("field","12:9");//10:2 ，9:2
		params.put("path", "/json");
		SDResult result = SDUtils.doPost(SDServiceType.MARKET, "20000", null,params);
		if(result.isOk()) {
			List<Object> data = (List<Object>) result.getData().get("results");
			if(data.size() > 0) {
				List<Object> shindexes = (List<Object>) data.get(0);
				String today = DateUtils.formate(new Date());
				GuessIndex index = new GuessIndex();
				index.setIndexDate(today);
				index.setType(1);
				index.setGuessNo(1);
			    List<GuessIndex> indexes = indexDao.findList(index);
			    if(indexes.size() == 0) {
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	index.setCreateTime(new Date());
			    	indexDao.insert(index);
			    }else {
			    	index = indexes.get(0);
					index.setGuessNo(1);
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	indexDao.update(index);
			    }
			}
	    }
	}

	@Override
	public void getMorningCloseIndex() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("stock_list","SH:000001");
		params.put("field","12:2");//10:2 ，9:2
		params.put("path", "/json");
		SDResult result = SDUtils.doPost(SDServiceType.MARKET, "20000", null,params);
		if(result.isOk()) {
			List<Object> data = (List<Object>) result.getData().get("results");
			if(data.size() > 0) {
				List<Object> shindexes = (List<Object>) data.get(0);
				String today = DateUtils.formate(new Date());
				GuessIndex index = new GuessIndex();
				index.setIndexDate(today);
				index.setType(1);
				index.setGuessNo(2);
			    List<GuessIndex> indexes = indexDao.findList(index);
			    if(indexes.size() == 0) {
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	index.setCreateTime(new Date());
			    	indexDao.insert(index);
			    }else {
			    	index = indexes.get(0);
			    	index.setGuessNo(2);
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	indexDao.update(index);
			    }
			}
	    }
	}
	
	@Override
	public void getCloseIndex() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("stock_list","SH:000001");
		params.put("field","12:2");//10:2 ，9:2
		params.put("path", "/json");
		SDResult result = SDUtils.doPost(SDServiceType.MARKET, "20000", null,params);
		if(result.isOk()) {
			List<Object> data = (List<Object>) result.getData().get("results");
			if(data.size() > 0) {
				List<Object> shindexes = (List<Object>) data.get(0);
				String today = DateUtils.formate(new Date());
				GuessIndex index = new GuessIndex();
				index.setIndexDate(today);
				index.setType(1);
				index.setGuessNo(3);
			    List<GuessIndex> indexes = indexDao.findList(index);
			    if(indexes.size() == 0) {
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	index.setCreateTime(new Date());
			    	indexDao.insert(index);
			    }else {
			    	index = indexes.get(0);
					index.setGuessNo(3);
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	indexDao.update(index);
			    }
			}
	    }
	}
	
	@Override
	public void settle(String date,Integer guessNo) {
		if(ObjectUtils.isEmptyOrNull(guessNo)) {
			logger.error("guessNo参数不能为空");
			throw BizException.COMMON_PARAMS_NOT_NULL.format("guessNo");
		}
		
		Thread  settleT = new Thread(new Runnable() {
			@Override
			public void run() {
				String sdate = date==null? DateUtils.formate(new Date()):date;
				settleHandle(sdate,guessNo);
			}
		});
		settleT.start();
	}
	
	private void settleHandle(final String date,final Integer guessNo) {
		//获取指数信息
		try {
			if(guessNo.equals(1)) {
				getOpenIndex();
			}else if(guessNo.equals(2)) {
				getMorningCloseIndex();
			}else if(guessNo.equals(3)) {
				getCloseIndex();
			}
		}catch(Exception ex) {
			
		}
		
		logger.info(date  + "猜涨跌开始计算奖励........................"+guessNo);
		GuessStatusDto status = guessDao.findGuessStatus(date,guessNo);
		if(status == null) {
			logger.info(date+"今日无人竞猜.........................."+guessNo);
			return;
		}
		Long downPoint = status.getDownPoint() == null?0:status.getDownPoint();
		Long upPoint = status.getUpPoint() == null?0:status.getUpPoint();
		status.setDownPoint(downPoint);
		status.setUpPoint(upPoint);
		//计算实时赔率,
		//猜涨赔率=1+猜跌总积分/猜涨总积分
		//猜跌赔率=1+猜涨总积分/猜跌总积分
		Double upRatio = 1 + Double.valueOf(status.getDownPoint())/status.getUpPoint(); 
		Double downRatio = 1 + Double.valueOf(status.getUpPoint())/status.getDownPoint();
		GuessIndex param =  new GuessIndex();
		param.setType(GuessIndex.TYPE_SH);
		param.setIndexDate(date);
		param.setGuessNo(guessNo);
		GuessIndex index = indexDao.findIndex(param);
		if(index == null) {
			logger.error(date+"无今日指数信息.........................."+guessNo);
			return;
		}
		Integer resultDirection = index.getClose() > index.getOpen()?Guess.DIRECTION_UP:Guess.DIRECTION_DOWN;
		
		int pageSize = 20;
		int count = 0;
		while(true) {
			List<Guess> guesses = guessDao.findUnsettlement(date,guessNo,0,pageSize);
			for(Guess guess:guesses) {
				//处理竞猜结果
				guessResultBiz.handle(guess, resultDirection, upRatio, downRatio);
				count++;
			}
			if(guesses.size() < pageSize) {
				break;
			}
		}
		logger.error(date+"-"+guessNo+"猜涨跌开始计算奖励完成，共处理"+count+"条信息..........................");
	}
	
	/**
	 * 获取竞猜日期
	 * @return
	 */
	private Date getGuessDate() {
		Date guessTime = new Date();
		if(tradeDateBiz.isTradeDate(guessTime)) {
			Date todayGuessStart = DateUtils.getDayStart(guessTime);
			Date todayGuessEnd = DateUtils.addHour(todayGuessStart, 13);
			if(guessTime.getTime() >= todayGuessStart.getTime() && guessTime.getTime() <= todayGuessEnd.getTime()) {
				return guessTime;
			}else {
				return tradeDateBiz.getNextTradeDate(guessTime);
			}
		}else {
			return tradeDateBiz.getNextTradeDate(guessTime);
		}
	}

	private void addLoginInfo(String userId) {
		GuessLoginRecord login = new GuessLoginRecord();
		login.setUserId(userId);
		login.setCreateTime(new Date());
		firstLoginDao.insert(login);
	}

	private GuessNo getGuessNo(){
		Date guessTime = new Date();
		String date = DateUtils.formate(guessTime,"yyyy-MM-dd");
		
		GuessNo guessN = new GuessNo();
		Integer no = null; 
		String guessDate = null;
		
		List<String> guessNoConfig = AppConfigUtils.getList(CONFIG_GUESS_NOS, ";");
		for(String guessNo:guessNoConfig) {
			String[] noInfo = guessNo.split(",");
			if(noInfo.length != 3) continue;
			String time = noInfo[0];
			String[] timeScope = time.split("-");
			if(timeScope.length != 2) continue;
			String startTimeString = timeScope[0];
			String endTimeString = timeScope[1];
			guessDate = noInfo[2].equals("1")?DateUtils.formate(guessTime,"yyyy-MM-dd"):DateUtils.formate(tradeDateBiz.getNextTradeDate(guessTime),"yyyy-MM-dd");
			no = Integer.valueOf(noInfo[1]); 
			Date startTime = DateUtils.parseDate(date+" "+startTimeString, "yyyy-MM-dd HH:mm:ss");
			Date endTime = DateUtils.parseDate(date+" "+endTimeString, "yyyy-MM-dd HH:mm:ss");
			if(guessTime.getTime() >= startTime.getTime() && guessTime.getTime() <= endTime.getTime()) {
				break;
			}
			if(startTime.getTime() > endTime.getTime()) {//跨天情况,分成两个时间段处理
				Date startEndTime = DateUtils.getDayEnd(startTime);
				Date endStartTime = DateUtils.getDayStart(endTime);
				if(guessTime.getTime() >= startTime.getTime() && guessTime.getTime() <= startEndTime.getTime()) {//次日午盘竟猜
					guessDate = DateUtils.formate(tradeDateBiz.getNextTradeDate(guessTime),"yyyy-MM-dd");
					break;
				}else if(guessTime.getTime() >= endStartTime.getTime() && guessTime.getTime() <= endTime.getTime()) {//当日午盘竟猜
					guessDate = DateUtils.formate(guessTime,"yyyy-MM-dd");
					break;
				}
			}
		}
		
		guessN.setGuessNo(no);
		guessN.setGuessDate(guessDate);
		
		return guessN;
	}

}
