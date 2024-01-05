package com.wlzq.activity.task.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ActTaskReqDto {
    private String key;
    private String accessToken;
    private String userId;
    private String customerId;

    /** 任务
     1：浏览主推基金页
     2：浏览投顾社区文章
     3：首次开通条件单
     4: 是否为新用户

     5: 订阅投顾产品
     6: 关注投顾
     7: 收藏投顾文章
     多个用逗号分隔
     */
    private String task;

    /** 开始时间（时间戳，精确到毫秒 */
    private Long beginDate;
    /** 结束时间（时间戳，精确到毫秒 */
    private Long endDate;


    private String goodsCode;
    private String mobile;
    /** 活动编号 */
    private String activityCode;
    /** 任务编号 */
    private String taskCode;
    /** 业务编码（阅读文章id） */
    private String bizCode;

    /* 积分描述 */
    private String description;

}
