package com.wlzq.activity.task.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ActTaskResDto {

    /** 任务开始时间（时间戳，精确到毫秒 */
    private Long endDate;

    /** 任务结束时间（时间戳，精确到毫秒 */
    private Long beginDate;

    private List<ActTaskStatusDto> taskStatusDtos;

}
