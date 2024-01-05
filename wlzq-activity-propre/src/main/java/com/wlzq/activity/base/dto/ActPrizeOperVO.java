package com.wlzq.activity.base.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhaozx
 * @date 2022/01/18 17:29
 */
@Data
@Accessors(chain = true)
public class ActPrizeOperVO implements Serializable {
	public static final Integer OPER_FLAG_UNABLE = 0;
	public static final Integer OPER_FLAG_RECEIVE = 1;
	public static final Integer OPER_FLAG_UPD = 2;
	public static final Integer OPER_HAD_RECEIVE = 3;
    private Integer  operFlag ;		// 0-不可操作，1-可领取，2-可更新  3已领取(而且不能再次领取)
    private String	code;
    private String redeemCode;

	private String lastActivityCode;		// 上次领取的活动编码
	private String lastCustomerId; // 上次领取客户号

}
