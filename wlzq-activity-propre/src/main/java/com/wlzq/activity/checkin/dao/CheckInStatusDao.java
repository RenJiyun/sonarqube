/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.checkin.dao;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.checkin.model.CheckInStatus;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 签到状态DAO接口
 * @author louie
 * @version 2018-03-14
 */
@MybatisScan
public interface CheckInStatusDao extends CrudDao<CheckInStatus> {
	/**
	 * 签到状态查询
	 * @param userId
	 * @return
	 */
	CheckInStatus findByUserId(@Param("userId")String userId);
	
	/**
	 * 签到状态查询
	 * @param openid
	 * @return
	 */
	CheckInStatus findByOpenId(@Param("openid")String open);
	/**
	 * 签到人数
	 * @return
	 */
	Long checkInCount();
}