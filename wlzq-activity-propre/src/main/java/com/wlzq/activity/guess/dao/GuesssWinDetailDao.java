/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.guess.dto.WinRanksDto;
import com.wlzq.activity.guess.model.GuesssWinDetail;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 连胜信息(按类型)DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuesssWinDetailDao extends CrudDao<GuesssWinDetail> {
	/**
	 * 获取最近的连胜信息
	 * @param userId
	 * @param activityCode TODO
	 * @return
	 */
	GuesssWinDetail findLastWin(@Param("userId")String userId,@Param("activityCode")String activityCode,@Param("type")Integer type, @Param("time")String time);
	
	/**
	 * 连胜排名
	 * @param win
	 * @return
	 */
	List<WinRanksDto> rankingList(@Param("win")GuesssWinDetail win,@Param("start")Integer start,@Param("end")Integer end);

	/**
	 * 连胜排名
	 * @param win
	 * @return
	 */
	WinRanksDto ranking(String userId,@Param("type")Integer type,@Param("time")String time);
	
	/**
	 * 排名总人数
	 * @return
	 */
	Long rankingCount(@Param("type")Integer type,@Param("time")String time);
}