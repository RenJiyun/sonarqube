package com.wlzq.activity.task.biz;

import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务池
 *
 * @author renjiyun
 */
public interface TaskPoolBiz {

    /**
     * 获取默认任务列表
     *
     * @return
     */
    List<ActTask> getDefaultTaskList();

    /**
     * 获取默认任务列表
     *
     * @param user
     * @param customer
     * @return
     */
    List<ActTask> getDefaultTaskList(AccTokenUser user, Customer customer);


    /**
     * 获取用户剩余可做的任务列表和当日可做的任务列表
     *
     * @param actGoodsFlowList
     * @param user
     * @param customer
     * @return
     */
    Tuple getRemainTasks(List<ActGoodsFlow> actGoodsFlowList, AccTokenUser user, Customer customer);


    /**
     * 获取用户新的任务列表
     *
     * @param candidateTaskCodes 候选任务列表
     * @param auto               是否为自动刷新触发
     * @param user
     * @param customer
     * @return
     */
    List<ActTask> getNewUserTaskList(Set<String> candidateTaskCodes, boolean auto, AccTokenUser user, Customer customer);

    /**
     * 更新用户任务列表
     *
     * @param newUserTaskList
     * @param user
     */
    void updateUserTaskList(List<ActTask> newUserTaskList, AccTokenUser user);

    /**
     * 获取任务列表以及该列表的创建时间
     *
     * @param user
     * @return
     */
    Tuple getUserTaskList(AccTokenUser user);

    /**
     * 获取所有任务
     *
     * @return
     */
    Map<String, ActTask> getActTaskMap();

}
