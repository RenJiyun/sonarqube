package com.wlzq.activity.task.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.task.biz.TaskBiz;
import com.wlzq.activity.task.biz.TaskPoolBiz;
import com.wlzq.activity.task.biz.UserTaskBiz;
import com.wlzq.activity.task.biz.impl.FreeCourseBizImpl;
import com.wlzq.activity.task.dto.ActTaskReqDto;
import com.wlzq.activity.task.dto.ActTaskStatusDto;
import com.wlzq.activity.task.dto.ActUserTaskDto;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.wlzq.activity.base.redis.CouponCommonRedis.COUPON_REDEEM_LOCK;
import static com.wlzq.activity.double11.biz.Double11Biz.ACTIVITY_2023DOUBLE11_ZJF;
import static com.wlzq.activity.task.redis.TaskRedis.TASK_LOCK;

/**
 * @author luohc
 */
@Service("activity.usertask")
@ApiServiceType({ApiServiceTypeEnum.APP})
@Slf4j
public class TaskService {

    @Autowired
    private TaskBiz taskBiz;
    @Autowired
    private UserTaskBiz userTaskBiz;
    @Autowired
    private FreeCourseBizImpl taskFreeCourseBiz;
    @Autowired
    private TaskPoolBiz taskPoolBiz;


    /**
     * 查询任务状态
     *
     * @param activityCode | 活动编码 |  | required
     * @param taskCode     | 任务编码 |  | required
     * @param bizCode      | 业务编码, 例如文章id |  | non-required
     * @return com.wlzq.activity.task.dto.ActTaskStatusDto
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto status(RequestParams params, AccTokenUser user, Customer customer) {
        String userId = user.getUserId();
        String mobile = user.getMobile();

        // 针对投顾相关的任务
        if (ObjectUtils.isEmptyOrNull(mobile)) {
            mobile = customer == null ? "" : customer.getMobile();
            log.info("投顾相关任务取柜台预留手机号, mobile:{}", mobile);
        }

        String activityCode = params.getString("activityCode");
        String taskCode = params.getString("taskCode");
        String customerId = customer == null ? null : customer.getCustomerId();
        String bizCode = params.getString("bizCode");

        log.info("查询任务状态, userId:{}, mobile:{}, activityCode:{}, taskCode:{}, bizCode:{}",
                userId, mobile, activityCode, taskCode, bizCode);

        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(taskCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("taskCode");
        }

        ActTaskReqDto req = new ActTaskReqDto().setUserId(userId).setCustomerId(customerId)
                .setTaskCode(taskCode).setActivityCode(activityCode)
                .setBizCode(bizCode).setMobile(mobile);
        StatusObjDto<ActTaskStatusDto> result = taskBiz.queryUserTaskStatus(req);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
    }

    /**
     * 任务列表查询
     */
    @Signature(true)
    public ResultDto tasklist(RequestParams params, AccTokenUser user, Customer customer) {
        String userId = user != null ? user.getUserId() : null;
        String mobile = user != null ? user.getMobile() : null;
        String activityCode = params.getString("activityCode");

        ActTaskReqDto req = new ActTaskReqDto()
                .setUserId(userId)
                .setMobile(mobile)
                .setActivityCode(activityCode);
        StatusObjDto<List<ActTask>> result = taskBiz.taskList(req);

        Map<String, Object> data = Maps.newHashMap();
        data.put("info", result.getObj());
        return new ResultDto(0, data, "");
    }


    /**
     * 获取用户任务列表
     *
     * @param activityCode | 活动编码 |  | required
     * @return com.wlzq.activity.task.dto.ActUserTaskDto
     * @cate 2023年双十一
     */
    @Signature(true)
    public ResultDto usertasklist(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }

