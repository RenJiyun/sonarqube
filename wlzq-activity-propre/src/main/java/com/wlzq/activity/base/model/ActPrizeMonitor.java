package com.wlzq.activity.base.model;

import lombok.Data;

@Data
public class ActPrizeMonitor {
    private Integer remainingQuantity; //奖品剩余数量
    private Integer remainingQuantityWarning;//剩余数量警戒值
    private String smsAlertPhoneNumber;//短信提示号码
    private String code;//奖品编码
    private String prizeName;//奖品名称
    private Integer sumQuantity;//奖品总数量
}
