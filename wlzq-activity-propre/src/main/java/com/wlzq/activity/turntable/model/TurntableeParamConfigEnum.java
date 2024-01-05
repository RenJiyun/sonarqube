package com.wlzq.activity.turntable.model;

import com.wlzq.common.utils.ObjectUtils;

public enum TurntableeParamConfigEnum {
	
	SPRING2021("ACTIVITY.SPRING2021.TURNTABLE", "activity.spring2021.turntable");
	
	private String activity;
	private String configKey;
	
	private TurntableeParamConfigEnum(String activity, String configKey) {
		this.activity = activity;
		this.configKey = configKey;
	}
	
	
	
	public String getActivity() {
		return activity;
	}
	
	public void setActivity(String activity) {
		this.activity = activity;
	}
	
	public String getConfigKey() {
		return configKey;
	}
	
	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}
	
	public static TurntableeParamConfigEnum getByActivity(String activity) {
		if (ObjectUtils.isEmptyOrNull(activity)) {
			return null;
		}
		for (TurntableeParamConfigEnum each : TurntableeParamConfigEnum.values()) {
			if (activity.equals(each.getActivity())) {
				return each;
			}
		}
		return null;
	}

}
