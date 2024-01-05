/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.Participate;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动参与信息DAO接口
 * @author louie
 * @version 2018-05-15
 */
@MybatisScan
public interface ParticipateDao extends CrudDao<Participate> {
	/**
	 * 参与人次计算
	 * @param participate
	 * @return
	 */
	Long findParticipateCount(Participate participate);
}