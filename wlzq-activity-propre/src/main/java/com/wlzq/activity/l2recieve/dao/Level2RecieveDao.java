/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.l2recieve.model.Level2Recieve;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * level2领取活动领取记录DAO接口
 * @author louie
 * @version 2018-05-03
 */
@MybatisScan
public interface Level2RecieveDao extends CrudDao<Level2Recieve> {
	/**
	 * 查询非邀请领取情况
	 * @param mobile
	 * @return
	 */
	List<Level2Recieve> findStatus(String mobile);
	/**
	 * 查询未开户状态领取记录
	 * @param start
	 * @param end
	 * @return
	 */
	List<Level2Recieve> findNotActive(@Param("start")Integer start,@Param("end")Integer end);
}