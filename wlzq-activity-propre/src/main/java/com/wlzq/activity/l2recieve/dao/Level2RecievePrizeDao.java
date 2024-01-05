/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.dao;

import com.wlzq.activity.l2recieve.model.Level2RecievePrize;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * level2领取活动奖品DAO接口
 * @author louie
 * @version 2018-05-03
 */
@MybatisScan
public interface Level2RecievePrizeDao extends CrudDao<Level2RecievePrize> {
	Level2RecievePrize findPrize(Integer type);
}