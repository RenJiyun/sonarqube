package com.wlzq.activity.task.biz.impl;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBiz;
import com.wlzq.activity.base.dto.ActivityInfoDto;
import com.wlzq.activity.task.biz.ITaskBaseBiz;
import com.wlzq.activity.task.dto.ActTaskReqDto;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author luohc
 * @date 2023/3/30 10:48
 */
@Service
public class TasKVasPzbLotteryBizImpl implements ITaskBaseBiz {


    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;
    @Autowired
    private Task2023FreeLotteryBizImpl task2023FreeLotteryBiz;



    @Override
    public ActivityInfoDto getActivityInfo(){
        return new ActivityInfoDto().setActivityCode(ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY_PZB)
                .setCustomerIdVsMobile(2);
    }


    /**
     * 获取当前参与任务的客户号, 并且验证有没有完成任务的权限
     */
    @Override
    public String getCustomerIdAndCheckDoTaskAuth(ActTaskReqDto req, String activityCode, String taskCode, String mobile, ActTask actTask) {
        return task2023FreeLotteryBiz.getCustomerIdAndCheckDoTaskAuth(req,activityCode,taskCode,mobile,actTask);
    }



}
