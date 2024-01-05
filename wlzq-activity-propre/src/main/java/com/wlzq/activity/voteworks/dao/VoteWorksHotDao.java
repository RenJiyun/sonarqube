/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.voteworks.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.voteworks.model.VoteWorksHot;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 页面热度DAO接口
 * @author louie
 * @version 2018-08-10
 */
@MybatisScan
public interface VoteWorksHotDao extends CrudDao<VoteWorksHot> {
	/**
	 * 作品编号查找热度
	 * @param no
	 * @param tag TODO
	 * @return
	 */
	VoteWorksHot findByNo(@Param("no")String no, @Param("tag")Integer tag, @Param("activitycode")String activitycode);
	
	long hotAll(@Param("activitycode")String activitycode);
	
	/**
	 * 作品热度加1
	 * @param no
	 * @return
	 */
	int hotAdd(VoteWorksHot hot);
	
	List<VoteWorksHot> findList(@Param("hot") VoteWorksHot hot, @Param("activitycode") String activitycode);
	
	List<VoteWorksHot> findAllList(@Param("hot") VoteWorksHot hot, @Param("activitycode") String activitycode);
}