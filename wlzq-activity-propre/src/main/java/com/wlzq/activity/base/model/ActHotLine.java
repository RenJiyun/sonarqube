package com.wlzq.activity.base.model;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-07-24
 */
@Data
public class ActHotLine {
	private Integer id;
	private String branchName;		//分支机构名称
	private String specialist;		//专员名称
	private String moblie;			//移动电话
	private String landlinePhone;	//固定电话
	
}
