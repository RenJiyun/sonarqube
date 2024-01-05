package com.wlzq.activity.task.biz.impl;

import com.wlzq.activity.task.biz.UserTaskSelectionBiz;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 根据任务权重大小挑选任务列表
 *
 * @author renjiyun
 */
@Service
public class UserTaskSelectionByWeightImpl implements UserTaskSelectionBiz {
    // 任务权重比例
    private static final int[] TASK_WEIGHTS = new int[]{
            50, 10, 30, 30, 10, 10, 10, 10, 10, 10,
            10, 20, 10, 10, 20, 10, 50, 10, 50, 50,
            50, 50, 50, 50, 50, 10, 10, 10, 10, 10,
            50, 30, 50, 50, 50, 30, 10
    };

    private static final int MAX_NUM = 8;
    @Override
    public List<ActTask> select(List<ActTask> candidateTaskList, AccTokenUser user, Customer customer) {
        // 1. 根据任务权重排序
        // 2. 选出前8个
        candidateTaskList.sort((o1, o2) -> {
            int index1 = getIndex(o1.getCode());
            int index2 = getIndex(o2.getCode());
            return TASK_WEIGHTS[index2] - TASK_WEIGHTS[index1];
        });
        return candidateTaskList.subList(0, Math.min(candidateTaskList.size(), MAX_NUM));
    }

    private int getIndex(String code) {
        // task code 的格式为 TASK.2023DOUBLE11.[index]
        String[] split = code.split("\\.");
        return Integer.parseInt(split[2]) - 1;
    }
}
