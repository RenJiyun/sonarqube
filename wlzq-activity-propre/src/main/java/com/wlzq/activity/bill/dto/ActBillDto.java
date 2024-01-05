package com.wlzq.activity.bill.dto;

import com.wlzq.activity.bill.model.ActBill;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 2021年度账单
 * @author jjw
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class ActBillDto extends ActBill{
	//private Integer openDateDays;	//开户天数
	//private List<String> wishs;	//愿望标签
}
