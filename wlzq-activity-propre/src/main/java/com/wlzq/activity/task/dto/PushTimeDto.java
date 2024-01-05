package com.wlzq.activity.task.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author luohc
 * @date 2022/2/17 10:16
 */
@Data
@Accessors(chain = true)
public class PushTimeDto {

    private Date startTime;
    private Date endTime;

    private String productCode;
    private String payTimeEnd;

}
