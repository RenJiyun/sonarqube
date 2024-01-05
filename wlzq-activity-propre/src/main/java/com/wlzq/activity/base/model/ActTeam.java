package com.wlzq.activity.base.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-07-24
 */
@Data
public class ActTeam {
	
	public static final Integer SUCCESS = new Integer(0);
	public static final Integer FAIL = new Integer(1);
	public static final Integer FORMING = new Integer(2);
	public static final Integer IS_TEAM_LEAD = new Integer(1);
	public static final Integer NOT_TEAM_LEAD = new Integer(0);
	public static final Integer HAS_HELP = new Integer(1);
	public static final Integer NOT_HAS_HELP = new Integer(0);
	@JsonIgnore
	private Integer id;
	private String teamSerial;		//队伍序列号
	private String teamName;		//队伍名称
	@JsonIgnore
	private String template;		//队伍模板
	@JsonIgnore
	private String createUserId;	//队伍创建人用户ID
	@JsonIgnore
	private String createCustomerId;	//
	@JsonSerialize (using=Date2LongSerializer.class)
	private Date createDate;		//创建时间
	private Integer status;			//状态 0-失效，1-成功，2-组队中
	private String createUserName;	//创建用户名
	private String createNickName;	//创建人微信昵称
	private String createPortrait;	//创建人头像
	private Integer isTeamLead = NOT_TEAM_LEAD;		//是否队长,0-否，1-是
	private Integer hasLight = NOT_HAS_HELP;			//是否已点亮,0-否，1-是
	@JsonIgnore
	private String openId;			//第三方opendId
	
	private List<ActTeamMember> memberNameList;	//组队成员列表
	
	public ActTeam() {
		
	}
	
	public static ActTeam createNewTeam(String teamSerial,String teamName, String template, String userId, String customerId, Date createDate, Integer status) {
		ActTeam actTeam = new ActTeam();
		actTeam.setTeamSerial(teamSerial);
		actTeam.setTeamName(teamName);
		actTeam.setCreateCustomerId(customerId);
		actTeam.setCreateUserId(userId);
		actTeam.setCreateDate(createDate);
		actTeam.setStatus(status);
		actTeam.setTemplate(template);
		return actTeam;
	}
}
