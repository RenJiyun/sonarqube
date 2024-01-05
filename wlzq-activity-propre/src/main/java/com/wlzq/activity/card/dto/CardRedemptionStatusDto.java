package com.wlzq.activity.card.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class CardRedemptionStatusDto implements Serializable {
    private Long id;
    /** 奖品名称 */
    private String name;
    /** 兑换码/优惠券编码 */
    private String code;
    /** 本次兑换所使用的卡牌 id 列表 */
    private List<Long> usedCardIds;
}
