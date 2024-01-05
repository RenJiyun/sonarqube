package com.wlzq.activity.lottery.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author luohc
 * @date 2023/3/27 14:22
 */
@Data
@Accessors(chain = true)
public class LotteryQueryResDto {

    /** 剩余抽奖次数 */
    private Integer remainTimes ;
    /** 累计获得的抽奖次数 */
    private Integer totalTimes ;
    /** 当天是否完成任务  1已完成 0未完成 */
    private Integer taskStatus;



}
