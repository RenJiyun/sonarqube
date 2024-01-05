/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.guess.dto.GuesssStatusDto;
import com.wlzq.activity.guess.model.Guesss;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 竞猜信息DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuesssDao extends CrudDao<Guesss> {
	/**
	 * 查询猜跌猜涨总部分
	 * @param date
	 * @param activityCode TODO
	 * @return
	 */
	GuesssStatusDto findGuessStatus( @Param("guessDate")String date, @Param("activityCode") String activityCode);
	/**
	 * 查询猜跌猜涨总数量
	 * @param date
	 * @param activityCode TODO
	 * @return
	 */
	GuesssStatusDto findGuessCount(@Param("guessDate")String date, @Param("activityCode") String activityCode);
	
	/**
	 * 竞猜次数
	 * @param userId
	 * @param activityCode TODO
	 * @return
	 */
	Long guessCount(@Param("userId")String userId, @Param("activityCode")String activityCode);
	
	/**
	 * 查询上一次竞猜信息
	 * @param guess
	 * @return
	 */
	Guesss findLastGuess(Guesss guess);

	/**
	 * 查询未结算的竞猜
	 * @param date
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @return
	 */
	List<Guesss> findUnsettlement(@Param("guessDate")String date,@Param("activityCode")String activityCode,@Param("start")Integer start, @Param("end")Integer end);
	
	/**
	 * 查询竞猜记录
	 * @param userId
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @return
	 */
	List<Guesss> findPage(@Param("userId")String userId,@Param("activityCode")String activityCode,@Param("start")Integer start, @Param("end")Integer end);
}