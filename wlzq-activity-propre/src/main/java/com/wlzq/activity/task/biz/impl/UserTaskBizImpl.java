package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.task.biz.SpecificTaskBiz;
import com.wlzq.activity.task.biz.TaskPoolBiz;
import com.wlzq.activity.task.biz.UserTaskBiz;
import com.wlzq.activity.task.dao.ActGoodsRecordDao;
import com.wlzq.activity.task.dto.ActTaskStatusDto;
import com.wlzq.activity.task.dto.ActUserTaskDto;
import com.wlzq.activity.task.model.ActGoodsRecord;
import com.wlzq.activity.task.redis.UserTaskRedis;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.wlzq.activity.ActivityBizException.*;

/**
 * @author renjiyun
 */
@Service
@Slf4j
public class UserTaskBizImpl implements UserTaskBiz, ApplicationContextAware {

    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private TaskPoolBiz taskPoolBiz;
    @Autowired
    private ActGoodsRecordDao actGoodsRecordDao;
    private ApplicationContext applicationContext;

    private volatile Map<String, SpecificTaskBiz> specificTaskBizMap;


    private SpecificTaskBiz getSpecificTaskBiz(String taskCode) {
        Map<String, SpecificTaskBiz> specificTaskBizMap = getSpecificTaskBizMap();
        return specificTaskBizMap.get(taskCode);
    }

    private Map<String, SpecificTaskBiz> getSpecificTaskBizMap() {
        if (specificTaskBizMap == null) {
            synchronized (this) {
                if (specificTaskBizMap == null) {
                    specificTaskBizMap = doGetSpecificTaskBizMap();
                }
            }
        }
        return specificTaskBizMap;
    }

    private Map<String, SpecificTaskBiz> doGetSpecificTaskBizMap() {
        Map<String, SpecificTaskBiz> specificTaskBizMap = new HashMap<>();
        Collection<SpecificTaskBiz> specificTaskBizList = applicationContext.getBeansOfType(SpecificTaskBiz.class).values();
        for (SpecificTaskBiz specificTaskBiz : specificTaskBizList) {
            String[] supportTaskCodes = specificTaskBiz.supportTaskCodes();
            if (supportTaskCodes != null) {
                for (String supportTaskCode : supportTaskCodes) {
                    specificTaskBizMap.put(supportTaskCode, specificTaskBiz);
                }
            }
        }
        return specificTaskBizMap;
    }

    private Collection<SpecificTaskBiz> getSpecificTaskBizs() {
        return applicationContext.getBeansOfType(SpecificTaskBiz.class).values();
    }


    @Override
    public StatusObjDto<List<ActUserTaskDto>> userTaskList(String activityCode, AccTokenUser user, Customer customer) {
        StatusDto actCheckResult = activityBaseBiz.isValid(activityCode);

        // 若活动已经结束, 还是展示原先该用户的任务列表
        if (!actCheckResult.isOk() && !actCheckResult.getCode().equals(219) && !actCheckResult.getCode().equals(210)) {
            return new StatusObjDto<>(false, null, actCheckResult.getCode(), actCheckResult.getMsg());
        }

        // 1. 若用户未登录手机号, 则直接返回任务池指定的默认任务列表
        if (user == null) {
            return new StatusObjDto<>(
                    true, convertActTaskToActUserTaskDto(taskPoolBiz.getDefaultTaskList()),
                    StatusDto.SUCCESS, "");
        }
        Tuple tuple = taskPoolBiz.getUserTaskList(user);
        List<ActTask> userTaskList = tuple.get(0);
        Date taskListCreateTime = tuple.get(1);
        if (CollectionUtil.isEmpty(userTaskList)) {
            // 2. 若用户首次进入活动页面(即该用户尚未有过任务列表), 则返回任务池指定的默认任务列表, 并将此任务列表作为该用户的当前任务列表
            // 需要注意的是, 这些任务用户有可能已经完成
            return doUpdateUserTaskList(activityCode, true, false, user, customer);
        } else {
            if (!DateUtil.isSameDay(taskListCreateTime, new Date())) {
                // 3. 若用户已有任务列表, 但不是当天, 则按规则自动为用户刷新任务列表, 并将此任务列表作为该用户的当前任务列表
                StatusObjDto<List<ActUserTaskDto>> updateResult =
                        doUpdateUserTaskList(activityCode, false, true, user, customer);

                // 若自动刷新结果成功, 则返回自动刷新结果, 否则返回用户当前任务列表
                if (updateResult.isOk()) {
                    return updateResult;
                }
            }
        }
        // 4. 默认情况, 返回用户当前任务列表
        List<ActUserTaskDto> actUserTaskDtoList = convertActTaskToActUserTaskDto(userTaskList);
        populateTaskStatus(actUserTaskDtoList, getActGoodsFlowList(activityCode, null, user.getMobile()));

        actUserTaskDtoList.sort(new Comparator<ActUserTaskDto>() {
            @Override
            public int compare(ActUserTaskDto o1, ActUserTaskDto o2) {
                return o2.getStatus() - o1.getStatus();
            }
        });

        return new StatusObjDto<>(true, actUserTaskDtoList, StatusDto.SUCCESS, "");
    }

