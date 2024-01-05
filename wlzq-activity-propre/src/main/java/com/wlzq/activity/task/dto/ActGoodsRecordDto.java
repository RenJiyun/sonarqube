package com.wlzq.activity.task.dto;

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
public class ActGoodsRecordDto {
    private String id;
    /** 物品编码 */
    private String goodsCode;
    /** 物品数量 */
    private Long goodsQuantity;
    /** 活动编码 */
    private String activityCode;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private Date createTime;
}
