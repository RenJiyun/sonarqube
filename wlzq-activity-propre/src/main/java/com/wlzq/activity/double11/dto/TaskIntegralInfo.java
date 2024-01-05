package com.wlzq.activity.double11.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 任务积分明细
 * @author user
 */
@Data
public class TaskIntegralInfo {
    /**用户手机*/
    private String mobile;
    /**任务编码*/
    private String taskCode;
    /**活动编码*/
    private String activityCode;
    /**积分*/
    private Double goodsQuantity;
    /**积分事项*/
    private String detail;
    /**积分*/
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date createTime;
}
