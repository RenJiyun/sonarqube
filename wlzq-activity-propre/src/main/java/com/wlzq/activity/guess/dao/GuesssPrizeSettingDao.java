/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.guess.model.GuesssPrizeSetting;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 竞猜奖品设置DAO接口
 * @author louie
 * @version 2018-05-25
 */
@MybatisScan
public interface GuesssPrizeSettingDao extends CrudDao<GuesssPrizeSetting> {
	/**
	 * 连胜查询奖励
	 * @param winCount
	 * @param activityCode TODO
	 * @return
	 */
	GuesssPrizeSetting findByWinCount(@Param("winCount")Long winCount, @Param("activityCode")String activityCode);
}