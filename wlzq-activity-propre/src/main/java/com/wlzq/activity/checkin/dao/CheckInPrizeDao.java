/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.checkin.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.checkin.model.CheckInPrize;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;


/**
 * 签到记录DAO接口
 * @author louie
 * @version 2018-03-14
 */
@MybatisScan
public interface CheckInPrizeDao extends CrudDao<CheckInPrize> {
	
	/**
	 * 奖品查询
	 * @param openid
	 * @return
	 */
	List<CheckInPrize> findPrize(@Param("openid")String openid,
			@Param("timeStart")Date timeStart,@Param("timeEnd")Date timeEnd);
	
}