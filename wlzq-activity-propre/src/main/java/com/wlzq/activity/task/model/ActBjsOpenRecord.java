package com.wlzq.activity.task.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 北交所权限开通记录
 *
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActBjsOpenRecord {
    /** 交易日期, 格式为yyyyMMdd */
    private String initDate;
    /** 客户号 */
    private String clientId;
}
