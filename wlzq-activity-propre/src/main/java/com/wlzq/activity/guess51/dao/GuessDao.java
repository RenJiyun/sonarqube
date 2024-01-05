/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.guess51.dto.GuessStatusDto;
import com.wlzq.activity.guess51.model.Guess;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 竞猜信息DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuessDao extends CrudDao<Guess> {
	/**
	 * 查询猜跌猜涨总部分
	 * @param date
	 * @return
	 */
	GuessStatusDto findGuessStatus(@Param("guessDate")String date, @Param("guessNo")Integer guessNo);
	/**
	 * 查询猜跌猜涨总数量
	 * @param date
	 * @return
	 */
	GuessStatusDto findGuessCount(@Param("guessDate")String date, @Param("guessNo")Integer guessNo);
	
	/**
	 * 竞猜次数
	 * @param userId
	 * @return
	 */
	Long guessCount(String userId);
	
	/**
	 * 查询上一次竞猜信息
	 * @param guess
	 * @return
	 */
	Guess findLastGuess(Guess guess);

	/**
	 * 查询未结算的竞猜
	 * @param date
	 * @param start
	 * @param end
	 * @return
	 */
	List<Guess> findUnsettlement(@Param("guessDate")String date,@Param("guessNo")Integer guessNo,@Param("start")Integer start,@Param("end")Integer end);
	
	/**
	 * 查询竞猜记录
	 * @param userId
	 * @param start
	 * @param end
	 * @return
	 */
	List<Guess> findPage(@Param("userId")String userId,@Param("start")Integer start,@Param("end")Integer end);
}