package com.wlzq.activity.bill.dto;

import com.wlzq.activity.bill.model.ActBill2023;
import lombok.Data;

import java.util.Map;

/**
 * @author renjiyun
 */
@Data
public class ActBill2023Dto {
    /** 客户年度账单 */
    private ActBill2023 clientBill;
    /** 公共信息 */
    private Map<String, Object> common;
}
