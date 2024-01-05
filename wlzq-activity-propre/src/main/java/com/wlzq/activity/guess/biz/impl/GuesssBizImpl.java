package com.wlzq.activity.guess.biz.impl;

import java.text.DecimalFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.AcReceivePriceVO;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.guess.dto.*;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.TradeDateBiz;
import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.guess.biz.GuesssBiz;
import com.wlzq.activity.guess.biz.GuesssResultBiz;
import com.wlzq.activity.guess.dao.GuesssDao;
import com.wlzq.activity.guess.dao.GuesssFirstLoginDao;
import com.wlzq.activity.guess.dao.GuesssIndexDao;
import com.wlzq.activity.guess.dao.GuesssPrizeDao;
import com.wlzq.activity.guess.dao.GuesssUserDao;
import com.wlzq.activity.guess.dao.GuesssWinDao;
import com.wlzq.activity.guess.dao.GuesssWinDetailDao;
import com.wlzq.activity.guess.model.Guesss;
import com.wlzq.activity.guess.model.GuesssFirstLogin;
import com.wlzq.activity.guess.model.GuesssIndex;
import com.wlzq.activity.guess.model.GuesssPrize;
import com.wlzq.activity.guess.model.GuesssUser;
import com.wlzq.activity.guess.model.GuesssWin;
import com.wlzq.activity.guess.model.GuesssWinDetail;
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

/**
 * 猜涨跌业务接口实现
 * @author louie
 *
 */
@Service
public class GuesssBizImpl extends ActivityBaseBiz implements GuesssBiz {
	private Logger logger = LoggerFactory.getLogger(GuesssBizImpl.class);
	private static final HashMap<Integer, String> CODELIST_CACHE;
	/**
	 * 积分配置
	 */
	private static final String ACTIVITY_GUESS_POINT_CONFIG = "activity.guess.point.config";

	static {
		CODELIST_CACHE = Maps.newHashMap();

		/*上证指数*/
		CODELIST_CACHE.put(1, "16(1A0001)");
		/*沪深300指数*/
		CODELIST_CACHE.put(2, "16(1B0300)");
	}

	@Autowired
	private	GuesssUserDao guessUserDao;
	@Autowired
	private	GuesssDao guessDao;
	@Autowired
	private	GuesssWinDao winDao;
	@Autowired
	private	GuesssWinDetailDao winDetailDao;
	@Autowired
	private	GuesssFirstLoginDao firstLoginDao;
	@Autowired
	private	GuesssIndexDao indexDao;
	@Autowired
	private	GuesssPrizeDao prizeDao;
	@Autowired
	private	PointBiz pointBiz;
	@Autowired
	private	TradeDateBiz tradeDateBiz;
	@Autowired
	private	GuesssResultBiz guessResultBiz;
	@Autowired
	private CouponCommonReceiveBiz couponRecieveBiz;
	@Autowired
	private ActPrizeDao actPrizeDao;

