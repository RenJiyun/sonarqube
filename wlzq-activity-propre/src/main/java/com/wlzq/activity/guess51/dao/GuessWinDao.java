/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.guess51.dto.WinRankDto;
import com.wlzq.activity.guess51.model.GuessWin;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 连胜信息DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuessWinDao extends CrudDao<GuessWin> {
	/**
	 * 获取最近的连胜信息
	 * @param userId
	 * @return
	 */
	GuessWin findLastWin(String userId);
	
	/**
	 * 连胜排名
	 * @param win
	 * @return
	 */
	List<WinRankDto> rankingList(@Param("win")GuessWin win,@Param("start")Integer start,@Param("end")Integer end);

	/**
	 * 连胜排名
	 * @param win
	 * @return
	 */
	WinRankDto ranking(String userId);
	
	/**
	 * 排名总人数
	 * @return
	 */
	Long rankingCount();
}