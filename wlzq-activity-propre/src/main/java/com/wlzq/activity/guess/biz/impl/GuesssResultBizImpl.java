package com.wlzq.activity.guess.biz.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.guess.biz.GuesssResultBiz;
import com.wlzq.activity.guess.dao.GuesssDao;
import com.wlzq.activity.guess.dao.GuesssPrizeDao;
import com.wlzq.activity.guess.dao.GuesssPrizeSettingDao;
import com.wlzq.activity.guess.dao.GuesssUserDao;
import com.wlzq.activity.guess.dao.GuesssWinDao;
import com.wlzq.activity.guess.dao.GuesssWinDetailDao;
import com.wlzq.activity.guess.model.Guesss;
import com.wlzq.activity.guess.model.GuesssPrize;
import com.wlzq.activity.guess.model.GuesssPrizeSetting;
import com.wlzq.activity.guess.model.GuesssUser;
import com.wlzq.activity.guess.model.GuesssWin;
import com.wlzq.activity.guess.model.GuesssWinDetail;
import com.wlzq.common.model.account.PointRecord;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.remote.service.common.account.PointBiz;

/**
 * 猜涨跌结果处理接口实现
 * @author louie
 *
 */
@Service
public class GuesssResultBizImpl implements GuesssResultBiz {
	private Logger logger = LoggerFactory.getLogger(GuesssResultBizImpl.class);
	@Autowired
	private	GuesssUserDao guessUserDao;
	@Autowired
	private	GuesssDao guessDao;
	@Autowired
	private	GuesssWinDao winDao;
	@Autowired
	private	GuesssWinDetailDao winDetailDao;
	@Autowired
	private GuesssPrizeSettingDao prizeSettingDao;
	@Autowired
	private GuesssPrizeDao guessPrizeDao;
	@Autowired
	private	PointBiz pointBiz;
	@Autowired
	private	ActPrizeBiz prizeBiz;

	/**
	 * 处理竞猜者竞猜结果
	 * @param guess
	 * @param resultDirection
	 * @param upRatio
	 * @param downRatio
	 */
	@Transactional
	public void handle(Guesss guess,Integer resultDirection,Double upRatio,Double downRatio) {
		guess.setResultDirection(resultDirection);
		Integer direction = guess.getDirection();
		Double ratio = direction.equals(Guesss.DIRECTION_UP)?upRatio:downRatio;
		guess.setRatio(ratio);
		if(direction.equals(resultDirection)) {
			Double winPoint = guess.getUsePoint()*ratio;

			//增加账户积分
			pointBiz.addPoint(guess.getUserId(), winPoint.longValue(), PointRecord.SOURCE_GUESS, PointRecord.FLOW_ADD,
					(direction.equals(PointRecord.FLOW_ADD) ? "押涨" : "押跌") + guess.getUsePoint() + "积分，胜，赔率：" + ratio, null);

			guess.setWinPoint(winPoint.longValue());
			//更新总连胜信息
			GuesssWin winInfo = calculateWinInfo(guess,DateUtils.parseDate(guess.getGuessDate(),"yyyy-MM-dd"));
			if(ObjectUtils.isEmptyOrNull(winInfo.getId())){//添加新连胜信息
				winDao.insert(winInfo);
			}else {//更新连胜信息
				winDao.update(winInfo);
			}
			//更新月连胜信息
			GuesssWinDetail winDetailInfo = calculateWinDetailInfo(guess,DateUtils.parseDate(guess.getGuessDate(),"yyyy-MM-dd"));
			if(ObjectUtils.isEmptyOrNull(winDetailInfo.getId())){//添加新月连胜信息
				winDetailDao.insert(winDetailInfo);
			}else {//更新月连胜信息
				winDetailDao.update(winDetailInfo);
			}

			//是否获取连胜奖励
			Long winCount = winInfo.getWinCount();
			//查询连胜奖励...
			GuesssPrizeSetting prizeSetting = prizeSettingDao.findByWinCount(winCount, guess.getActivityCode());
			if(prizeSetting != null) { //若该连胜有奖励，添加奖励信息
				GuesssPrize guessPrize = getPrize(winInfo,prizeSetting);
				if(guessPrize != null && !ActPrize.TYPE_CARD_PASSWORD.equals(guessPrize.getType())) {
					guessPrizeDao.insert(guessPrize);
					//更新奖品状态为已发出
					prizeBiz.updatePrize(guess.getUserId(),guessPrize.getPrizeId(), ActPrize.STATUS_SEND);
				}
			}
		}else {
			/*没有猜中也要记录*/
			pointBiz.addPoint(guess.getUserId(), 0L, PointRecord.SOURCE_GUESS, PointRecord.FLOW_ADD,
					(direction.equals(PointRecord.FLOW_ADD) ? "押涨" : "押跌") + guess.getUsePoint() + "积分，负，赔率：" + ratio, null);
			guess.setWinPoint(Long.valueOf(-guess.getUsePoint()));
		}
		guess.setUpdateTime(new Date());
		guessDao.update(guess);
	}

