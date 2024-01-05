package com.wlzq.activity.finsection.dto;

import java.util.List;

import com.wlzq.activity.base.model.ActTeam;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-07-24
 */
@Data
public class ActTeamDto {
	
	List<ActTeam> succesTeams;
	List<ActTeam> formingTeams;

}
