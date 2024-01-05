package com.wlzq.activity.task.biz;

import com.wlzq.activity.base.dto.ActivityInfoDto;
import com.wlzq.activity.task.dto.ActTaskReqDto;
import com.wlzq.activity.virtualfin.model.ActTask;

/**
 * @author luohc
 * @date 2023/3/30 13:53
 */
public interface ITaskBaseBiz {


    ActivityInfoDto getActivityInfo();

    String getCustomerIdAndCheckDoTaskAuth(ActTaskReqDto req, String activityCode, String taskCode, String mobile, ActTask actTask) ;


}
