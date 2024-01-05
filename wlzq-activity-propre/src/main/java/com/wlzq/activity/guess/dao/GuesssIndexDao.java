/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import com.wlzq.activity.guess.model.GuesssIndex;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 大盘指数DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuesssIndexDao extends CrudDao<GuesssIndex> {
	/**
	 * 查询指数
	 * @param index
	 * @return
	 */
	GuesssIndex findIndex(GuesssIndex index);
}