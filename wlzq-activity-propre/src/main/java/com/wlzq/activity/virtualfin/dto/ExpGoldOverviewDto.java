package com.wlzq.activity.virtualfin.dto;

import java.util.List;

import com.wlzq.activity.virtualfin.model.ActTask;

import lombok.Data;

@Data
public class ExpGoldOverviewDto {
	
	private String mobile;
	private String activityCode;
	private Double expGold;
	private Double redEnvelope;
	private Integer isFirstLogin;
	private List<ActTask> tasks;
//	private List<ActFinProduct> products;

}