	/**
	 * 计算连胜信息
	 * @param userId
	 * @param date
	 */
	private GuesssWin calculateWinInfo(Guesss guess,Date date) {
		//查询最近的连胜信息
		String userId = guess.getUserId();
		String activityCode = guess.getActivityCode();
		GuesssWin guessWin = winDao.findLastWin(userId, activityCode);
		if(guessWin == null) {//无连胜信息，添加
			guessWin = addNewWinInfo(userId, date, activityCode);
			return guessWin;
		}
		/*
		Date winToDate = guessWin.getWinToDate();
		if(tradeDateBiz.isContinuousTradeDate(winToDate, date)) {//更新连胜信息
			guessWin.setWinToDate(date);
			guessWin.setWinCount(guessWin.getWinCount() + 1);
			guessWin.setUpdateTime(new Date());
			return guessWin;
		}else {//添加新连胜信息
			return addNewWinInfo(userId, date);
		}
		*/
		//查询上次竞猜信息
		Date winToDate = guessWin.getWinToDate();
		String winToDateStr = DateUtils.formate(winToDate);
		Guesss lastGuess = guessDao.findLastGuess(guess);
		//若上次竞猜正确交易日与连胜交易日相同，则更新连胜次数、连胜结束日期与连胜所有日期
		if(lastGuess != null && lastGuess.getGuessDate().equals(winToDateStr)) {
			guessWin.setWinToDate(date);
			guessWin.setWinCount(guessWin.getWinCount() + 1);
			guessWin.setWinDates(guessWin.getWinDates()+","+DateUtils.formate(date));
			guessWin.setUpdateTime(new Date());
			return guessWin;
		}else {
			return addNewWinInfo(userId, date, activityCode);
		}
	}

