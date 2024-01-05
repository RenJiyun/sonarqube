package com.wlzq.activity.base.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: qiaofeng
 * @date: 2022/5/18 15:47
 * @description: 奖品每日限量领取
 */
@Data
public class ActPrizesDailyLimitDto implements Serializable {
    /*每日限量领取的奖品*/
    private String prizeType;
    /*每日限量领取多少张优惠券*/
    private Integer limit;
}
