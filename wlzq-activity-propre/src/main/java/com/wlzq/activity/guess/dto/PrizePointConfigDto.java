package com.wlzq.activity.guess.dto;

import lombok.Data;

/**
 * 奖品的积分价值，即：多少积分可以兑换奖品
 */
@Data
public class PrizePointConfigDto {
    /*奖品编码*/
    private String prizeType;
    /*积分*/
    private Long point;
}
