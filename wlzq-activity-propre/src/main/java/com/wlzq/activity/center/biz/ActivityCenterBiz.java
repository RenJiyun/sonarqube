package com.wlzq.activity.center.biz;

import com.wlzq.activity.center.dto.ActCenterDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

public interface ActivityCenterBiz {
    /*根据客户端类型查询活动列表*/
    StatusObjDto<List<ActCenterDto>> listActivity(Integer cientType);

    /*活动列表有修改时，删除活动列表的缓存*/
    StatusDto delCache();
}