    @Override
    public StatusObjDto<List<ActUserTaskDto>> updateUserTaskList(String activityCode, boolean isDefault, boolean auto,
                                                                 AccTokenUser user, Customer customer) {
        // 该方法由以下两个场景触发:
        // 1. 用户点击换一换
        // 2. 用户未登录情况下, 看到默认任务列表, 之后用户点击去完成, 用户登录后, 需要强制更新为默认任务列表, 此时参数 isDefault 为 true

        StatusDto actCheckResult = activityBaseBiz.isValid(activityCode);
        if (!actCheckResult.isOk()) {
            return new StatusObjDto<>(false, null, actCheckResult.getCode(), actCheckResult.getMsg());
        }

        return doUpdateUserTaskList(activityCode, isDefault, auto, user, customer);
    }

    /**
     * 更新用户任务列表
     *
     * @param activityCode
     * @param isDefault    是否使用默认任务列表
     * @param auto         是否为自动刷新触发
     * @param user
     * @param customer
     * @return
     */
    private StatusObjDto<List<ActUserTaskDto>> doUpdateUserTaskList(String activityCode, boolean isDefault, boolean auto,
                                                                    AccTokenUser user, Customer customer) {
        // 获取该用户在该活动下的所有任务完成记录
        List<ActGoodsFlow> actGoodsFlowList = getActGoodsFlowList(activityCode, null, user.getMobile());

        // 1. 若强制更新为默认任务列表, 则将默认任务列表作为该用户的当前任务列表
        // 2. 若用户已经完成任务库中所有任务, 则任务列表不再更新, 由前端给出相应的提示语
        // 3. 若用户已经完成当日所有任务, 则当日不再更新任务列表, 由前端给出相应的提示语
        // 4. 按规则生成新的任务列表, 且将此任务列表作为该用户的当前任务列表
        List<ActTask> newUserTaskList = null;
        boolean needPopulateTaskStatus = false;
        if (isDefault) {
            newUserTaskList = taskPoolBiz.getDefaultTaskList(user, customer);
            needPopulateTaskStatus = true;
        } else {
            // 过滤已经完成的任务
            Tuple tuple = taskPoolBiz.getRemainTasks(actGoodsFlowList, user, customer);
            Set<String> remainTaskCodeSet = tuple.get(0);
            Set<String> todayRemainTaskCodeSet = tuple.get(1);
            if (CollectionUtil.isEmpty(remainTaskCodeSet)) {
                return new StatusObjDto<>(false, null, ACT_TASK_ALL_HAS_DONE.getCode(), ACT_TASK_ALL_HAS_DONE.getMsg());
            }

            if (CollectionUtil.isEmpty(todayRemainTaskCodeSet)) {
                return new StatusObjDto<>(false, null, ACT_TASK_TODAY_ALL_HAS_DONE.getCode(), ACT_TASK_TODAY_ALL_HAS_DONE.getMsg());
            }

            // 根据相关的特殊条件, 过滤掉不符合条件的任务
            Collection<SpecificTaskBiz> specificTaskBizs = getSpecificTaskBizs();
            for (SpecificTaskBiz specificTaskBiz : specificTaskBizs) {
                specificTaskBiz.excludeTaskCodes(todayRemainTaskCodeSet, user, customer);
            }

            // 对于 auto = true 的场景(第二天首次进入活动页面刷新任务列表), 有可能该列表将为空
            // 因为自动刷新不能出现编号为29, 30, 33, 34, 35, 36的任务
            // 因此当该列表为空时, 则依旧返回上一次的任务列表
            newUserTaskList = taskPoolBiz.getNewUserTaskList(todayRemainTaskCodeSet, auto, user, customer);
            if (CollectionUtil.isEmpty(newUserTaskList)) {
                newUserTaskList = taskPoolBiz.getUserTaskList(user).get(0);

                // 作为一种兜底的方案
                if (CollectionUtil.isEmpty(newUserTaskList)) {
                    newUserTaskList = taskPoolBiz.getDefaultTaskList(user, customer);
                }
                needPopulateTaskStatus = true;
            }
        }

        taskPoolBiz.updateUserTaskList(newUserTaskList, user);
        List<ActUserTaskDto> actUserTaskDtoList = convertActTaskToActUserTaskDto(newUserTaskList);
        if (needPopulateTaskStatus) {
            populateTaskStatus(actUserTaskDtoList, actGoodsFlowList);
        }
        return new StatusObjDto<>(true, actUserTaskDtoList, StatusDto.SUCCESS, "");
    }


