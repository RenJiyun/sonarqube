package com.wlzq.activity.base.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActListDto {
    /** 活动数量 */
    private int count;
    /** 活动列表 */
    private List<ActivityDto> actList;
}
