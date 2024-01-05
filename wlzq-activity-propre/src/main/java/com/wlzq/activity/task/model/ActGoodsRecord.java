package com.wlzq.activity.task.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 活动物品流水记录
 *
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActGoodsRecord {
    /** 获得 */
    public static final Integer FLOW_FLAG_GET = 1;
    /** 消耗 */
    public static final Integer FLOW_FLAG_CONSUME = 2;

    private String id;
    /** 用户 id */
    private String userId;
    /** openId */
    private String openId;
    /** 手机号 */
    private String mobile;
    /** 客户号 */
    private String customerId;
    /** 物品编码 */
    private String goodsCode;
    /** 物品数量 */
    private Long goodsQuantity;
    /** 标志，1-获得，2-消耗 */
    private Integer flag;
    /** 活动编码 */
    private String activityCode;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date createTime;
}
