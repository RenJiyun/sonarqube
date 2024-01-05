package com.wlzq.activity.task.biz;

import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;

import java.util.List;

/**
 * 用户任务挑选策略
 *
 * @author renjiyun
 */
public interface UserTaskSelectionBiz {
    /**
     * 选择任务
     *
     * @param candidateTaskList
     * @param user
     * @param customer
     * @return
     */
    List<ActTask> select(List<ActTask> candidateTaskList, AccTokenUser user, Customer customer);
}
