package com.wlzq.activity.lottery.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author luohc
 * @date 2023/3/27 14:22
 */
@Data
@Accessors(chain = true)
public class PrizeResDto {

    /** 手机号 */
    private String mobile ;
    /** 更新时间 */
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date updateTime;
    /** 奖品编码 */
    private String prizeCode ;
    /** 奖品名称 */
    private String prizeName ;


}
