/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.quant.model;

import java.util.Date;

import lombok.Data;

/**
 * 建模大赛队伍点赞Entity
 * @author zhaozx
 * @version 2020-10-28
 */
@Data
public class ActQuantTeamLike {
	
	private String id;
	private String mobile;		// 手机号
	private String userId;		// 用户id
	private String openId;		// openId
	private String teamId;		// 队伍Id
	private Date likeTime;		// 运行时间
	private Date createTime;		// create_time
	private Date updateTime;		// update_time
	private Integer isDeleted;		// is_deleted
	private Date dateFrom;
	private Date dateTo;
	
}