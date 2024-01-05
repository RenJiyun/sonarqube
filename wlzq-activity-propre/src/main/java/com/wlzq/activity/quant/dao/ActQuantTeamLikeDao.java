/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.quant.dao;

import com.wlzq.activity.quant.model.ActQuantTeamLike;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 建模大赛队伍点赞DAO接口
 * @author zhaozx
 * @version 2020-10-28
 */
@MybatisScan
public interface ActQuantTeamLikeDao extends CrudDao<ActQuantTeamLike> {
	
}