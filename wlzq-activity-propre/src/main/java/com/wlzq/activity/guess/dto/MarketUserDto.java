package com.wlzq.activity.guess.dto;

import lombok.Data;

/**
 * 市场推广人员
 */
@Data
public class MarketUserDto {
    /*市场推广人员账号*/
    private String account;
    /*手机号*/
    private String marketMobile;
    /*负责渠道*/
    private String chn;

}
