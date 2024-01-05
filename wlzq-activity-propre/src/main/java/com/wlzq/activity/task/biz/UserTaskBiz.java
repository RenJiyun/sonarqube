package com.wlzq.activity.task.biz;

import com.wlzq.activity.task.dto.ActTaskStatusDto;
import com.wlzq.activity.task.dto.ActUserTaskDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

/**
 * @author renjiyun
 */
public interface UserTaskBiz {
    /**
     * 获取用户任务列表
     *
     * @param activityCode
     * @param user
     * @param customer
     * @return
     */
    StatusObjDto<List<ActUserTaskDto>> userTaskList(String activityCode, AccTokenUser user, Customer customer);

    /**
     * 更新用户任务列表
     *
     * @param activityCode
     * @param isDefault
     * @param auto
     * @param user
     * @param customer
     * @return
     */
    StatusObjDto<List<ActUserTaskDto>> updateUserTaskList(String activityCode, boolean isDefault, boolean auto,
                                                          AccTokenUser user, Customer customer);

    /**
     * 完成用户任务
     *
     * @param activityCode
     * @param taskCode
     * @param bizCode
     * @param recommendMobile
     * @param mobile
     * @param user
     * @param customer
     * @param isBatch
     * @return
     */
    StatusObjDto<ActTaskStatusDto> doUserTask(String activityCode, String taskCode, String bizCode, String recommendMobile,
                                              String mobile, AccTokenUser user, Customer customer, boolean isBatch);
}
