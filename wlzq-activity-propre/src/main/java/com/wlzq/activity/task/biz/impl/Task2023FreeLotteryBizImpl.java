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
public class Task2023FreeLotteryBizImpl implements ITaskBaseBiz {


    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;



    @Override
    public ActivityInfoDto getActivityInfo(){
        return new ActivityInfoDto().setActivityCode(ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY)
                .setCustomerIdVsMobile(2);
    }


    /**
     * 获取当前参与任务的客户号, 并且验证有没有完成任务的权限
     */
    @Override
    public String getCustomerIdAndCheckDoTaskAuth(ActTaskReqDto req, String activityCode, String taskCode, String mobile, ActTask actTask) {
        //region 校验有没有完成该任务权限; 并获取 List<Map<String, Object>> vasOrderList
        Date now = new Date();
        Map<String, Object> pMap = new HashMap<>();
        pMap.put("mobile", mobile);
        pMap.put("goodsCode", actTask.getGoodsCode());
        pMap.put("articleId", req.getBizCode());
        pMap.put("status",1);
        pMap.put("goodsCodeOrArticleId",1);
        pMap.put("now",now.getTime());
        //根据当前登录手机号，查询决策购买订单表VAS_GOODS_ORDER；查出购买的customer_id
        ResultDto resultDto = RemoteUtils.call("vas.decisioncooperation.findorders", ApiServiceTypeEnum.COOPERATION, pMap, true);
        List<Map<String, Object>> vasOrderList = null;
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            vasOrderList = (List<Map<String, Object>>) resultDto.getData().get("info");
        }
        if(CollectionUtils.isEmpty(vasOrderList)){
            throw ActivityBizException.ACT_TASK_NO_AUTH;
        }
        //endregion


        //region 校验同一个客户号，有多个手机号参与完成任务; 并获取当前参与任务的客户号
        String curCustomerId = "";
        ActivityInfoDto activityInfo = getActivityInfo();
        if (activityInfo.getCustomerIdVsMobile() >= 1) {
            for (int i = 0; i < vasOrderList.size(); i++) {
                Map<String, Object> vasOrder = vasOrderList.get(i);
                String customerId = (String) vasOrder.get("customerId");
                //customer_id、activity_code、task_code 判断活动历史上，同一个客户号，是否有超过两个手机号参与完成任务
                ActGoodsFlow goodsFlow = new ActGoodsFlow();
                goodsFlow.setActivityCode(activityCode);
                goodsFlow.setTaskCode(taskCode);
                goodsFlow.setCustomerId(customerId);
                List<ActGoodsFlow> actGoodsFlows = actGoodsFlowDao.list(goodsFlow);
                if (!CollectionUtils.isEmpty(actGoodsFlows)) {
                    List<String> mobiles = actGoodsFlows.stream().map(ActGoodsFlow::getMobile).distinct().collect(Collectors.toList());
                    if (mobiles.size()>=activityInfo.getCustomerIdVsMobile() && !mobiles.contains(mobile) ) {
                        if (i!=vasOrderList.size()-1) {
                            continue;
                        }else{
                            throw ActivityBizException.ACT_TASK_TIMES_LIMIT;
                        }
                    }
                }
                curCustomerId = customerId;
                break;
            }
        }
        if (StringUtils.isBlank(curCustomerId)) {
            throw ActivityBizException.ACT_TASK_NO_AUTH;
        }
        //endregion
        return curCustomerId;
    }



}
