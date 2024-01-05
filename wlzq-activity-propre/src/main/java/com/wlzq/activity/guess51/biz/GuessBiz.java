package com.wlzq.activity.guess51.biz;

import java.util.Date;
import java.util.List;

import com.wlzq.activity.guess51.dto.AchievementDto;
import com.wlzq.activity.guess51.dto.GuessDto;
import com.wlzq.activity.guess51.dto.GuessInfoDto;
import com.wlzq.activity.guess51.dto.GuessPrizeDto;
import com.wlzq.activity.guess51.dto.GuessStatusDto;
import com.wlzq.activity.guess51.dto.WinRankDto;
import com.wlzq.activity.guess51.model.Guess;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 猜涨跌业务接口
 * @author louie
 *
 */
public interface GuessBiz {

	/**
	 * 概况
	 * @param userId
	 * @return
	 */
	public StatusObjDto<GuessInfoDto> overview(AccTokenUser user);

	/**
	 * 竞猜情况
	 * @param userId 用户ID
	 * @return
	 */
	public StatusObjDto<GuessStatusDto> betStatus();
	
	/**
	 * 战绩情况
	 * @param user
	 * @return
	 */
	public AchievementDto  achievement(AccTokenUser user);
	
	/**
	 * 竞猜
	 * @param userId 用户ID
	 * @param direction 押涨跌，0：押跌，1：押涨
	 * @param point 使用积分
	 * @return
	 */
	public StatusObjDto<GuessDto> guess(String userId,Integer direction,Integer point);
	
	/**
	 * 竞猜记录
	 * @param userId
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<Guess>> guessRecord(String userId,Integer start,Integer end);
	
	/**
	 * 连胜榜单
	 * @param date 连胜所在月连胜榜，null时总连胜榜
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<WinRankDto>> winRanking(Date date,Integer start,Integer end);

	/**
	 * 奖品记录
	 * @param userId
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<GuessPrizeDto>> prizes(Integer start,Integer end);

	/**
	 * 获取开盘指数信息
	 */
	public void getOpenIndex();

	/**
	 * 获取上午收盘指数信息
	 */
	public void getMorningCloseIndex();
	
	/**
	 * 获取收盘指数信息
	 */
	public void getCloseIndex();

	/**
	 * 计算得分(定时计划执行)
	 *  @param date 
	 *  @param guessNo 
	 */
	void settle(String date,Integer guessNo);
}
