package com.wlzq.activity.guess.biz;

import java.util.Date;
import java.util.List;

import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.guess.dto.AchievementsDto;
import com.wlzq.activity.guess.dto.GuesssInfoDto;
import com.wlzq.activity.guess.dto.GuesssPrizeDto;
import com.wlzq.activity.guess.dto.GuesssStatusDto;
import com.wlzq.activity.guess.dto.WinRanksDto;
import com.wlzq.activity.guess.model.Guesss;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 猜涨跌业务接口
 * @author louie
 *
 */
public interface GuesssBiz {

	/**
	 * 概况
	 * @param activityCode TODO
	 * @param userId
	 * @return
	 */
	public StatusObjDto<GuesssInfoDto> overview(AccTokenUser user, String activityCode);

	/**
	 * 竞猜情况
	 * @param activityCode TODO
	 * @param userId 用户ID
	 * @return
	 */
	public StatusObjDto<GuesssStatusDto> betStatus(String activityCode);

	/**
	 * 战绩情况
	 * @param user
	 * @param activityCode TODO
	 * @return
	 */
	public AchievementsDto  achievement(AccTokenUser user, String activityCode);

	/**
	 * 竞猜
	 * @param userId 用户ID
	 * @param activityCode TODO
	 * @param direction 押涨跌，0：押跌，1：押涨
	 * @param point 使用积分
	 * @return
	 */
	public StatusDto guess(String userId,String activityCode,Integer direction, Integer point);

	/**
	 * 竞猜记录
	 * @param userId
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<Guesss>> guessRecord(String userId,String activityCode,Integer start, Integer end);

	/**
	 * 连胜榜单
	 * @param date 连胜所在月连胜榜，null时总连胜榜
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<WinRanksDto>> winRanking(Date date,String activityCode,Integer start, Integer end);

	/**
	 * 奖品记录
	 * @param userId
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<UserPrizeDto>> userPrizes(String userId,String activityCode,Integer start, Integer end);

	/**
	 * 奖品记录
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @param userId
	 * @return
	 */
	public StatusObjDto<List<GuesssPrizeDto>> prizes(String activityCode,Integer start, Integer end);
	/**
	 * 获取指数信息
	 */
	public void getIndex();

	/**
	 * 计算得分(定时计划执行)
	 *  @param date
	 * @param activityCode TODO
	 */
	void settle(String date, String activityCode, Integer type);

	/**
	 * 积分兑换奖品
	 */
    StatusObjDto<Object> pointPrize(String activityCode, String prizeType, String userId, String customerId, String mobile);

	/**
	 * 获取沪深300行情数据，存储到中台的指数模块
	 */
	StatusDto getIndex(Integer type);
}
