package com.wlzq.activity.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.model.ActMessage;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动留言DAO接口
 * @author cjz
 * @version 2018-06-12
 */
@MybatisScan
public interface ActMessageDao extends CrudDao<ActMessage> {

	/**
	 * 
	 * @param activityCode
	 * @param maxOrder
	 * @param maxLength
	 * @return
	 */
	List<ActMessage> findListByMaxId(@Param("activityCode") String activityCode, @Param("maxOrder") Integer maxOrder, @Param("maxLength") Integer maxLength);
}