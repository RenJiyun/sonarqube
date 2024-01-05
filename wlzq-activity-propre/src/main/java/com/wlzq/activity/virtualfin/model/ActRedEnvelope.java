package com.wlzq.activity.virtualfin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import com.wlzq.core.Page;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 活动红包
 * @author zhaozx
 * @version 2020-07-28
 */
@Data
@Accessors(chain = true)
public class ActRedEnvelope {
	
	public static final Integer FLOW_FLAG_GET = 1;		// 获得
	public static final Integer FLOW_FLAG_CONSUME = 2;	// 消耗
	
	@JsonIgnore
	private String id;
	private String mobile;		// 手机号
	private String userId;		// 用户Id
	private String openId;		// openid
	private String customerId;		// 客户号
	private Integer flag;		// 标识，1-购买,2-赎回
	private String activityCode;		// 活动编码
	private String businessCode;		// 业务编码
	private String businessName;		// business_name
	private String orderId;			// 订单号
	private Double quantity;		// 数量
	private Integer status;		// 状态，0-未处理，1-已处理
	private String redEnvelopeUrl;		// 红包链接
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date createTime;
	@JsonIgnore
	private Date updateTime;
	@JsonIgnore
	private Integer isDeleted;
	@JsonIgnore
	private Page page;

	//非数据字段
	private String redeemCode;

}
