package com.wlzq.activity.lottery.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LotteryInfoDto {
    private Integer todayLeft;
}
