/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.turntable.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.turntable.dto.TurntableePrizeDto;
import com.wlzq.activity.turntable.model.TurntableePrize;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 大转盘奖品DAO接口
 * @author louie
 * @version 2018-05-30
 */
@MybatisScan
public interface TurntableePrizeDao extends CrudDao<TurntableePrize> {

	/**
	 * 中奖列表
	 * @param activity TODO
	 * @param start
	 * @param end
	 * @return
	 */
	List<TurntableePrizeDto> findPrizes(@Param("activity")String activity,@Param("start")Integer start, @Param("end")Integer end);

	/**
	 * 用户中奖列表
	 * @param userId
	 * @param activity TODO
	 * @param start
	 * @param end
	 * @return
	 */
	List<UserPrizeDto> findUserPrizes(@Param("userId")String userId, @Param("activity")String activity,@Param("start")Integer start, @Param("end")Integer end);
	
	/**
	 * 查询未使用的奖品数
	 * @param userId
	 * @param activity TODO
	 * @return
	 */
	Integer findNotUseCount(@Param("userId")String userId, @Param("activity")String activity);
}