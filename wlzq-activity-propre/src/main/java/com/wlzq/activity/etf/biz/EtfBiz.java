package com.wlzq.activity.etf.biz;

import com.wlzq.core.dto.StatusDto;

public interface EtfBiz {

    /**
     * 根据活动编码和奖品编码批量发券
     */
    StatusDto batchReceive(String activityCode, String prizeType);
}