    private List<ActUserTaskDto> convertActTaskToActUserTaskDto(List<ActTask> actTaskList) {
        if (CollectionUtil.isEmpty(actTaskList)) {
            return new ArrayList<>();
        } else {
            List<ActUserTaskDto> actUserTaskDtoList = new ArrayList<>();
            for (ActTask actTask : actTaskList) {
                actUserTaskDtoList.add(new ActUserTaskDto(actTask));
            }
            return actUserTaskDtoList;
        }
    }

    /**
     * 计算任务状态
     *
     * @param actUserTaskDtoList
     * @param actGoodsFlowList
     */
    private void populateTaskStatus(List<ActUserTaskDto> actUserTaskDtoList, List<ActGoodsFlow> actGoodsFlowList) {
        if (CollectionUtil.isEmpty(actUserTaskDtoList) || CollectionUtil.isEmpty(actGoodsFlowList)) {
            return;
        }

        Map<String, List<ActGoodsFlow>> actGoodsFlowMap = actGoodsFlowList.stream()
                .collect(Collectors.groupingBy(ActGoodsFlow::getTaskCode));
        Map<String, ActTask> taskMap = taskPoolBiz.getActTaskMap();
        for (ActUserTaskDto actUserTaskDto : actUserTaskDtoList) {
            ActTask actTask = taskMap.get(actUserTaskDto.getCode());
            if (actTask == null) {
                continue;
            }

            // 主要针对解锁盘中宝任务和解锁公告全知道任务
            if (actUserTaskDto.getStatus() != null && actUserTaskDto.getStatus().equals(1)) {
                continue;
            }

            List<ActGoodsFlow> taskActGoodsFlowList = actGoodsFlowMap.get(actUserTaskDto.getCode());
            if (CollectionUtil.isEmpty(taskActGoodsFlowList)) {
                continue;
            }
            if (ActTask.ONCE_TASK.equals(actTask.getTaskType())) {
                // 若该任务为一次性任务, 且已有完成记录, 则该任务状态为已完成
                actUserTaskDto.setStatus(ActUserTaskDto.STATUS_FINISHED);
            } else if (ActTask.DAILY_TASK.equals(actTask.getTaskType())) {
                if (actTask.getAllTotalNum() != null && taskActGoodsFlowList.size() >= actTask.getAllTotalNum()) {
                    // 若该任务为每日任务, 且总次数限制数已达到, 则该任务状态为已完成
                    actUserTaskDto.setStatus(CodeConstant.CODE_YES);
                } else {
                    // 若该任务为每日任务, 且当日次数限制数已达到, 则该任务状态为已完成
                    Date now = new Date();
                    List<ActGoodsFlow> todayActGoodsFlowList = getTodayActGoodsFlowList(taskActGoodsFlowList, now);
                    if (CollectionUtil.isNotEmpty(todayActGoodsFlowList)
                            && todayActGoodsFlowList.size() >= actTask.getTotalNum()) {
                        actUserTaskDto.setStatus(CodeConstant.CODE_YES);
                    }
                }
            }
        }
    }


