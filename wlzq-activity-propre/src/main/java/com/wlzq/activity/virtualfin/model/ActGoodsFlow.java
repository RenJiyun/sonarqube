package com.wlzq.activity.virtualfin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import com.wlzq.core.Page;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 活动物品流水
 * @author zhaozx
 * @version 2020-07-28
 */
@Data
@Accessors(chain = true)
public class ActGoodsFlow {

	/** 获得 */
	public static final Integer FLOW_FLAG_GET = 1;
	/** 消耗 */
	public static final Integer FLOW_FLAG_CONSUME = 2;
	@JsonIgnore
	private String id;
	/** 手机号 */
	private String mobile;
	/** 用户号 */
	private String userId;
	/** openid */
	private String openId;
	/** 客户号 */
	private String customerId;
	/** 标志，1-获得，2-消耗 */
	private Integer flag;
	/** 任务代码 */
	private String taskCode;
	/** 任务代码列表 */
	@JsonIgnore
	private List<String> taskCodeList;
	/** 活动代码 */
	private String activityCode;
	/** 任务名称 */
	private String taskName;
	/** 产品代码 */
	private String productCode;
	/** 产品名称 */
	private String productName;
	/** 物品代码 */
	private String goodsCode;
	/** 物品名称 */
	private String goodsName;
	/** 物品数量 */
	private Double goodsQuantity;
	/** 已使用物品数量 */
	private Double usedGoodsQuantity;
	/** 订单Id */
	private String orderId;
	/** 业务编码（阅读文章id） */
	private String bizCode;
	/** 推荐人手机号 */
	private String recommendMobile;
	/** 渠道 */
	private String source;

	@JsonSerialize(using=Date2LongSerializer.class)
	private Date createTime;
	@JsonIgnore
	private Date updateTime;
	@JsonIgnore
	private Integer isDeleted;
	@JsonIgnore
	private Page page;

	@JsonIgnore
	private Date createTimeStart;
	@JsonIgnore
	private Date createTimeEnd;

}
