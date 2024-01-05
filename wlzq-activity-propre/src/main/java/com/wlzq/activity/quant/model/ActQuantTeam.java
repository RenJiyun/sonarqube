/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.quant.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 建模大赛队伍管理Entity
 * @author zhaozx
 * @version 2020-10-28
 */
@Data
public class ActQuantTeam  {
	
	private String id;
	private String teamId;		// 队伍id
	private String leader;		// 队长名
	@JsonIgnore
	private String leaderMobile;		// 队长手机号
	private String leaderEmail;		// 队长邮箱
	private String teammates;		// 队员
	private List<String> teammateList;
	private String professor;		// 教授名
	@JsonIgnore
	private String professorMobile;		// 教授手机号
	@JsonIgnore
	private String professorEmail;		// 教授邮箱
	private String university;		// 大学名
	private String universityFull;		// 大学全名
	private String department;		// 学院
	private String major;		// 专业
	private String education;		// 学历
	@JsonIgnore
	private String thsAccount;		// 同花顺账户
	@JsonIgnore
	private String dkAccount;		// 点宽账号
	private Date createTime;		// create_time
	private Date updateTime;		// update_time
	private Integer isDeleted;		// is_deleted
	
	private Integer totalVoteCount;			// 总票数
	private Integer order;
	
}