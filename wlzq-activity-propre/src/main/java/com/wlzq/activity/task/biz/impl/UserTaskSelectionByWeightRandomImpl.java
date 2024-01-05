package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.WeightRandom;
import com.wlzq.activity.task.biz.UserTaskSelectionBiz;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 根据权重随机选择任务
 *
 * @author renjiyun
 */
@Service
public class UserTaskSelectionByWeightRandomImpl implements UserTaskSelectionBiz {

    // 任务权重比例
    private static final int[] TASK_WEIGHTS = new int[]{
            50, 10, 30, 30, 10, 10, 10, 10, 10, 10,
            10, 20, 10, 10, 20, 10, 50, 10, 50, 50,
            50, 50, 50, 50, 50, 10, 10, 10, 10, 10,
            50, 30, 10000000, 10000000, 50, 30, 10
    };

    private static final int MAX_NUM = 8;
    @Override
    public List<ActTask> select(List<ActTask> candidateTaskList, AccTokenUser user, Customer customer) {
        int maxNum = AppConfigUtils.getInt("2023double11.task.maxNum", 8);
        if (CollectionUtil.isEmpty(candidateTaskList)) {
            return candidateTaskList;
        }

        Set<WeightRandom.WeightObj<ActTask>> weightObjs = candidateTaskList.stream().map(task ->
            new WeightRandom.WeightObj<>(task, TASK_WEIGHTS[getIndex(task.getCode())])
        ).collect(Collectors.toSet());

        List<ActTask> result = new ArrayList<>();

        for (int i = 0; i < maxNum; i++) {
            WeightRandom<ActTask> weightRandom = new WeightRandom<>(weightObjs);
            ActTask actTask = weightRandom.next();
            result.add(actTask);
            weightObjs.remove(new WeightRandom.WeightObj<>(actTask, TASK_WEIGHTS[getIndex(actTask.getCode())]));
            if (weightObjs.isEmpty()) {
                break;
            }
        }

        // 获取候选任务对应的权重
//        List<Integer> weights = candidateTaskList.stream().map(task -> TASK_WEIGHTS[getIndex(task.getCode())])
//                .collect(Collectors.toList());
//        int upper = weights.stream().reduce(Integer::sum).orElse(0);
//
//        for (int i = 0; i < MAX_NUM; i++) {
//            int random = (int) (Math.random() * upper);
//            int sum = 0;
//            for (int j = 0; j < weights.size(); j++) {
//                sum += weights.get(j);
//                if (random <= sum) {
//                    result.add(candidateTaskList.get(j));
//                    upper -= weights.get(j);
//                    weights.set(j, 0);
//                    break;
//                }
//            }
//        }

        return result;
    }

    private int getIndex(String code) {
        // task code 的格式为 TASK.2023DOUBLE11.[index]
        String[] split = code.split("\\.");
        return Integer.parseInt(split[2]) - 1;
    }
}
