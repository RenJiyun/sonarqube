package com.wlzq.activity.push.dto;

import lombok.Data;

/**
 * @author: qiaofeng
 * @date: 2022/5/5 16:24
 * @description:
 */
@Data
public class RenewedReceivedNoticeDto {
    /**
     * 客户号
     */
    private String customerId;
    /**
     * 手机号
     */
    private String mobile;
}
