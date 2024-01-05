package com.wlzq.activity.actWL20.dto;

import com.wlzq.activity.base.model.ActPrize;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 我的奖品dto
 * @author 
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MyPrizeDto2 extends ActPrize{	
	
	private Integer redenvelopeAmt;	//红包金额,单位分
	private String redenvelopeUrl;	//红包链接
}

