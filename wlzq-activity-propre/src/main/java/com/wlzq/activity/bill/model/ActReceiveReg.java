package com.wlzq.activity.bill.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActReceiveReg {
    /** 已登记 */
    public static final Integer STATUS_REG = 1;
    /** 已领取 */
    public static final Integer STATUS_RECEIVED = 2;
    /** 领取失败 */
    public static final Integer STATUS_RECEIVE_FAIL = 3;

    private Long id;
    /** 用户 id */
    private String userId;
    /** 客户 id */
    private String customerId;
    /** 手机号 */
    private String mobile;
    /** 活动编码 */
    private String activityCode;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}
