package com.wlzq.activity.virtualfin.dto;

import com.wlzq.activity.virtualfin.model.ActRedEnvelope;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActRedEnvelopeDto {
	private List<ActRedEnvelope> info;
	private BigDecimal totalIncome;
	private BigDecimal totalExpand;
}
