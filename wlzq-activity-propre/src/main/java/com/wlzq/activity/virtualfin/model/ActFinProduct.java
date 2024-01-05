package com.wlzq.activity.virtualfin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 活动理财产品
 * @author zhaozx
 * @version 2020-07-28
 */
@Data
public class ActFinProduct {
	@JsonIgnore
	private String id;
	private String code;		// 产品代码
	private String activityCode;	// 活动编码
	@JsonIgnore
	private List<String> codeList;	// 产品代码列表，用于查寻
	private String name;		// 产品名称
	private Double profit;		// 收益率
	private Double minBuy;		// 起购金额（元）
	private Double maxBuy;		// 最大购买金额（元）
	private Integer period;		// 投资期限
	private Integer status;		// 状态
	private Integer sort;		// 排序
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date openDate;		// 产品名称
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date closeDate;		// 产品名称
	@JsonIgnore
	private Date createTime;
	@JsonIgnore
	private Date updateTime;
	@JsonIgnore
	private Integer isDeleted;
	private String activityName;	// 活动名称
	private String goodsCode;		// 物品代码
	private String goodsName;		// 物品名称

	private BigDecimal totalSold; //产品每天销售总额
	
	private List<ActAgreementRef> agreements;
}
