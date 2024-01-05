package com.wlzq.activity.double11.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 决策销售传播奖实体
 */
@Data
public class DecisionSaleCommunicationAward extends Double11AchievementDto {
    private final Integer achievementType = 6;
    public static final String DECISION_SALE_AWARD_UPDATE_TIME = "DECISION_SALE_AWARD_UPDATE_TIME";
    private Integer sort;//排序
    private Integer reward;//拟获得奖励
    private Integer orderCount;//累计订单
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date payTime;//支付时间
    private String recommendName;//营销人员
    private String recommendMobile;//营销人员手机
    private String recommendOfficeName;//所属分支机构
    private String recommendOfficeId;//所属分支机构编号
}
