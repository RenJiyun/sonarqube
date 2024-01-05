package com.wlzq.activity.turntable.model;

import java.util.List;

import lombok.Data;

@Data
public class TurntableeDice {
	
	private  Double minValue;
	private  Double maxValue;
	private  String prizeTypeCode;

	
	public static TurntableeDice getActLottery(List<TurntableeDice> list, Double random) {
		for (TurntableeDice each : list) {
			if (random.compareTo(each.minValue) >= 0 && random.compareTo(each.maxValue) == -1) {
				return each;
			}
		}
		return null;
	}
}
