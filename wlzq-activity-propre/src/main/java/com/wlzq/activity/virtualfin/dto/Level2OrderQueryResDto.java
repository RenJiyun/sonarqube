package com.wlzq.activity.virtualfin.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author luohc
 * @date 2021/7/20 13:33
 */
@Data
@Accessors(chain = true)
public class Level2OrderQueryResDto {

    //0免费；1保证金支付；2支付宝支付；3微信支付；4免费活动；5后台操作；6兑换码；7新客户免费体验
    public static final String[] real_pay_modes = {"1","2","3"};


    private Integer id;
    private String orderNo;
    private String goodsCode;
    private String customerId;
    private Integer payStatus;
    private Integer orderStatus;
    private Date createTime;//订单时间
    private Date replyTime;//支付时间
    private String payMode;


}
