package com.wlzq.activity.lottery.dto;

import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.common.constant.CodeConstant;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author luohc
 * @date 2023/3/27 14:22
 */
@Data
@Accessors(chain = true)
public class LotteryResDto {
    public static final LotteryResDto NOT_HIT = new LotteryResDto().setStatus(ActPrize.STATUS_SEND).setHit(CodeConstant.CODE_NO);
    public static final LotteryResDto USED_UP = new LotteryResDto().setHit(2);

    public static final Integer NOT_RECEIVE=1;
    public static final Integer RECEIVED=2;
    public static final Integer NO_REMAIN=3;
    public static final Integer USED=4;

    /** 状态,1:未领取,2:已领取,3:已抢光,4:已使用 */
    private Integer status;
    /** 是否抽中: 0-未抽中 1-抽中 2-抽奖次数已用完 */
    private Integer hit;
    /** 奖品类型 */
    private String prizeType;
    /** 奖品名称 */
    private String prizeName;
    private BigDecimal amount;
    private Long prizeId;
    private String prizeCode;


}
