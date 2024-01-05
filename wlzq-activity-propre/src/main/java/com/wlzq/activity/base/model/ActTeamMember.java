package com.wlzq.activity.base.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-07-24
 */
@Data
public class ActTeamMember {
	
	public static final Integer TYPE_MANUAL = new Integer(0);
	public static final Integer TYPE_AUTO = new Integer(1);
	@JsonIgnore
	private Integer id;
	@JsonIgnore
	private String teamSerial;		//队伍序列号
	@JsonIgnore
	private String userId;			//用户ID
	@JsonIgnore
	private String customerId;		//客户ID
	@JsonIgnore
	private Integer type;			//组队方式：0-人工，1-系统
	private Date createDate;		//创建时间
	private String nickName;		//微信昵称
	private String userName;		//客户的用户名称
	private String portrait;		//创建人头像
	private Integer position;		//位置
	@JsonIgnore
	private String openId;			//openId
	
	public static ActTeamMember createNewMember(String teamSerial, String customerId, String userId, Date createDate, Integer type) {
		ActTeamMember member = new ActTeamMember();
		member.setCustomerId(customerId);
		member.setTeamSerial(teamSerial);
		member.setUserId(userId);
		member.setCreateDate(createDate);
		member.setType(type);
		return member;
	}
}
