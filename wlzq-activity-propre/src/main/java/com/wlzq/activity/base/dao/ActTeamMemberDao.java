package com.wlzq.activity.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.model.ActTeam;
import com.wlzq.activity.base.model.ActTeamMember;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

@MybatisScan
public interface ActTeamMemberDao extends CrudDao<ActTeamMember> {
	
	List<ActTeamMember> findByTeamSerial(@Param("teamSerial") String teamSerial, @Param("customerId") String customerId);
	
	Integer deleteByTeamSerial(String teamSerial);
	
	List<ActTeamMember> findAutoList(@Param("actTeamList") List<ActTeam> actTeamList,@Param("rownum") Integer rownum);

	List<ActTeamMember> findByTemplateAndOpenId(@Param("template")String template, @Param("openId")String openId);
	
}
