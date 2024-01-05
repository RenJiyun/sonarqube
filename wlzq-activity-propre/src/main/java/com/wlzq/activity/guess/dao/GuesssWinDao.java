/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.guess.dto.WinRanksDto;
import com.wlzq.activity.guess.model.GuesssWin;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 连胜信息DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuesssWinDao extends CrudDao<GuesssWin> {
	/**
	 * 获取最近的连胜信息
	 * @param userId
	 * @param activityCode TODO
	 * @return
	 */
	GuesssWin findLastWin(@Param("userId")String userId, @Param("activityCode")String activityCode);

	/**
	 * 连胜排名
	 * @param win
	 * @return
	 */
	List<WinRanksDto> rankingList(@Param("win")GuesssWin win,@Param("start")Integer start,@Param("end")Integer end);

	/**
	 * 连胜排名
	 * @param activityCode TODO
	 * @param win
	 * @return
	 */
	WinRanksDto ranking(@Param("userId")String userId, @Param("activityCode")String activityCode);

	/**
	 * 排名总人数
	 * @param activityCode TODO
	 * @return
	 */
	Long rankingCount(@Param("activityCode")String activityCode);

	/**
	 * 计算累计获胜次数
	 */
    Long allWinCount(@Param("userId") String userId, @Param("activityCode") String activityCode);

	/**
	 * 胜利排行榜，排序
	 */
	WinRanksDto order(@Param("userId") String userId, @Param("activityCode") String activityCode);

	/**
	 * 有胜利记录的总人数
	 */
	long count(@Param("activityCode") String activityCode);
}
