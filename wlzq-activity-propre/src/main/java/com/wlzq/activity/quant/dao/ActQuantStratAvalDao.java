/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.quant.dao;

import com.wlzq.activity.quant.model.ActQuantStratAval;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 建模大赛策略评估DAO接口
 * @author zhaozx
 * @version 2020-10-28
 */
@MybatisScan
public interface ActQuantStratAvalDao extends CrudDao<ActQuantStratAval> {
	
}