/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.turntable.dao;

import com.wlzq.activity.turntable.model.Turntablee;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 大转盘DAO接口
 * @author louie
 * @version 2018-05-30
 */
@MybatisScan
public interface TurntableeDao extends CrudDao<Turntablee> {
	/**
	 * 抽奖次数
	 * @param turntable
	 * @return
	 */
	Integer turnCount(Turntablee turntable);
}