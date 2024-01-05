/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.CheckList;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动审核名单DAO接口
 * @author louie
 * @version 2018-05-09
 */
@MybatisScan
public interface CheckListDao extends CrudDao<CheckList> {
	
}