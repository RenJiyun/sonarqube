/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.voteworks.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.voteworks.model.VoteWorks;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 投票作品DAO接口
 * @author louie
 * @version 2018-08-10
 */
@MybatisScan
public interface VoteWorksDao extends CrudDao<VoteWorks> {
	/**
	 * 编号查询作品信息
	 * @param no
	 * @param tag TODO
	 * @return
	 */
	VoteWorks findByNo(@Param("no")String no, @Param("tag")Integer tag, @Param("activitycode")String activitycode);
	/**
	 * 作品点赞数加1
	 * @param works
	 * @return
	 */
	int likeAdd(VoteWorks works);
	
	Long allLikeCount(@Param("activitycode") String activitycode);
	
	List<VoteWorks> ranking(@Param("activitycode") String activitycode, @Param("tag")Integer tag,@Param("start")Integer start, @Param("end")Integer end);
}