    @Override
    @Transactional
    public StatusObjDto<ActTaskStatusDto> doUserTask(String activityCode, String taskCode, String bizCode, String recommendMobile,
                                                     String mobile, AccTokenUser user, Customer customer, boolean isBatch) {
        log.info("用户完成任务, activityCode: {}, taskCode: {}, bizCode: {}, recommendMobile: {}, mobile: {}, user: {}, customer: {}, isBatch: {}",
                activityCode, taskCode, bizCode, recommendMobile, mobile,
                user.getUserId(), customer == null ? "" : customer.getCustomerId(), isBatch);

        // 该方法的入口有两个:
        // 1. 用户完成相关任务后, 如完成文章浏览, 由前端触发
        // 2. 用户的某些任务完成结果有一定的延迟, 例如开通北交所权限, 由定时任务接收到相关的数据后, 批量触发完成

        ActTaskStatusDto statusDto = new ActTaskStatusDto();
        statusDto.setStatus(CodeConstant.CODE_NO);

        // 1. 校验活动和任务是否有效
        Activity activity = activityBaseBiz.findActivity(activityCode);
        StatusDto actCheckResult = activityBaseBiz.isValid(activity);
        if (!actCheckResult.isOk() && !isBatch) {
            return new StatusObjDto<>(false, statusDto, actCheckResult.getCode(), actCheckResult.getMsg());
        }

        Map<String, ActTask> taskMap = taskPoolBiz.getActTaskMap();
        ActTask actTask = taskMap.get(taskCode);
        if (actTask == null) {
            return new StatusObjDto<>(false, statusDto, StatusDto.FAIL_COMMON, "活动任务不存在");
        }

        // 2. 校验任务是否已经完成: 总次数限制, 每日次数限制
        Date now = new Date();
        List<ActGoodsFlow> actGoodsFlowList = getActGoodsFlowList(activityCode, taskCode, mobile);
        checkTaskHasDone(actTask, actGoodsFlowList, now);

        // 3. 根据不同的任务校验相应的条件, 例如针对 bizCode 的校验, 需要与其他业务系统交互, 如查询订单等
        SpecificTaskBiz specificTaskBiz = getSpecificTaskBiz(taskCode);
        String source = "";
        if (specificTaskBiz != null) {
            Tuple checkResult = specificTaskBiz.checkTask(activity, actTask, actGoodsFlowList, bizCode,
                    mobile, user, customer, recommendMobile, isBatch);
            Boolean canBeDone = checkResult.get(0);
            if (!canBeDone) {
                return new StatusObjDto<>(false, statusDto, StatusDto.FAIL_COMMON, "任务完成条件不符合");
            }
            recommendMobile = checkResult.get(1);

            // 检查结果的第三个字段为渠道, 主要特定于企微添加任务
            if (checkResult.size() > 2) {
                source = checkResult.get(2);
            }

            // 对于使用七天体验券这些任务的校验会返回一个订单号, 或者企微添加任务的添加人手机号
            if (checkResult.size() > 3) {
                bizCode = checkResult.get(3);
            }
        }

        // 对于解锁盘中宝任务和解锁公告全知道任务, 需要特殊的标记以满足运营需求
        if (actTask.getCode().equals("TASK.2023DOUBLE11.29") || actTask.getCode().equals("TASK.2023DOUBLE11.30")) {
            UserTaskRedis.UserTask userTask = (UserTaskRedis.UserTask) UserTaskRedis.USER_TASK.get(user.getMobile());
            if (userTask != null) {
                String taskCodesStr = userTask.getTaskCodes();
                List<String> taskCodeList = Arrays.asList(taskCodesStr.split(","));
                if (taskCodeList.stream().anyMatch(e -> e.startsWith(actTask.getCode()))) {
                    String newTaskCodesStr = "";
                    for (String taskCodeStr : taskCodeList) {
                        if (!taskCodeStr.startsWith(actTask.getCode())) {
                            // 正常情形
                            newTaskCodesStr += taskCodeStr + ",";
                        } else if (!taskCodeStr.endsWith("*")) {
                            // 尚未做过该任务
                            newTaskCodesStr += taskCodeStr + "*" + ",";
                        } else {
                            // 已经做过该任务
                            newTaskCodesStr += taskCodeStr + ",";
                        }
                    }
                    if (newTaskCodesStr.endsWith(",")) {
                        newTaskCodesStr = newTaskCodesStr.substring(0, newTaskCodesStr.length() - 1);
                    }
                    userTask.setTaskCodes(newTaskCodesStr);
                    UserTaskRedis.USER_TASK.set(user.getMobile(), userTask);
                }
            }
        }

        String specialMobile = "18602705253";
        if (specialMobile.equals(recommendMobile)) {
            recommendMobile = "";
        }

        ActGoodsFlow newGoodsFlow = new ActGoodsFlow()
                .setMobile(mobile)
                .setCustomerId(customer == null ? "" : customer.getCustomerId())
                .setUserId(user == null ? "" : user.getUserId())
                .setActivityCode(activityCode)
                .setTaskCode(taskCode)
                .setBizCode(bizCode)
                .setCreateTime(now)
                .setUpdateTime(now)
                .setFlag(ActGoodsFlow.FLOW_FLAG_GET)
                .setGoodsCode(actTask.getGoodsCode())
                .setGoodsQuantity(actTask.getGoodsQuantity())
                // 设置推荐人手机号
                .setRecommendMobile(recommendMobile)
                .setSource(source);
        actGoodsFlowDao.insert(newGoodsFlow);

        ActGoodsRecord actGoodsRecord = new ActGoodsRecord()
                .setUserId(user == null ? "" : user.getUserId())
                .setCustomerId(customer == null ? "" : customer.getCustomerId())
                .setMobile(mobile)
                .setActivityCode("ACTIVITY.2023DOUBLE11.DHL")
                .setFlag(ActGoodsRecord.FLOW_FLAG_GET)
                .setGoodsCode(actTask.getGoodsCode())
                .setGoodsQuantity(actTask.getGoodsQuantity().longValue())
                .setRemark(actTask.getName())
                .setCreateTime(now);

        actGoodsRecordDao.insert(actGoodsRecord);
        statusDto.setStatus(CodeConstant.CODE_YES);
        return new StatusObjDto<>(true, statusDto, StatusDto.SUCCESS, "");
    }


