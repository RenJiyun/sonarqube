package com.wlzq.activity.actWL20.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wlzq.activity.actWL20.model.ActSubscribe;

import lombok.Data;

/**
 * 
 * @author jjw
 *
 */
@Data
public class ActSubscribeDto {	
	private Integer status;		// 状态,0:未订阅1：已订阅
	@JsonIgnore
	private ActSubscribe actSubscribe;
}
