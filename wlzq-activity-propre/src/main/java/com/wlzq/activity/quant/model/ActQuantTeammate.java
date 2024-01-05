/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.quant.model;

import java.util.Date;

import lombok.Data;

/**
 * 建模大赛队员表Entity
 * @author zhaozx
 * @version 2020-11-05
 */
@Data
public class ActQuantTeammate  {
	
	private String id;
	private String teamId;		// 队伍id
	private String name;		// 队员
	private String mobile;		// 手机号
	private String email;		// 学校邮箱
	private String professor;		// 指导老师
	private String professorMobile;		// 指导老师手机
	private String professorEmail;		// 指导老师邮箱
	private String professorTitle;		// 导师职称
	private String university;		// 学校
	private String department;		// 学院
	private String major;		// 专业
	private String education;		// 学历
	private Date createTime;
	private Date updateTime;
	private Integer isDeleted;
	
	
}