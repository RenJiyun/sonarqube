/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess.dao;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.guess.model.GuesssUser;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 猜涨跌用户DAO接口
 * @author louie
 * @version 2018-05-21
 */
@MybatisScan
public interface GuesssUserDao extends CrudDao<GuesssUser> {
	GuesssUser findByUserId(@Param("userId") String userId, @Param("activityCode")String activityCode);

    /**
     * 参与活动的用户总数
     */
    long count(@Param("activityCode") String activityCode);
}
