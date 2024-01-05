package com.wlzq.activity.double11.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 同花顺投顾团队爆发奖实体
 */
@Data
public class ThsAdviserTeamOutbreakAward extends Double11AchievementDto {
    private final Integer achievementType = 4;
    private Integer sort;//业绩排序
    private Integer reward;//拟获得奖励
    private String productName;//投顾产品
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date bookTime;//成交时间
    private Double cumulativePerformance;//当日累计业绩（元）
}
