/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.l2recieve.model.Level2RecieveUser;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * level2领取活动新用户推送数据DAO接口
 * @author louie
 * @version 2018-05-03
 */
@MybatisScan
public interface Level2RecieveUserDao extends CrudDao<Level2RecieveUser> {
	/**
	 * 查询客户信息
	 * @param type
	 * @param mobile
	 * @return
	 */
	Level2RecieveUser findCustomer(@Param("type")Integer type,@Param("mobile")String mobile);
	
	/**
	 * 查询领取有效期的用户
	 * @param user
	 * @return
	 */
	List<Level2RecieveUser> findValidList(Level2RecieveUser user);
}