	@Transactional
	public StatusObjDto<GuesssInfoDto> overview(AccTokenUser user, String activityCode){
		if(ObjectUtils.isEmptyOrNull(user)){
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("用户信息为空");
		}

		StatusDto isValidAct = isValid(activityCode);
		if(!isValidAct.isOk()) {
			throw ActivityBizException.ACTIVITY_NOT_EXIST;
		}

		GuessPointConfigDto guessPointConfig = JSON.parseObject(AppConfigUtils.get(ACTIVITY_GUESS_POINT_CONFIG), GuessPointConfigDto.class);
		if (guessPointConfig == null) {
			guessPointConfig = new GuessPointConfigDto();
		}

		String userId = user.getUserId();
		GuesssInfoDto overview = new GuesssInfoDto();
		GuesssUser existUser = guessUserDao.findByUserId(userId, activityCode);
		if(existUser == null) { //增加首次登录积分
			pointBiz.addPoint(userId, Long.valueOf(guessPointConfig.getFirstPoint()), PointRecord.SOURCE_GUESS, PointRecord.FLOW_ADD,
					"首次登录", activityCode);

			//增加猜跌用户信息
			GuesssUser guessUser = new GuesssUser();
			guessUser.setUserId(userId);
			guessUser.setOpenid(user.getOpenid());
			guessUser.setMobile(user.getMobile());
			guessUser.setCreateTime(new Date());
			guessUser.setActivityCode(activityCode);
			guessUserDao.insert(guessUser);

			addLoginInfo(userId, activityCode);

			overview.setIsLoginFirst(GuesssInfoDto.YES);
			overview.setFirstPoint(guessPointConfig.getFirstPoint());
		}else {//增加每日登录积分
			overview.setIsLoginFirst(GuesssInfoDto.NO);
			//查询当天是否首次登录
			GuesssFirstLogin login = new GuesssFirstLogin();
			login.setUserId(userId);
			Date now = new Date();
			login.setActivityCode(activityCode);
			login.setCreateTimeFrom(DateUtils.getDayStart(now));
			login.setCreateTimeTo(DateUtils.getDayEnd(now));
			List<GuesssFirstLogin> logins = firstLoginDao.findLogin(login);
			if(logins.size() == 0) {
				pointBiz.addPoint(userId, Long.valueOf(guessPointConfig.getLoginPoint()), PointRecord.SOURCE_GUESS, PointRecord.FLOW_ADD,
						"每日登录", activityCode);

				//增加每日首次登录信息
				addLoginInfo(userId, activityCode);

				overview.setIsTodayLoginFirst(GuesssInfoDto.YES);
				overview.setLoginPoint(guessPointConfig.getLoginPoint());
			}else {
				overview.setIsTodayLoginFirst(GuesssInfoDto.NO);
			}
		}
		//查询是否有未提示的连胜中奖信息
		GuesssPrizeDto prize = prizeDao.findNotPopup(userId, activityCode);
		if(prize != null) {
			//查询连胜信息
			overview.setIsGetPrize(GuesssInfoDto.YES);
			overview.setPrize(prize);
			//设置奖励为已提示
			GuesssPrize upPrize = new GuesssPrize();
			upPrize.setId(prize.getId());
			upPrize.setHasPopup(GuesssPrize.HAS_POPUP_YES);
			prizeDao.update(upPrize);
		}else {
			overview.setIsGetPrize(GuesssInfoDto.NO);
		}

		//设置是否有未使用的奖品数
		Integer notUseCount = prizeDao.findNotUseCount(userId, activityCode);
		Integer hasNotUsed = notUseCount > 0?1:0;
		overview.setHasNotUsePrize(hasNotUsed);

		Date guessDate = getGuessDate();
		String guessDateStr = DateUtils.formate(guessDate);
		Guesss param = new Guesss();
		param.setGuessDate(guessDateStr);
		param.setUserId(userId);
		param.setActivityCode(activityCode);
		List<Guesss> guesses = guessDao.findList(param);
		Integer hasGuess = guesses.size() > 0?1:0;
		overview.setHasGuess(hasGuess);
		overview.setActivityCode(activityCode);
		/*用户押涨还是压跌*/
		overview.setDirection(!guesses.isEmpty() ? guesses.get(0).getDirection() : null);
		/*增加是否交易日字段，用于前端展示不同的文案*/
		boolean isTradeDate = tradeDateBiz.isTradeDate(new Date());
		overview.setIsTradeDate(isTradeDate ? 1 : 0);
		return new StatusObjDto<GuesssInfoDto>(true,overview,StatusDto.SUCCESS,"");
	}

