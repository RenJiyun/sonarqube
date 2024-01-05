/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.dao;

import java.util.List;

import com.wlzq.activity.guess51.model.GuessLoginRecord;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 当天首次登录信息DAO接口
 * @author louie
 * @version 2018-05-22
 */
@MybatisScan
public interface GuessLoginRecordDao extends CrudDao<GuessLoginRecord> {
	/**
	 * 查询登录信息
	 * @param login
	 * @return
	 */
	List<GuessLoginRecord> findLogin(GuessLoginRecord login);
}