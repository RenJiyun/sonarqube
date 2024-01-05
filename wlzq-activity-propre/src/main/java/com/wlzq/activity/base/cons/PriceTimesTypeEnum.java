package com.wlzq.activity.base.cons;

import java.util.Optional;

/**
 * @author luohc
 */
public enum PriceTimesTypeEnum {
    /** 一次性 */
    ONCE(1),
    /** 每天一次 */
    DAILY_ONCE(2),
    /** 每天多次 */
    MULTI_TIMES(3),
    MULTI_ON_CONDITION(4);

    final Integer code;

    PriceTimesTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static Optional<PriceTimesTypeEnum> fromCode(Integer code) {
        for (PriceTimesTypeEnum priceTimesTypeEnum : PriceTimesTypeEnum.values()) {
            if (priceTimesTypeEnum.getCode().equals(code)) {
                return Optional.of(priceTimesTypeEnum);
            }
        }
        return Optional.empty();
    }


}