	public StatusObjDto<GuesssStatusDto> betStatus(String activityCode){
		Date guessDate = getGuessDate();
		String guessDateStr = DateUtils.formate(guessDate);

		GuesssStatusDto status = guessDao.findGuessStatus(guessDateStr, activityCode);
		if(status == null) { //无人竞猜
			status = new GuesssStatusDto();
			status.setDownPoint(0l);
			status.setUpPoint(0l);
		}

		GuesssStatusDto countStatus = guessDao.findGuessCount(guessDateStr, activityCode);
		if(countStatus == null) {//无人竞猜
			countStatus = new GuesssStatusDto();
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
		Integer isNextTradeDate = guessDateStr.equals(currentDateStr)?0:1;
		status.setIsNextTradeDate(isNextTradeDate);

		return new StatusObjDto<GuesssStatusDto>(true,status,StatusDto.SUCCESS,"");
	}

	public StatusDto guess(String userId,String activityCode,Integer direction, Integer point){
		StatusDto activityStatus = isValid(activityCode);
		if(!activityStatus.isOk()) {
			return activityStatus;
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

		GuessPointConfigDto guessPointConfig = JSON.parseObject(AppConfigUtils.get(ACTIVITY_GUESS_POINT_CONFIG), GuessPointConfigDto.class);
		if (guessPointConfig == null) {
			guessPointConfig = new GuessPointConfigDto();
		}
		if(point < guessPointConfig.getGuessMinPoint()) {
			throw ActivityBizException.GUESS_MIN_POINT.format(guessPointConfig.getGuessMinPoint());
		}
		if(point > guessPointConfig.getGuessMaxPoint()) {
			throw ActivityBizException.GUESS_MAX_POINT.format(guessPointConfig.getGuessMaxPoint());
		}

		Date guessDate = getGuessDate();
		String guessDateStr = DateUtils.formate(guessDate);
		//查询押注日期是否有押注信息
		Guesss param = new Guesss();
		param.setGuessDate(guessDateStr);
		param.setUserId(userId);
		param.setActivityCode(activityCode);
		List<Guesss> guesses = guessDao.findList(param);
		if(guesses.size() > 0) { //不能重复押注同个交易日
		    throw ActivityBizException.GUESS_TRADE_DAY_REPEAT;
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

		Guesss guess = new Guesss();
		guess.setUserId(userId);
		guess.setDirection(direction);
		guess.setUsePoint(point);
		guess.setGuessDate(guessDateStr);
		guess.setActivityCode(activityCode);
		guess.setCreateTime(new Date());
		guessDao.insert(guess);

		//减少积分
		pointBiz.addPoint(userId, Long.valueOf(point), PointRecord.SOURCE_GUESS, PointRecord.FLOW_PLUS,
				direction.equals(PointRecord.FLOW_ADD) ? "押涨" : "押跌", null);

		return new StatusDto(true,StatusDto.SUCCESS,"");
	}

	public StatusObjDto<List<Guesss>> guessRecord(String userId,String activityCode,Integer start, Integer end){
		if(ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		List<Guesss> guesses = guessDao.findPage(userId,activityCode,start, end);

		return new StatusObjDto<List<Guesss>>(true,guesses,StatusDto.SUCCESS,"");
	}

	@Override
	public StatusObjDto<List<WinRanksDto>> winRanking(Date date,String activityCode,Integer start, Integer end) {
		List<WinRanksDto> ranking = null;
		if(date == null) { //总连胜榜
			GuesssWin win = new GuesssWin();
			win.setActivityCode(activityCode);
			ranking = winDao.rankingList(win,start,end);
		}else {
			GuesssWinDetail winDetail = new GuesssWinDetail();
			winDetail.setType(GuesssWinDetail.TYPE_MONTH);
			winDetail.setTime(DateUtils.formate(date, "yyyy-MM"));
			winDetail.setActivityCode(activityCode);
			ranking = winDetailDao.rankingList(winDetail, start, end);
		}
		if(ranking == null) {
			ranking = new ArrayList<WinRanksDto>();
		}

		return new StatusObjDto<List<WinRanksDto>>(true,ranking,StatusDto.SUCCESS,"");
	}

	@Override
	public AchievementsDto  achievement(AccTokenUser user, String activityCode) {
		if(ObjectUtils.isEmptyOrNull(user)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("user");
		}
		String userId = user.getUserId();
		GuesssUser guessUser = guessUserDao.findByUserId(user.getUserId(), activityCode);
		if(guessUser == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("竞猜用户不存在");
		}

		AchievementsDto achievement = new AchievementsDto();

		//头像与手机获取
		achievement.setPortrait(user.getPortrait());
		String mobileLike = user.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
		achievement.setMobile(mobileLike);

		//积分获取
		Long point = null;
		StatusObjDto<Long> pointStatus = pointBiz.getPoint(userId,null);
		if(!pointStatus.isOk()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("获取积分信息异常");
		}else {
			point = pointStatus.getObj();
		}
		achievement.setPoint(point);
		Long guessCount = guessDao.guessCount(userId, activityCode);
		guessCount = guessCount == null?0:guessCount;
		achievement.setTotal(guessCount);

		//连胜与排序获取
		WinRanksDto ranking = winDao.ranking(userId, activityCode);
		Long winCount = ranking != null?ranking.getWinCount():0;
		Long order =  ranking != null?ranking.getOrder():0;
		achievement.setOrder(order);
		achievement.setWinCount(winCount);

		//打败用户比例计算
		/*if(ranking != null) {
			Long rCount = winDao.rankingCount(activityCode);
			Long beatCount = rCount - order;
			Double ratio = Double.valueOf(beatCount)/rCount*100;
			DecimalFormat  df  = new DecimalFormat("#.0");
			String ratioText = ratio == 0?"0":df.format(ratio);
			achievement.setBeatRatio(ratioText);
		}else {
			achievement.setBeatRatio("0");
		}*/

		/*累计胜利次数*/
		Long allWinCount = winDao.allWinCount(userId, activityCode);
		achievement.setAllWinCount(allWinCount);

		DecimalFormat  df  = new DecimalFormat("#.0");
		/*参与活动的总人数*/
	   	long allGuessUser = guessUserDao.count(activityCode);
		/*打败用户比例(累计胜利)*/
		if (allWinCount > 0) {
			/*查排序*/
			WinRanksDto winOrder = winDao.order(userId, activityCode);
		    /*有胜利记录的用户：打败用户数比例=（总人数-当前用户的排序） / 总人数*/
			double beatRatio = (double) (allGuessUser - winOrder.getOrder()) / allGuessUser * 100;
			achievement.setBeatRatio(df.format(beatRatio));

		} else {
			/*有胜利记录的人数*/
			long allWinUser = winDao.count(activityCode);
			/*没有胜利记录的用户：打败用户数比例=（总人数-有胜利记录的人数-1）/总人数*/
			double beatRatio = (double) (allGuessUser - allWinUser - 1) / allGuessUser * 100;
			achievement.setBeatRatio(df.format(beatRatio));
		}

		return achievement;
	}

	public StatusObjDto<List<UserPrizeDto>> userPrizes(String userId,String activityCode,Integer start, Integer end){
		if(ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}

		List<UserPrizeDto> prizes = prizeDao.findUserPrizes(userId, activityCode, start, end);
		for(UserPrizeDto prize:prizes) {
			prize.setSource(UserPrizeDto.SOURCE_GUESS);
		}
		return new  StatusObjDto<List<UserPrizeDto>>(true,prizes,StatusDto.SUCCESS,"");
	}

	public StatusObjDto<List<GuesssPrizeDto>> prizes(String activityCode,Integer start, Integer end){
		List<GuesssPrizeDto> prizes = prizeDao.findPrizes(activityCode, start, end);
		return new  StatusObjDto<List<GuesssPrizeDto>>(true,prizes,StatusDto.SUCCESS,"");
	}

	@Override
	public void getIndex() {
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
				GuesssIndex index = new GuesssIndex();
				index.setIndexDate(today);
				index.setType(1);
			    List<GuesssIndex> indexes = indexDao.findList(index);
			    if(indexes.size() == 0) {
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	index.setCreateTime(new Date());
			    	indexDao.insert(index);
			    }else {
			    	index = indexes.get(0);
			    	index.setOpen((Double)shindexes.get(0));
			    	index.setClose((Double)shindexes.get(1));
			    	indexDao.update(index);
			    }
			}
	    }
	}

	@Override
	public StatusDto getIndex(Integer type) {

		String today = DateUtils.formate(new Date());

		GuesssIndex index = new GuesssIndex();
		index.setIndexDate(today);
		index.setType(type);
		List<GuesssIndex> indexes = indexDao.findList(index);

		QuotationDto quotationDto = stockPrice(CODELIST_CACHE.get(type));
		QuotationDto.Record record = quotationDto.getDataResult().getRecord();

		if(indexes.size() == 0) {
			index.setOpen(record.getClosingPrice());
			/*定时任务执行时间：0 5 15 * * ? 所以现价就是今日收盘价*/
			index.setClose(record.getCurrentPrice());
			index.setCreateTime(new Date());
			indexDao.insert(index);

		}else {
			index = indexes.get(0);
			index.setType(type);
			index.setOpen(record.getClosingPrice());
			index.setClose(record.getCurrentPrice());
			indexDao.update(index);
		}

		return new StatusDto(true);
	}

	private QuotationDto stockPrice(String codelist) {
		Map<String, Object> busparams = Maps.newHashMap();
		busparams.put("method", "quote");
		busparams.put("datetime", "0(0-0)");
		busparams.put("datatype", "6,10");
		busparams.put("codelist", codelist);

		ResultDto result = RemoteUtils.call("quotation.infocooperation.get", ApiServiceTypeEnum.COOPERATION, busparams, false);
		String quotation = JSON.toJSONString(result.getData().get("Result"));

		return JSON.parseObject(quotation, QuotationDto.class);
	}

	@Override
	public void settle(String date, String activityCode, Integer type) {
		Thread  settleT = new Thread(new Runnable() {
			@Override
			public void run() {
				String sdate = date==null? DateUtils.formate(new Date()):date;
				settleHandle(sdate, activityCode, type);
			}
		});
		settleT.start();
	}

	@Transactional
	@Override
	public StatusObjDto<Object> pointPrize(String activityCode, String prizeType, String userId, String customerId, String mobile) {
		// 判断当前客户号是否与其它手机号绑定兑换过奖品
		ActPrize queryByCustomerId = new ActPrize()
				.setActivityCode(activityCode)
				.setCustomerId(customerId);
		/*按活动编码和customerId查奖品兑换记录*/
		List<ActPrize> dbActPrizes = actPrizeDao.findList(queryByCustomerId);
		Optional<ActPrize> first = dbActPrizes.stream().findFirst();
		if (first.isPresent() && !Objects.equals(first.get().getUserId(), userId)) {
			/*手机号脱敏*/
			String bound = first.get().getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");

			/*情况1：用户登录的客户号已经被其他手机号登录过*/
			HashMap<Object, Object> data = Maps.newHashMap();
			data.put("mobile", bound);
			return new StatusObjDto<>(false, data,
					ActivityBizException.ACT_PRIZE_POINT_BOUND_MOBILE.getCode(), ActivityBizException.ACT_PRIZE_POINT_BOUND_MOBILE.format(bound).getMsg());
		}

		ActPrize queryByUserId = new ActPrize()
				.setActivityCode(activityCode)
				.setUserId(userId);
		/*按活动编码和userId查奖品兑换记录*/
		dbActPrizes = actPrizeDao.findList(queryByUserId);
		first = dbActPrizes.stream().findFirst();
		if (first.isPresent() && !Objects.equals(first.get().getCustomerId(), customerId)) {
			/*手机号脱敏*/
			String bound = first.get().getCustomerId().replaceAll("(\\d{2})\\d{4}(\\d{2})", "$1****$2");

			/*用户此前已登录过a客户号，第二次又登陆了了b客户号*/
			HashMap<Object, Object> data = Maps.newHashMap();
			data.put("customerId", bound);
			return new StatusObjDto<>(false, data,
					ActivityBizException.ACT_PRIZE_POINT_BOUND_CUSTOMERID.getCode(), ActivityBizException.ACT_PRIZE_POINT_BOUND_CUSTOMERID.format(bound).getMsg());
		}

		// 判断积分是否足够兑换奖品
		ActPrizeType actPrizeType = findPrizeType(prizeType);
		/*奖品的积分价值*/
		Long prizePoint = actPrizeType.getPoint().longValue();
		/*查总积分*/
		Long point = pointBiz.getPoint(userId, null).getObj();
		if (point < prizePoint) {
			throw ActivityBizException.ACT_PRIZE_POINT_INSUFFICIENT;
		}

		// 兑换奖品
		AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO()
				.setActivityCode(activityCode)
				.setUserId(userId)
				.setCustomerId(customerId)
				.setPrizeType(prizeType)
				.setMobile(mobile);
		List<CouponRecieveStatusDto> recieveStatusDtos = couponRecieveBiz.receivePriceCommon(acReceivePriceVO);

		// 登记竞猜奖品记录表
		CouponRecieveStatusDto recieveStatusDto = recieveStatusDtos.get(0);
		MyPrizeDto prize = recieveStatusDto.getPrize();

		GuesssPrize guessPrize = new GuesssPrize();
		guessPrize.setUserId(userId);
		guessPrize.setRedeemCode(prize.getRedeemCode());
		guessPrize.setCardNo(prize.getCardNo());
		guessPrize.setCardPassword(prize.getCardPassword());
		guessPrize.setCreateTime(new Date());
		guessPrize.setType(prize.getType());
		guessPrize.setWinId(0L);
		guessPrize.setPrizeId(prize.getId());
		guessPrize.setPrizeCode(recieveStatusDto.getPrizeType());
		guessPrize.setHasPopup(0);
		guessPrize.setWinCount(null);
		guessPrize.setActivityCode(activityCode);
		prizeDao.insert(guessPrize);

		// 扣减积分、积分记录
		String prizeName = recieveStatusDto.getPrizeName();
		pointBiz.addPoint(userId, prizePoint, PointRecord.SOURCE_POINT_PRIZE, PointRecord.FLOW_PLUS,
				 "兑换" + prizeName, activityCode);

		return new StatusObjDto<>(true, recieveStatusDtos);
	}

	private void settleHandle(final String date, String activityCode, Integer type) {
		logger.info(date  + "猜涨跌开始计算奖励........................");
		try{
			getIndex(type);
		}catch(Exception ex) {

		}
		GuesssStatusDto status = guessDao.findGuessStatus(date, activityCode);
		if(status == null) {
			logger.info(date+"今日无人竞猜..........................");
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
		/*保留两位小数*/
		upRatio = processRatio(upRatio);
		downRatio = processRatio(downRatio);

		GuesssIndex param =  new GuesssIndex();
		param.setType(type);
		param.setIndexDate(date);
		GuesssIndex index = indexDao.findIndex(param);
		if(index == null) {
			logger.error(date+"无今日指数信息..........................");
			return;
		}
		Integer resultDirection = index.getClose() > index.getOpen()?Guesss.DIRECTION_UP:Guesss.DIRECTION_DOWN;

		int pageSize = 20;
		int count = 0;
		while(true) {
			List<Guesss> guesses = guessDao.findUnsettlement(date, activityCode, 0,pageSize);
			for(Guesss guess:guesses) {
				//处理竞猜结果
				guessResultBiz.handle(guess, resultDirection, upRatio, downRatio);
				count++;
			}
			if(guesses.size() < pageSize) {
				break;
			}
		}
		logger.error(date+"猜涨跌开始计算奖励完成，共处理"+count+"条信息..........................");
	}

	private Double processRatio(Double ratio) {
		int r1 = (int) (ratio * 100);
		return r1 / 100.0;
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

	private void addLoginInfo(String userId, String activityCode) {
		GuesssFirstLogin login = new GuesssFirstLogin();
		login.setUserId(userId);
		login.setCreateTime(new Date());
		login.setActivityCode(activityCode);
		firstLoginDao.insert(login);
	}

}
