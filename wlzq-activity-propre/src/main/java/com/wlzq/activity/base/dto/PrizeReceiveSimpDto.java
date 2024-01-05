package com.wlzq.activity.base.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author luohc
 * @date 2021/7/13 17:27
 */
@Data
@Accessors(chain = true)
public class PrizeReceiveSimpDto implements Serializable {
    private String prizeType;		// 奖品类型
    private Integer status;		// 状态,1:未领取,2:已领取,3:已抢光 , 常量参考 CouponRecieveStatusDto
    private String prizeName;     //奖品名称
}
