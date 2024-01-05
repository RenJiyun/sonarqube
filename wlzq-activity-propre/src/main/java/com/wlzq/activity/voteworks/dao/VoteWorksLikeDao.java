/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.voteworks.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.voteworks.dto.LikeDto;
import com.wlzq.activity.voteworks.model.VoteWorksLike;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 点赞记录DAO接口
 * @author louie
 * @version 2018-08-10
 */
@MybatisScan
public interface VoteWorksLikeDao extends CrudDao<VoteWorksLike> {
	/**
	 * 点赞次数
	 * @param like
	 * @return
	 */
	Integer likeCount(@Param("like")VoteWorksLike like, @Param("activitycode")String activitycode);

	/**
	 * 点赞总人数
	 * @param no
	 * @param tag TODO
	 * @return
	 */
	Long likeTotalPeople(@Param("no")String no, @Param("activitycode")String activitycode, @Param("tag")Integer tag);
	/**
	 * 点赞记录
	 * @param no
	 * @param tag TODO
	 * @param start
	 * @param end
	 * @return
	 */
	List<LikeDto> likes(@Param("no")String no, @Param("tag")Integer tag, @Param("activitycode") String activitycode,@Param("start")Integer start, @Param("end")Integer end);
	
	List<VoteWorksLike> findList(@Param("like") VoteWorksLike hot, @Param("activitycode") String activitycode);
	
	List<VoteWorksLike> findAllList(@Param("like") VoteWorksLike hot, @Param("activitycode") String activitycode);
}