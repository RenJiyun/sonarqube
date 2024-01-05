/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.brokeragemeeting.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingVote;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 经纪业务会议游戏投票DAO接口
 * @author cjz
 * @version 2019-01-21
 */
@MybatisScan
public interface ActBrokeragemeetingVoteDao extends CrudDao<ActBrokeragemeetingVote> {
	
	int clearGameData();
	
	List<ActBrokeragemeetingVote> findWordCountResult(@Param("maxLength") Integer maxLength);
	
	List<Map<String, Object>> findFastResult(@Param("maxLength") Integer maxLength);
	
	Integer findPersonNum();
	
	List<Map<String, Object>> findFastRanking(@Param("userId") String userId);
}