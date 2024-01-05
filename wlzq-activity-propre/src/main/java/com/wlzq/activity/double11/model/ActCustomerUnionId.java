package com.wlzq.activity.double11.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActCustomerUnionId {
    private String id;
    /** openId */
    private String openId;
    /** 手机号 */
    private String mobile;
    /** 客户号 */
    private String customerId;
    /** unionId */
    private String unionId;
    /** 创建时间 */
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date createTime;
}
