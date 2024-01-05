package com.wlzq.activity.task.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class VasPayAgreement {
    private String productCode;
    private String customerId;
    private String status;
    private String periodType;
    private String mobile;
    private Date createTime;
}
