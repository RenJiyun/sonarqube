package com.wlzq.activity.double11.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 一马当先奖Dto
 * @author user
 */
@Data
public class SaleRankingDto {
    /**排序*/
    private Integer sort;
    /**拟获得奖励*/
    private Integer reward;
    /**营销人员*/
    private String recommendName;
    /**营销人员手机*/
    private String recommendMobile;
    /**所属分支机构*/
    private String shortName;
    private String branchNo;
    /**积分*/
    private Double goodsQuantity;
}
