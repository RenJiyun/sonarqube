package com.wlzq.activity.task.model;

import lombok.Data;

import java.util.Date;

/**
 * @author renjiyun
 */
@Data
public class DataDate {
    /** 数据类型: 1-持仓明细数据, 2: 北交所权限开通, 3: 非有效户入金流水 */
    private Integer type;

    /** 数据结束日期 */
    private Date dataDate;

    /** 数据开始日期 */
    private Date dataDateStart;

}
