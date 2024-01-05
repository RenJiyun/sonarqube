package com.wlzq.activity.base.biz;

import com.wlzq.activity.task.biz.ITaskBaseBiz;
import com.wlzq.activity.task.biz.impl.Task2023FreeLotteryBizImpl;
import com.wlzq.activity.task.biz.impl.TaskGgqzdBizImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luohc
 * @date 2023/3/30 13:47
 */
@Service
public class ActivityBiz {

    /** 风控研报12天免费天天抽奖活动 */
    public static final String ACTIVITY_2023_12_FREE_LOTTERY = "ACTIVITY.2023.DAILY.LOTTERY";
    /** 盘中宝12天免费天天抽奖活动 */
    public static final String ACTIVITY_2023_12_FREE_LOTTERY_PZB = "ACTIVITY.VAS.PZB.LOTTERY";

    public static final String ACTIVITY_GGQZD = "ACTIVITY.GGQZD";


    @Autowired
    private Task2023FreeLotteryBizImpl task2023FreeLotteryBiz;

    @Autowired
    private TaskGgqzdBizImpl taskGgqzdBiz;


    public ITaskBaseBiz getTaskBizImplByActivityCode(String activityCode){
        if (ACTIVITY_2023_12_FREE_LOTTERY.equals(activityCode)) {
            return task2023FreeLotteryBiz;
        }

        if (ACTIVITY_GGQZD.equals(activityCode)) {
            return taskGgqzdBiz;
        }
        return task2023FreeLotteryBiz;
    }





}
