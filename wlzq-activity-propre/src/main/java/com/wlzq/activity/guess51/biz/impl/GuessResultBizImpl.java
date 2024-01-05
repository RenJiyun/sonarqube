package com.wlzq.activity.guess51.biz.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.guess51.biz.GuessResultBiz;
import com.wlzq.activity.guess51.dao.GuessDao;
import com.wlzq.activity.guess51.dao.GuessUserDao;
import com.wlzq.activity.guess51.dao.GuessWinDao;
import com.wlzq.activity.guess51.model.Guess;
import com.wlzq.activity.guess51.model.GuessPrize;
import com.wlzq.activity.guess51.model.GuessPrizeSetting;
import com.wlzq.activity.guess51.model.GuessUser;
import com.wlzq.activity.guess51.model.GuessWin;
import com.wlzq.common.model.account.PointRecord;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.remote.service.common.account.PointBiz;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

/**
 * 猜涨跌结果处理接口实现
 * @author louie
 *
 */
@Service
public class GuessResultBizImpl implements GuessResultBiz {
	private Logger logger = LoggerFactory.getLogger(GuessResultBizImpl.class);
	private static final String CONFIG_PRIZE_SETTING = "activity.guess51.prize";
	@Autowired
	private	GuessUserDao guessUserDao;
	@Autowired
	private	GuessDao guessDao;
	@Autowired
	private	GuessWinDao winDao;
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
	public void handle(Guess guess,Integer resultDirection,Double upRatio,Double downRatio) {
		guess.setResultDirection(resultDirection);
		Integer direction = guess.getDirection();
		Double ratio = direction.equals(Guess.DIRECTION_UP)?upRatio:downRatio;
		guess.setRatio(ratio);
		if(direction.equals(resultDirection)) {
			Double winPoint = guess.getUsePoint()*ratio;
			
			//增加账户积分
			pointBiz.addPoint(guess.getUserId(), winPoint.longValue(), PointRecord.SOURCE_GUESS_51, PointRecord.FLOW_ADD,
					"51猜涨跌赢取", null);
			
			guess.setWinPoint(winPoint.longValue());
			//更新总连胜信息
			GuessWin winInfo = calculateWinInfo(guess,DateUtils.parseDate(guess.getGuessDate(),"yyyy-MM-dd"));
			if(ObjectUtils.isEmptyOrNull(winInfo.getId())){//添加新连胜信息
				winDao.insert(winInfo);
			}else {//更新连胜信息
				winDao.update(winInfo);
			}
			
			//是否获取连胜奖励
			Long winCount = winInfo.getWinCount();
			//查询连胜奖励...
			GuessPrizeSetting prizeSetting = getPrizeSetting(winCount.intValue());
			if(prizeSetting != null) { //若该连胜有奖励，添加奖励信息
				GuessPrize guessPrize = getPrize(winInfo,prizeSetting);
				if(guessPrize != null) {
					//更新奖品状态为已发出
					prizeBiz.updatePrize(guess.getUserId(),guessPrize.getPrizeId(), ActPrize.STATUS_SEND);
				}
			}
		}else {
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
	private GuessWin calculateWinInfo(Guess guess,Date date) {
		//查询最近的连胜信息
		String userId = guess.getUserId();
		GuessWin guessWin = winDao.findLastWin(userId);
		if(guessWin == null) {//无连胜信息，添加
			guessWin = addNewWinInfo(userId, date,guess.getGuessNo());
			return guessWin;
		}
		//查询上次竞猜信息
		Date winToDate = guessWin.getWinToDate();
		String winToDateStr = DateUtils.formate(winToDate);
		String[] winDates = guessWin.getWinDates().split(",");
		Integer lastWinGuessNo = Integer.valueOf(winDates[winDates.length - 1].split(":")[1]);
		
		Guess lastGuess = guessDao.findLastGuess(guess);
		//若上次竞猜正确交易日及场次与连胜交易日相同，则更新连胜次数、连胜结束日期与连胜所有日期
		if(lastGuess != null && lastGuess.getGuessDate().equals(winToDateStr) &&
				lastGuess.getGuessNo().equals(lastWinGuessNo)) {
			guessWin.setWinToDate(date);
			guessWin.setWinCount(guessWin.getWinCount() + 1);
			guessWin.setWinDates(guessWin.getWinDates()+","+DateUtils.formate(date)+":"+guess.getGuessNo());
			guessWin.setUpdateTime(new Date());
			return guessWin;
		}else {
			return addNewWinInfo(userId, date,guess.getGuessNo());
		}
	}

	
	/**
	 * 添加新连胜信息
	 * @param userId
	 * @param date
	 */
	private GuessWin addNewWinInfo(String userId,Date date,Integer guessNo) {
		GuessWin guessWin = new GuessWin();
		guessWin.setUserId(userId);
		guessWin.setWinCount(1l);
		guessWin.setWinFromDate(date);
		guessWin.setWinToDate(date);
		guessWin.setWinDates(DateUtils.formate(date)+":"+guessNo);
		guessWin.setCreateTime(new Date());
		
		return guessWin;
	}
	
	/**
	 * 添加连胜奖励
	 * @param guess
	 * @param prize
	 */
	private GuessPrize getPrize(GuessWin win,GuessPrizeSetting prize) {
		GuessUser user = guessUserDao.findByUserId(win.getUserId());
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
		GuessPrize guessPrize = new GuessPrize();
		BeanUtils.copyProperties(actPrize, guessPrize);
		guessPrize.setUserId(win.getUserId());
		guessPrize.setWinId(win.getId());
		guessPrize.setPrizeCode(actPrize.getCode());
		guessPrize.setPrizeId(actPrize.getId());
		guessPrize.setWinCount(prize.getWinCount());
		guessPrize.setCreateTime(new Date());
		
		return guessPrize;
	}

    private GuessPrizeSetting getPrizeSetting(Integer count) {
		List<String> prizes = AppConfigUtils.getList(CONFIG_PRIZE_SETTING, ";");
		for(String prize:prizes) {
			String[] prizeInfo = prize.split(":");
			if(prizeInfo.length != 2) continue;
			Integer winCount  = Integer.valueOf(prizeInfo[0]);
			if(winCount.equals(count)) {
				String prizeCode = prizeInfo[1];
				GuessPrizeSetting prizeSetting = new GuessPrizeSetting();
				prizeSetting.setWinCount(winCount);
				prizeSetting.setPrizeCode(prizeCode);
				return prizeSetting;
			}
		}
		return null;
	}
}