        StatusObjDto<List<ActUserTaskDto>> result = userTaskBiz.userTaskList(activityCode, user, customer);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        Map<String, Object> data = Maps.newHashMap();
        List<ActUserTaskDto> actUserTaskDtos = result.getObj();
        data.put("total", actUserTaskDtos == null ? 0 : actUserTaskDtos.size());
        data.put("info", actUserTaskDtos);
        return new ResultDto(0, data, "");
    }


    /**
     * 更新用户任务列表
     *
     * @param activityCode | 活动编码 |  | required
     * @param isDefault    | 是否更新为默认任务列表 | 0-否, 1-是 | non-required
     * @return com.wlzq.activity.task.dto.ActUserTaskDto
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto updateusertasklist(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }

        boolean isDefault = "1".equals(params.getString("isDefault"));
        boolean auto = "1".equals(params.getString("auto"));

        StatusObjDto<List<ActUserTaskDto>> result = userTaskBiz.updateUserTaskList(activityCode, isDefault, auto, user, customer);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        } else {
            Map<String, Object> data = Maps.newHashMap();
            List<ActUserTaskDto> actUserTaskDtos = result.getObj();
            data.put("total", actUserTaskDtos == null ? 0 : actUserTaskDtos.size());
            data.put("info", actUserTaskDtos);
            return new ResultDto(0, data, "");
        }
    }


    /**
     * 完成任务
     *
     * @param activityCode     | 活动编码 |  | required
     * @param taskCode         | 任务编码 |  | required
     * @param bizCode          | 业务编码, 例如文章id |  | required
     * @param description      | 描述 |  | non-required
     * @param recommendMobile | 推荐人手机号 |  | non-required
     * @return com.wlzq.activity.task.dto.ActTaskStatusDto
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto dotask(RequestParams params, AccTokenUser user, Customer customer) {
        String userId = user.getUserId();
        String activityCode = params.getString("activityCode");
        String taskCode = params.getString("taskCode");
        String bizCode = params.getString("bizCode");
        String mobile = user.getMobile();
        String description = params.getString("description");
        String customerId = customer == null ? null : customer.getCustomerId();

        if (ObjectUtils.isEmptyOrNull(mobile)) {
            mobile = customer == null ? "" : customer.getMobile();
            log.info("投顾相关任务取柜台预留手机号, mobile:{}", mobile);
        }

        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(taskCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("taskCode");
        }

        String recommendMobile = params.getString("recommendMobile");

        ActTaskReqDto req = new ActTaskReqDto()
                .setUserId(userId)
                .setTaskCode(taskCode)
                .setActivityCode(activityCode)
                .setDescription(description)
                .setCustomerId(customerId)
                .setBizCode(bizCode)
                .setMobile(mobile);
        if (customer != null && StringUtils.isNotBlank(customer.getCustomerId())) {
            req.setCustomerId(customer.getCustomerId());
        }

        StatusObjDto<ActTaskStatusDto> result = null;
        // 2023年双11攒积分活动走新的逻辑分支
        if (ACTIVITY_2023DOUBLE11_ZJF.equals(activityCode)) {
            Long currentThreadId = Thread.currentThread().getId();
            boolean success = TASK_LOCK.setNXEX(user.getMobile(), currentThreadId);
            if (!success) {
                throw BizException.COMMON_PARAMS_NOT_NULL.format("任务完成中, 请稍后再试");
            }
            try {
                result = userTaskBiz.doUserTask(activityCode, taskCode, bizCode, recommendMobile, mobile, user, customer, false);
            } finally {
                if (currentThreadId.equals(TASK_LOCK.get(user.getMobile()))) {
                    TASK_LOCK.del(user.getMobile());
                }
            }
        } else {
            result = taskBiz.doTask(req);
        }
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
    }

    @Signature(true)
    @MustLogin(true)
    public ResultDto infofreecource(RequestParams params, AccTokenUser user, Customer customer) {
        String userId = user.getUserId();
        String mobile = user.getMobile();
        String activityCode = params.getString("activityCode");
        String customerId = customer == null ? null : customer.getCustomerId();

        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }

        ActTaskReqDto req = new ActTaskReqDto().setUserId(userId).setCustomerId(customerId)
                .setActivityCode(activityCode).setMobile(mobile);
        StatusObjDto<Object> result = taskFreeCourseBiz.queryTaskInfo(req);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
    }


}
