/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.advertising.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * 收集投放广告手机号记录Entity
 * @author pjw
 * @version 2021-07-16
 */
@Data
public class ActAdvertising{

	@JsonIgnore
	private String id;
	@JsonIgnore
	private String mobile;		// 手机号码
	@JsonIgnore
	private String pageEncoding;		 // 页面编码    1=智能条件, 2=小白理财,  3=新人福利, 4=诊股   5=新人弹窗,6=万山红,7=7周年庆活动
	@JsonIgnore
	private Date createTime;		// 创建时间
	@JsonIgnore
	private Date updateTime;		// 修改时间
	@JsonIgnore
	private String remarks;       //备注信息
	@JsonIgnore
	private String marketAccount;       //营销人员账号
	@JsonIgnore
	private Integer followStatus;       //跟进状态(0=待跟进、1=跟进中、2=已开户、3=未接听、4=已终止)
	@JsonIgnore
	private String marketMobile;       //营销人员手机号
	@JsonIgnore
	private String pgm;       //备注
	@JsonIgnore
	private String kw;       //推广词
	@JsonIgnore
	private String grp;       //推广组
	@JsonIgnore
	private String pln;       //推广计划
	@JsonIgnore
	private String chn;       //广告渠道
	

	
}