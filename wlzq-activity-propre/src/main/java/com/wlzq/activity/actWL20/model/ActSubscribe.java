package com.wlzq.activity.actWL20.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 
 * @author jjw
 *
 */
@Data
public class ActSubscribe {	
	@JsonIgnore
	private Integer id;
	private String activityCode;		//活动编码
	private String customerId;	//客户号
	private String mobile;		//手机号
	@JsonSerialize (using=Date2LongSerializer.class)
	private Date createDate;		//创建时间
}
