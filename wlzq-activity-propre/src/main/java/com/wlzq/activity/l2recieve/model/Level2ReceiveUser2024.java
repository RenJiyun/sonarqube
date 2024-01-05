/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.model;

import lombok.Data;

import java.util.Date;

/**
 * @author renjiyun
 */
@Data
public class Level2ReceiveUser2024 {
    /** 客户号 */
    private String customerId;
    /** 手机号 */
    private String mobile;
    /** 类型: 4-开通预约打新, 5-开通北交所权限, 6-开通科创板权限 */
    private Integer type;
    /** 生效时间 */
    private Date effectiveDate;
    /** 写入时间 */
    private Date createTime;
    /** 生效开始时间 (查询使用) */
    private Date effectiveDateBegin;
    /** 生效结束时间 (查询使用) */
    private Date effectiveDateEnd;
}