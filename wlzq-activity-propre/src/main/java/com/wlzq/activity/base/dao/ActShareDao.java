/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.ActShare;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动分享信息DAO接口
 * @author louie
 * @version 2018-11-30
 */
@MybatisScan
public interface ActShareDao extends CrudDao<ActShare> {
	int findCount(ActShare share);
}