package com.wlzq.activity.task.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class TaskPushDto implements Serializable {
	private static final long serialVersionUID = -1880411373522942517L;

	private String customerId;
	private String mobile;
	private String sendToKey;



}
