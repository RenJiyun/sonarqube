package com.wlzq.activity.virtualfin.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author luohc
 * @date 2021/7/20 9:37
 */
@Data
public class LastAmountFlowResDto {
    private String mobile;
    private BigDecimal expAmount;
    private BigDecimal withdrawAmount;

}
