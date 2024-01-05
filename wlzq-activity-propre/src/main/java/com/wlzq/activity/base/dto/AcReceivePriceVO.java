package com.wlzq.activity.base.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author luohc
 * @date 2021/10/8 17:29
 */
@Data
@Accessors(chain = true)
public class AcReceivePriceVO implements Serializable {
    public static final Integer UNIQUE_MOBILE_AND_CUSTOMER = 1;
    public static final Integer UNIQUE_MOBILE_OR_CUSTOMER = 2;
    public static final Integer UNIQUE_MOBILE = 3;
    public static final Integer UNIQUE_CUSTOMER = 4;


    private String userId ;
    private String customerId ;
    private String activityCode ;
    private String prizeType ;//支持多个prizeType，以逗号分割

    /** 奖品金额 */
    private Integer prizeAmount ;

    //
    private String recommendCode;
    private String remark;
    private String openId;


    //非接口参数
    private String mobile;
    /** 多个奖品的时候，错误是否继续领取下一个 */
    private boolean continueFlag=false;
    /** 客户纬度来限制是否重复领取 */
    private boolean customerDimension=true;
    /** 手机号纬度来限制是否重复领取 */
    private boolean mobileDimension=false;   
    private Integer needUserId;




    /**  唯一类型 1 ： 手机号 + 客户号；   2 ： 手机号 or 客户号；   3 ： 手机号；  4： 客户号； */
    private Integer uniqueType;
    /** 奖品编码是否夸活动唯一 */
    private Boolean globalUniquePrizeType;
    /** 不需要客户号登录就可以领取的券，（而且不是占有状态） 1 是 0 否  */
    private Integer noNeedCustomerLogin;


}
