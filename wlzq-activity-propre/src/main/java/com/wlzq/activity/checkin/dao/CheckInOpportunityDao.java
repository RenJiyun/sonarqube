/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.checkin.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.checkin.model.CheckInOpportunity;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 签到补签机会DAO接口
 * @author louie
 * @version 2018-03-14
 */
@MybatisScan
public interface CheckInOpportunityDao extends CrudDao<CheckInOpportunity> {
	/**
	 * 未使用补签机会查询
	 * @param openid
	 * @return
	 */
	List<CheckInOpportunity> findOpportunities(@Param("openid")String openid);
	/**
	 * 时间查询补签卡机会
	 * @param openid
	 * @param timeStart
	 * @param timeEnd
	 * @param status
	 * @return
	 */
	List<CheckInOpportunity> findByTime(@Param("openid")String openid,
			@Param("timeStart")Date timeStart,@Param("timeEnd")Date timeEnd,@Param("status")Integer status);
}