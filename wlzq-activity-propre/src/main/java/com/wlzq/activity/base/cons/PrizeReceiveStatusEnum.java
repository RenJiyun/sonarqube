package com.wlzq.activity.base.cons;

/**
 * @author renjiyun
 */
public enum PrizeReceiveStatusEnum {

    /** 未领取 */
    NOT_RECEIVED(1),
    /** 已领取 */
    RECEIVED(2);

    public final Integer code;

    private PrizeReceiveStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}