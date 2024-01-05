package com.wlzq.activity.guess.dto;

import lombok.Data;

/**
 * 积分配置
 */
@Data
public class GuessPointConfigDto {
    /** 首次登录猜涨跌活动获得积分*/
    private  Integer firstPoint = 100;
    /** 每日登录获取积分*/
    private  Integer loginPoint = 30;
    /** 竞猜最少使用积分*/
    private Integer guessMinPoint = 10;
    /** 竞猜最高使用积分*/
    private Integer guessMaxPoint = 1_0000;
}
