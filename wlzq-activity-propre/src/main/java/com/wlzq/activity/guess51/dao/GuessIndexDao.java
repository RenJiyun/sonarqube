/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.dao;

import com.wlzq.activity.guess51.model.GuessIndex;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 大盘指数DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuessIndexDao extends CrudDao<GuessIndex> {
	/**
	 * 查询指数
	 * @param index
	 * @return
	 */
	GuessIndex findIndex(GuessIndex index);
}