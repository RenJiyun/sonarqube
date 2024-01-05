package com.wlzq.activity.virtualfin.dto;

import java.util.List;

import com.wlzq.activity.virtualfin.model.ActGoodsFlow;

import lombok.Data;

@Data
public class ActGoodsFlowDto {
	private List<ActGoodsFlow> info;
	private Double totalIncome;
	private Double totalExpand;
}
