package com.wlzq.activity.virtualfin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 活动任务
 *
 * @author zhaozx
 */
@Data
@Accessors(chain = true)
public class ActTask implements Serializable {
    public static final String CODE_FIRST_LOGIN = "TASK.818.2020.FIRSTLOGIN";
    public static final Integer ONCE_TASK = 0;
    public static final Integer DAILY_TASK = 1;
    private static final long serialVersionUID = 7675196428590950861L;

    @JsonIgnore
    private String id;
    /** 任务代码 */
    private String code;
    /** 任务代码List，用于筛选 */
    private List<String> codeList;
    /** 完成任务的客户手机号 */
    private String mobile;
    /** 任务名称 */
    private String name;
    private String describe;
    /** 活动代码 */
    private String activityCode;
    /** 活动代码 */
    private String activityName;
    /** 物品代码 */
    private String goodsCode;
    /** 物品代码 */
    private String goodsName;
    /** 物品数量 */
    private Double goodsQuantity;
    /** 红包 */
    private Double redEnvelope;
    /** 任务类型，0-一次性任务，1-每日任务 */
    private Integer taskType;
    /** 客户任务，0-否，1-是 */
    private Integer customerTask;
    /** 任务数限制 */
    private Integer totalNum;
    /** 任务总次数限制 */
    private Integer allTotalNum;
    /** 完成数 */
    private Integer completeNum;
    /** 备注 */
    private String remark;
    /** 任务状态,0-下架，1-上架 */
    @JsonIgnore
    private Integer status;
    /** 任务是否展示，0-否，1-是 */
    private Integer isShow;
    /** 排序 */
    private Integer sort;
    /** 跳转链接 */
    private String url;
    @JsonIgnore
    private Date createTime;
    @JsonIgnore
    private Date updateTime;
    @JsonIgnore
    private Integer isDeleted;

    /** 用户任务状态 */
    private Integer userTaskStatus;
}
