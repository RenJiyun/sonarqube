/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import java.util.List;

import com.wlzq.activity.guess.model.GuesssFirstLogin;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 当天首次登录信息DAO接口
 * @author louie
 * @version 2018-05-22
 */
@MybatisScan
public interface GuesssFirstLoginDao extends CrudDao<GuesssFirstLogin> {
	/**
	 * 查询登录信息
	 * @param login
	 * @return
	 */
	List<GuesssFirstLogin> findLogin(GuesssFirstLogin login);
}