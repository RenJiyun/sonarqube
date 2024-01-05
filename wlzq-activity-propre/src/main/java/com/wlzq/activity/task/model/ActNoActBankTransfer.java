package com.wlzq.activity.task.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 非有效户入金流水
 *
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActNoActBankTransfer {
    /** 交期日期, 格式为yyyyMMdd */
    private String initDate;
    /** 客户号 */
    private String clientId;
    /** 发生金额 */
    private String occurBalance;
}
