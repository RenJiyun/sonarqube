/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.redeem.dao;

import com.wlzq.activity.redeem.model.ActActivityPrize;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动奖品表DAO接口
 * @author cjz
 * @version 2018-09-28
 */
@MybatisScan
public interface ActActivityPrizeDao extends CrudDao<ActActivityPrize> {
	
}