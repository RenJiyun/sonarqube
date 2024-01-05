package com.wlzq.activity.base.biz;

import com.wlzq.core.dto.StatusDto;

/**
 * 奖品数量监控业务类
 */
public interface ActPrizeMonitoringBiz {
    /**
     * 监控奖品数量是否达到警戒值，并发送短信，修改状态
     */
    StatusDto selectPrizeCount();
}
