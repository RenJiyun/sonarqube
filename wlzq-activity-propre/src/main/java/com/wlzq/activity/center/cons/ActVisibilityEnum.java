package com.wlzq.activity.center.cons;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: qiaofeng
 * @date: 2022/3/18 10:41
 * @description: 活动是否可见
 */
@Getter
@AllArgsConstructor
public enum ActVisibilityEnum {
    INVISIBLE(0, "全不可见"),
    ALL_VISIBLE(1, "全部可见"),
    APP_VISIBLE(2, "仅APP可见"),
    WECHAT_VISIBLE(3, "仅微信可见");

    private Integer code;
    private String desc;
}
