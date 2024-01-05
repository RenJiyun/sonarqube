package com.wlzq.activity.base.dto;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 跑马灯dto
 * @author 
 * @version 1.0
 */
@Data
public class ShowPrizeDto {
	/** 类型,1:兑换码，2：京东卡，3：积分 */
	private Integer type;
	/** 价值 */
	private Double worth;
	/** 奖品名称 */
	private String prizeName;
	/** 昵称 */
	private String nickName;
	/** 获取时间 */
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date createTime;
}

