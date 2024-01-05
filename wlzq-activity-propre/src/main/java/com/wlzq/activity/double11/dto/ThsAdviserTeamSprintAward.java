package com.wlzq.activity.double11.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 同花顺投顾团队冲刺奖实体
 */
@Data
public class ThsAdviserTeamSprintAward extends Double11AchievementDto {
    private final Integer achievementType = 3;
    private Integer sort;//排序
    private Integer reward;//拟获得奖励
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date bookTime;//订单时间
    private String productName;//投顾产品
    private String orderId;//订单号
    private Double amount;//订单订阅金额（元）

}
