/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.l2recieve.model.Level2RecieveInvite;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * level2领取活动邀请记录DAO接口
 * @author louie
 * @version 2018-05-03
 */
@MybatisScan
public interface Level2RecieveInviteDao extends CrudDao<Level2RecieveInvite> {
	List<Level2RecieveInvite> findNotOpen(@Param("inviteMobile")String inviteMobile,@Param("start")Integer start,@Param("end")Integer end);
}