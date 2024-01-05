package com.wlzq.activity.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.model.ActTeam;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

@MybatisScan
public interface ActTeamDao extends CrudDao<ActTeam> {
	
	List<ActTeam> findSample(@Param("actTeam") ActTeam actTeam, @Param("rownum") Integer rownum);
	
	List<ActTeam> findList(@Param("actTeam") ActTeam actTeam, @Param("rownum") Integer rownum);
	
	List<ActTeam> findCreateTeam(@Param("teamSerial")String teamSerial, @Param("template") String template,
			@Param("userId") String userId, @Param("customerId") String customerId);
	
	Integer update(@Param("teamSerial") String teamSerial, @Param("status") Integer status, @Param("customerId") String customerId);

	ActTeam findMyCreateTeam(@Param("template") String template, @Param("customerId") String customerId);
}
