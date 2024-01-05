package com.wlzq.activity.springfestival.dto;

import lombok.Data;

@Data
public class LotteryPreviewDto {
	private Integer leftDays; //活动剩余天数
	private Integer left;
	private Integer hasLottery;
}
