package com.wlzq.activity.point.biz;

import com.wlzq.activity.point.dto.PointsActivityReqDto;
import com.wlzq.core.dto.StatusObjDto;

public interface PointsActivityBiz {

    /**
     * 积分兑换
     */
    StatusObjDto<Object> redeem(PointsActivityReqDto req);
}
