package com.wlzq.activity.task.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActReturnVisitRecord {
    private String rvTaskNo;
    private String customerId;
    private Date finishTime;
    private Integer recordStatus;
}
