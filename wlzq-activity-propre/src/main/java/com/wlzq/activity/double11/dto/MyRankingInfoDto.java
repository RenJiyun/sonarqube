package com.wlzq.activity.double11.dto;

import lombok.Data;

import java.util.List;

/**
 * 一马当先奖Dto
 * @author user
 */
@Data
public class MyRankingInfoDto {
    /**排序*/
    private Integer sort;
    /**积分*/
    private Double myGoodsQuantity;
    private Integer total;
    private List<TaskIntegralInfo> info;
}
