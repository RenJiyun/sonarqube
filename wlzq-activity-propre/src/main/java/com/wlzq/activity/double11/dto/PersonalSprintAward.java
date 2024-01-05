package com.wlzq.activity.double11.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 个人冲刺奖实体
 */
@Data
public class PersonalSprintAward extends Double11AchievementDto{
    private final Integer achievementType = 1;
    private Integer sort;//排序
    private Integer reward;//拟获得奖励
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date payTime;//成交时间
    private String productName;//投顾产品
    private String productCode;//产品编码
    private String recommendName;//营销人员
    private String recommendMobile;//营销人员手机
    private String recommendOfficeName;//所属分支机构
    private String recommendOfficeId;//所属分支机构编号
    private String outTradeNo;//订单号
    private Double totalFee;//订单实付金额（元）
}
