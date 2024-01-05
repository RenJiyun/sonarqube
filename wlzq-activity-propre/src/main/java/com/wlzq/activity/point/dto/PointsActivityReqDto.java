package com.wlzq.activity.point.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PointsActivityReqDto {
    private String userId;
    private String mobile;
    private String customerId;
    /*活动编码*/
    private String activityCode;
    /*奖品编码*/
    private String prizeType;
    /* 积分使用描述 */
    private String description;
}
