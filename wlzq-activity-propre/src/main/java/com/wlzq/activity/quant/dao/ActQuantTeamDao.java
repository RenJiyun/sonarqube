/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.quant.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.quant.model.ActQuantTeam;
import com.wlzq.activity.quant.model.ActQuantTeamStratDto;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.Page;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 建模大赛队伍管理DAO接口
 * @author zhaozx
 * @version 2020-10-28
 */
@MybatisScan
public interface ActQuantTeamDao extends CrudDao<ActQuantTeam> {
	
	List<ActQuantTeamStratDto> findTeamStratDto(@Param("teamId") String teamId, @Param("leader")String leader, 
			@Param("orderType")Integer orderType, @Param("accountType")Integer accountType, @Param("backTraceDateStart") String backTraceDateStart, @Param("backTraceDateEnd") String backTraceDateEnd, @Param("page") Page page);
	
	List<ActQuantTeam> findHotteams(@Param("teamIdOrLeader") String teamIdOrLeader, @Param("page") Page page);
}