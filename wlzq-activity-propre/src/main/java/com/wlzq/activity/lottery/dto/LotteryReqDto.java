package com.wlzq.activity.lottery.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author luohc
 * @date 2023/3/27 14:22
 */
@Data
@Accessors(chain = true)
public class LotteryReqDto {

    private String activityCode ;
    private String userId;
    private String mobile;
    private String customerId;

    private String openId;


}
