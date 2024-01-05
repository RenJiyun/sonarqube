package com.wlzq.activity.actWL20.model;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 
 * @author jjw
 *
 */
@Data
public class ActFundinGo {	
	
	public static Integer CUS_NOT_VALID = 1;	//不满足条件
	public static Integer CUS_VALID = 2;	//满足条件
	public static Integer CUS_RECIEVE = 3;	//已领券
	
	private Long id;
	private String customerId;	//客户号
	@JsonSerialize (using=Date2LongSerializer.class)
	private Date openDate;		//开户时间
	@JsonSerialize (using=Date2LongSerializer.class)
	private Date createTime;	//创建时间
	@JsonSerialize (using=Date2LongSerializer.class)
	private Date firstFundingDate;	//首次入金时间
	private BigDecimal amount;	//入金金额
	@JsonSerialize (using=Date2LongSerializer.class)
	private Date lastFundingDate;	//最近入金日期
	private Integer status;	//是否满足领券条件  1：不满足条件，2：满足条件，3：已领券
	@JsonSerialize (using=Date2LongSerializer.class)
	private Date updateTime;	//更新时间
	
}
