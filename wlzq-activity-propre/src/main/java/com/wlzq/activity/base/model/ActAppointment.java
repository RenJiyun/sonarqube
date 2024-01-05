package com.wlzq.activity.base.model;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-07-23
 */
@Data
public class ActAppointment {
	
	private Integer id;
	private String activityCode;		// 活动编码
	private String appointmentCode;		//预约代码
	private List<String> appointmentCodeList;	// 预约代码列表，用于查询
	private String appointmentName;		//预约名称
	private String userId;				//用户ID
	private String customerId;			//客户ID
	private String phone;				//手机号码
	private String mobile;				//手机号码
	private Date createDate;			//创建日期
	private String openId;				// openId
	private Date appointmentTime;		//预约时间
	private String reachType;			//1：短信，2，微信，3:app，4：企业微信，5：微店, 逗号分隔
	private Date updateTime;
	private Integer status;				//状态：0：未处理，1：已处理
}
