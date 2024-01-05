package com.wlzq.activity.task.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActGoodsRecordInfoDto {
    /** 脱敏手机号 */
    private String mobile = "";
    /** 剩余物品数量 */
    private Long remainCount = 0L;
    /** 物品流水记录列表 */
    private List<ActGoodsRecordDto> recordList = new ArrayList<>();
}
