package com.wlzq.activity.base.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author luohc
 * @date 2023/3/29 9:58
 */
@Data
@Accessors(chain = true)
public class ActivityInfoDto {

    private String activityCode;

    /**
     * 活动中一个客户号可以对应几个手机号
     */
    private Integer customerIdVsMobile;

}
