package com.wlzq.activity.double11.dto;

import lombok.Data;

/**
 * 巅峰登顶奖Dto
 * @author user
 */
@Data
public class BranchRankingDto {
    /**排序*/
    private Integer sort;
    /**拟获得奖励*/
    private Integer reward;
    /**所属分支机构*/
    private String shortName;
    private String branchNo;
    /**积分*/
    private Double goodsQuantity;
}