	/**
	 * 计算月连胜信息
	 * @param userId
	 * @param date
	 */
	private GuesssWinDetail calculateWinDetailInfo(Guesss guess,Date date) {
		//查询最近的连胜信息
		String userId = guess.getUserId();
		String activityCode = guess.getActivityCode();
		String month  = DateUtils.formate(date,"yyyy-MM");
		GuesssWinDetail guessWin = winDetailDao.findLastWin(userId,activityCode,GuesssWinDetail.TYPE_MONTH, month);
		if(guessWin == null) {//无连胜信息，添加
			guessWin = addNewWinDetailInfo(userId, activityCode, date);
			return guessWin;
		}
		/*
		Date winToDate = guessWin.getWinToDate();
		if(tradeDateBiz.isContinuousTradeDate(winToDate, date)) {//更新连胜信息
			guessWin.setWinToDate(date);
			guessWin.setWinCount(guessWin.getWinCount() + 1);
			guessWin.setUpdateTime(new Date());
			return guessWin;
		}else {//添加新连胜信息
			return addNewWinInfo(userId, date);
		}
		*/
		//查询上次竞猜信息
		Date winToDate = guessWin.getWinToDate();
		String winToDateStr = DateUtils.formate(winToDate);
		String winToDateMonth = DateUtils.formate(winToDate,"yyyy-MM");
		Guesss lastGuess = guessDao.findLastGuess(guess);
		//若月上次竞猜正确交易日与连胜交易日相同，则更新连胜次数、连胜结束日期与连胜所有日期
		if(lastGuess != null && lastGuess.getGuessDate().equals(winToDateStr) && month.equals(winToDateMonth)) {
			guessWin.setWinToDate(date);
			guessWin.setWinCount(guessWin.getWinCount() + 1);
			guessWin.setWinDates(guessWin.getWinDates()+","+DateUtils.formate(date));
			guessWin.setUpdateTime(new Date());
			return guessWin;
		}else {
			return addNewWinDetailInfo(userId, activityCode, date);
		}
	}

	/**
	 * 添加新连胜信息
	 * @param userId
	 * @param date
	 * @param activityCode TODO
	 */
	private GuesssWin addNewWinInfo(String userId,Date date, String activityCode) {
		GuesssWin guessWin = new GuesssWin();
		guessWin.setUserId(userId);
		guessWin.setWinCount(1l);
		guessWin.setWinFromDate(date);
		guessWin.setWinToDate(date);
		guessWin.setActivityCode(activityCode);
		guessWin.setWinDates(DateUtils.formate(date));
		guessWin.setCreateTime(new Date());

		return guessWin;
	}

	/**
	 * 添加新月连胜信息
	 * @param userId
	 * @param activityCode TODO
	 * @param date
	 */
	private GuesssWinDetail addNewWinDetailInfo(String userId,String activityCode, Date date) {
		String time = DateUtils.formate(date, "yyyy-MM");
		GuesssWinDetail guessWinDetail = new GuesssWinDetail();
		guessWinDetail.setType(GuesssWinDetail.TYPE_MONTH);
		guessWinDetail.setTime(time);
		guessWinDetail.setUserId(userId);
		guessWinDetail.setWinCount(1l);
		guessWinDetail.setWinFromDate(date);
		guessWinDetail.setWinToDate(date);
		guessWinDetail.setActivityCode(activityCode);
		guessWinDetail.setWinDates(DateUtils.formate(date));
		guessWinDetail.setCreateTime(new Date());

		return guessWinDetail;
	}

	/**
	 * 添加连胜奖励
	 * @param guess
	 * @param prize
	 */
	private GuesssPrize getPrize(GuesssWin win,GuesssPrizeSetting prize) {
		GuesssUser user = guessUserDao.findByUserId(win.getUserId(), win.getActivityCode());
		if(user == null) {
			logger.info(win.getUserId()+"添加奖励时用户不存在...............");
			return null;
		}
		/*
		String openId = user.getOpenid();
		if(ObjectUtils.isNotEmptyOrNull(openId)) {
			logger.info(win.getUserId()+"微信信息为空...............");
			return;
		}
		*/
		ActPrize actPrize = prizeBiz.findAvailablePrize(prize.getPrizeCode());
		if(actPrize == null) {
			logger.info(prize.getPrizeCode()+"奖励未设置...............");
			return null;
		}
		GuesssPrize guessPrize = new GuesssPrize();
		BeanUtils.copyProperties(actPrize, guessPrize);
		guessPrize.setUserId(win.getUserId());
		guessPrize.setWinId(win.getId());
		guessPrize.setActivityCode(win.getActivityCode());
		guessPrize.setPrizeCode(actPrize.getCode());
		guessPrize.setPrizeId(actPrize.getId());
		guessPrize.setWinCount(prize.getWinCount());
		guessPrize.setCreateTime(new Date());

		return guessPrize;
	}
}
