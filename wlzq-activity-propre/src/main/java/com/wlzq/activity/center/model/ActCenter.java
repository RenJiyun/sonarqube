package com.wlzq.activity.center.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;

/**
 * 活动中心页面配置
 *
 * @author qiaofeng
 * @date 2022-03-18 14:39:33
 */
@Data
public class ActCenter implements Serializable {
    private static final long serialVersionUID = 233348047467556751L;

    @JsonIgnore
    private Integer id;
    /**
     * 位置,1:任务区,2:今日推荐,3:热门活动,4:底部栏,5:已结束活动,6:新客专享
     */
    @JsonIgnore
    private Integer position;
    /**
     * 标题
     */
    private String title;
    /**
     * 摘要
     */
    private String summary;
    /**
     * 图片
     */
    private String image;
    /**
     * 排序，值小排前
     */
    private Integer sort;
    /**
     * 是否置顶，0:否,1:是
     */
    private Integer isTop;
    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createTime;
    /**
     * 置顶时间
     */
    @JsonIgnore
    private Date topTime;
    /**
     * 更新时间
     */
    @JsonIgnore
    private Date updateTime;
    /**
     * 是否删除,0:否,1:是
     */
    @JsonIgnore
    private Integer isDeleted;
    /**
     * 备注
     */
    private String remark;
    /**
     * 活动来源，1:自研,2:其他
     */
    private Integer platform;
    /**
     * 活动开始时间
     */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date startTime;
    /**
     * 活动结束时间
     */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date endTime;
    /**
     * 跳转链接
     */
    private String routeUrl;
    /**
     * 是否可见, 0:全不可见,1:全部可见,2:仅APP可见,3:仅微信可见
     */
    @JsonIgnore
    private Integer visibility;
    /**
     * 活动编码
     */
    private String activityCode;
    /**
     * 前端展示状态,0:不展示,1:正在展示
     */
    @JsonIgnore
    private Integer display;
}