/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.guess.dto.GuesssPrizeDto;
import com.wlzq.activity.guess.model.GuesssPrize;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 竞猜活动奖品DAO接口
 * @author louie
 * @version 2018-05-25
 */
@MybatisScan
public interface GuesssPrizeDao extends CrudDao<GuesssPrize> {

	/**
	 * 中奖列表
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @return
	 */
	List<GuesssPrizeDto> findPrizes(@Param("activityCode")String activityCode,@Param("start")Integer start, @Param("end")Integer end);
	
	/**
	 * 用户中奖列表
	 * @param userId
	 * @param activityCode TODO
	 * @param start
	 * @param end
	 * @return
	 */
	List<UserPrizeDto> findUserPrizes(@Param("userId")String userId,@Param("activityCode")String activityCode,@Param("start")Integer start, @Param("end")Integer end);
	
	/**
	 * 兑换码查询奖品
	 * @param redeemCode
	 * @return
	 */
	GuesssPrize findByRedeem(String redeemCode);
	
	/**
	 * 查询未弹出提示的奖品
	 * @param userId
	 * @param activityCode TODO
	 * @return
	 */
	GuesssPrizeDto findNotPopup(@Param("userId")String userId, @Param("activityCode")String activityCode);
	
	/**
	 * 查询未使用奖品
	 * @param userId
	 * @param activityCode TODO
	 * @return
	 */
	Integer findNotUseCount(@Param("userId")String userId, @Param("activityCode")String activityCode);
}