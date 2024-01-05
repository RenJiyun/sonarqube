/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.dao;

import com.wlzq.activity.guess51.model.GuessUser;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 猜涨跌用户DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuessUserDao extends CrudDao<GuessUser> {
	GuessUser findByUserId(String userId);
}