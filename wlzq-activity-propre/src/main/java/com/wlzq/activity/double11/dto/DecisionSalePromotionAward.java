package com.wlzq.activity.double11.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 决策销售推动奖实体
 */
@Data
public class DecisionSalePromotionAward extends Double11AchievementDto{
    private final Integer achievementType = 5;
    private Integer sort;//排序
    private Integer reward;//拟获得奖励
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date completionTime;//完成时间
    private String recommendOfficeName;//所属分支机构
    private String recommendOfficeId;//所属分支机构编号
    private Double progress;//完成进度
}
