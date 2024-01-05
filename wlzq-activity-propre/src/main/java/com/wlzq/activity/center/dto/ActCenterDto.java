package com.wlzq.activity.center.dto;

import com.wlzq.activity.center.model.ActCenter;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: qiaofeng
 * @date: 2022/3/18 15:01
 * @description: 活动列表数据传输对象
 */
@Data
public class ActCenterDto implements Serializable {
    private static final long serialVersionUID = 247347379866329463L;

    /*区块位置*/
    private Integer position;
    /*当前区块展示的活动列表*/
    private List<ActCenter> activityList;
}