    private void checkTaskHasDone(ActTask actTask, List<ActGoodsFlow> actGoodsFlowList, Date now) {
        if (actTask.getAllTotalNum() != null) {
            if (actGoodsFlowList.size() >= actTask.getAllTotalNum()) {
                throw ActivityBizException.ACT_TASK_HAS_DONE;
            }
        }

        if (ActTask.DAILY_TASK.equals(actTask.getTaskType())) {
            List<ActGoodsFlow> todayActGoodsFlowList = getTodayActGoodsFlowList(actGoodsFlowList, now);
            if (CollectionUtil.isNotEmpty(todayActGoodsFlowList) && todayActGoodsFlowList.size() >= actTask.getTotalNum()) {
                throw ACT_TASK_TODAY_HAS_DONE;
            }
        }
    }


    private List<ActGoodsFlow> getActGoodsFlowList(String activityCode, String taskCode, String mobile) {
        ActGoodsFlow qryActGoodsFlow = new ActGoodsFlow();
        qryActGoodsFlow.setActivityCode(activityCode);
        qryActGoodsFlow.setTaskCode(taskCode);
        qryActGoodsFlow.setMobile(mobile);
        List<ActGoodsFlow> actGoodsFlowList = actGoodsFlowDao.list(qryActGoodsFlow);
        return actGoodsFlowList == null ? new ArrayList<>() : actGoodsFlowList;
    }

    private List<ActGoodsFlow> getTodayActGoodsFlowList(List<ActGoodsFlow> actGoodsFlowList, Date now) {
        return actGoodsFlowList.stream().filter(actGoodsFlow -> DateUtil.isSameDay(actGoodsFlow.getCreateTime(), now))
                .collect(Collectors.toList());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
