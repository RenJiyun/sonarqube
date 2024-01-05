package com.wlzq.activity.virtualfin.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author luohc
 * @date 2021/5/31 8:40
 */
@Data
@Accessors(chain = true)
public class TjdOpenResDto implements Serializable {
    /** 手机号 */
    private String mobile;
    /** 开通时间 */
    private Date openTime;
    private String customerId;
}
