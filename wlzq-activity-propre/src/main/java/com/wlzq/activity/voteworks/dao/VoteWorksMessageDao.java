/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.voteworks.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.voteworks.model.VoteWorksHot;
import com.wlzq.activity.voteworks.model.VoteWorksMessage;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 留言DAO接口
 * @author louie
 * @version 2018-08-10
 */
@MybatisScan
public interface VoteWorksMessageDao extends CrudDao<VoteWorksMessage> {
	/**
	 * 总数
	 * @param tag TODO
	 * @return
	 */
	Integer count(@Param("no")String no, @Param("tag")Integer tag, @Param("activitycode") String activitycode);
	/**
	 * 留言
	 * @param no
	 * @param tag TODO
	 * @param start
	 * @param end
	 * @param beginId
	 * @return
	 */
	List<VoteWorksMessage> findMessages(@Param("no")String no,@Param("tag")Integer tag,@Param("activitycode") String activitycode,@Param("minId")Long minId,@Param("start")Integer start, @Param("end")Integer end);
	
	List<VoteWorksMessage> findList(@Param("msg") VoteWorksMessage msg, @Param("activitycode") String activitycode);
	
	List<VoteWorksMessage> findAllList(@Param("msg") VoteWorksMessage msg, @Param("activitycode") String activitycode);
}