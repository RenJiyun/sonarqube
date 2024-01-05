package com.wlzq.activity.push.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author luohc
 * @date 2022/11/3 14:23
 *
 * 员工推送对象
 */
@Data
@Accessors(chain = true)
public class StaffPushDto {

    /***
     * ekp 名称
     */
    private String ekpName;
    private String orderNo;

    /***
     * 微信openid
     */
    private String openId;
    /***
     * 手机号
     */
    private String recommendMobile;

    private String customerName;

}
