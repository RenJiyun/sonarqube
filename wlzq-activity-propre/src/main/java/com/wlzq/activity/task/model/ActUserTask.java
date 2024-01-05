package com.wlzq.activity.task.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户任务
 *
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActUserTask {

    /** 未完成 */
    public static final Integer STATUS_UNFINISHED = 0;
    /** 已完成 */
    public static final Integer STATUS_FINISHED = 1;
    /** 完成中 */
    public static final Integer STATUS_DOING = 2;
    /** 该状态依赖于此前的任务 */
    public static final Integer STATUS_DEPENDENCY = 3;

    private String id;
    /** 任务模板代码 */
    private String code;
    /** 任务名称 */
    private String name;
    /** 用户手机号 */
    private String mobile;
    /** 活动代码 */
    private String activityCode;
    /** 任务状态: 0-未完成, 1-已完成, 2-完成中, 3-关联依赖 */
    private Integer status;
    /** 排序 */
    private Integer sort;
    /** 任务批次编号: 例如当前时间戳 */
    private String batch;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    /** 是否删除 */
    private Integer isDeleted;
}
