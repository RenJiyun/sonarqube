package com.wlzq.activity.task.dto;

import com.wlzq.activity.virtualfin.model.ActTask;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActUserTaskDto {

    /** 未完成 */
    public static final Integer STATUS_UNFINISHED = 0;
    /** 已完成 */
    public static final Integer STATUS_FINISHED = 1;

    /** 任务模板代码 */
    private String code;
    /** 任务名称 */
    private String name;
    /** 活动代码 */
    private String activityCode;
    /** 物品代码 */
    private String goodsCode;
    /** 物品代码 */
    private String goodsName;
    /** 物品数量 */
    private Double goodsQuantity;
    /** 任务状态: 0-未完成, 1-已完成 */
    private Integer status = 0;
    /** 跳转链接 */
    private String url;
    /** 是否为客户任务, 是否需要登录客户号: 0-否, 1-是 */
    private Integer customerTask;
    /** 备注 */
    private String remark;

    public ActUserTaskDto(ActTask actTask) {
        this.code = actTask.getCode();
        this.name = actTask.getName();
        this.activityCode = actTask.getActivityCode();
        this.goodsCode = actTask.getGoodsCode();
        this.goodsName = actTask.getGoodsName();
        this.goodsQuantity = actTask.getGoodsQuantity();
        this.url = actTask.getUrl();
        this.customerTask = actTask.getCustomerTask();
        this.remark = actTask.getRemark();

        if (actTask.getUserTaskStatus() != null) {
            this.status = actTask.getUserTaskStatus();
        }
    }
}
