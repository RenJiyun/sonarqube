package com.wlzq.activity.double11.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 双11活动销售榜单
 */
@Data
public class Double11AchievementDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 榜单更新时间
     */
    private Long updateTime;
}

