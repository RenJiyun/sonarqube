package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.task.biz.SpecificTaskBiz;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 浏览类任务校验
 *
 * @author renjiyun
 */
@Service
public class BrowseTaskBizImpl implements SpecificTaskBiz {

    private final String[] supportTaskCodes = {
            "TASK.2023DOUBLE11.01", // 浏览投顾文章10s
            "TASK.2023DOUBLE11.02",
            "TASK.2023DOUBLE11.03",
            "TASK.2023DOUBLE11.04",
            "TASK.2023DOUBLE11.05",
            "TASK.2023DOUBLE11.06",
            "TASK.2023DOUBLE11.07",
            "TASK.2023DOUBLE11.08",
            "TASK.2023DOUBLE11.09",
            "TASK.2023DOUBLE11.10",
            "TASK.2023DOUBLE11.11",
            "TASK.2023DOUBLE11.12",
            "TASK.2023DOUBLE11.13",
            "TASK.2023DOUBLE11.14",
            "TASK.2023DOUBLE11.15",
            "TASK.2023DOUBLE11.16",
            "TASK.2023DOUBLE11.17",
            "TASK.2023DOUBLE11.18",
    };

    @Override
    public String[] supportTaskCodes() {
        return supportTaskCodes;
    }

    @Override
    public Tuple checkTask(Activity activity, ActTask actTask, List<ActGoodsFlow> actGoodsFlowList, String bizCode,
                           String mobile, AccTokenUser user, Customer customer, String recommendMobile, boolean isBatch) {
        // 浏览投顾文章任务
        if ("TASK.2023DOUBLE11.01".equals(actTask.getCode())) {
            if (ObjectUtils.isEmptyOrNull(bizCode)) {
                return new Tuple(false, "");
            }
            // bizCode 为文章 id, 校验该文章 id 是否已经做过任务
            if (actGoodsFlowList.stream().anyMatch(actGoodsFlow -> bizCode.equals(actGoodsFlow.getBizCode()))) {
                return new Tuple(false, "");
            }
        }

        // 员工给自己分享的场景
        if (user.getMobile().equals(recommendMobile)) {
            return new Tuple(true, "");
        }
        return new Tuple(true, recommendMobile);
    }

    @Override
    public void excludeTaskCodes(Set<String> candidateTaskCodes, AccTokenUser user, Customer customer) {}
}
