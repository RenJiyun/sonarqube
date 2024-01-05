/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.checkin.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.checkin.model.CheckIn;
import com.wlzq.activity.checkin.model.CheckInStatistic;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;


/**
 * 签到记录DAO接口
 * @author louie
 * @version 2018-03-14
 */
@MybatisScan
public interface CheckInDao extends CrudDao<CheckIn> {
	/**
	 * 签到记录查询
	 * @param userId
	 * @return
	 */
	List<CheckIn> findByUserId(@Param("userId")String userId);
	
	/**
	 * 签到记录查询
	 * @param openid
	 * @param timeStart
	 * @param timeEnd
	 * @return
	 */
	List<CheckIn> findByOpenId(@Param("openid")String openid,
			@Param("timeStart")Date timeStart,@Param("timeEnd")Date timeEnd);

	/**
	 * 签到记录查询
	 * @param openid
	 * @param timeStart
	 * @param timeEnd
	 * @return
	 */
	List<CheckIn> findByTime(@Param("openid")String openid,
			@Param("timeStart")Date timeStart,@Param("timeEnd")Date timeEnd);
	
	/**
	 * 
	 * @param openid
	 * @param timeStart
	 * @param checkInTime
	 * @return
	 */
	CheckInStatistic findContinuousCount(@Param("openid")String openid,@Param("timeStart")String timeStart,
			@Param("checkInTime")Date checkInTime);
}