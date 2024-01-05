package com.wlzq.activity.task.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author luohc
 */
@Data
@Accessors(chain = true)
public class ActTaskStatusDto implements Serializable {
    /** 任务
     2：浏览投顾社区文章
     1：浏览主推基金页
     3：首次开通条件单
     4: 是否为新用户
     5: 订阅投顾产品(即购买投顾产品)。
        万联绩牛双十一淘金游园会0元抽iphone活动，需要相应的返回金额。
     6: 关注投顾
     7: 收藏投顾文章
     8: 购买投顾e课程

     万联绩牛双十一淘金游园会0元抽iphone活动涉及 ： 5,8
     */
    private Integer task;

    /** 状态，0：未完成（否），1：完成（是） */
    private Integer status;

    /** 奖励次数。目前task=3的时候，才读取这个字段。*/
    private Integer times;

    /** 金额，单位分。如task=5的时候，amount代表购买xx金额的投顾产品 */
    private Integer amount;


    private String bizCode;
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date time;


}
