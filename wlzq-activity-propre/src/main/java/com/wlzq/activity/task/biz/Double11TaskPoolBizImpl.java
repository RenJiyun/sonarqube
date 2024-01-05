package com.wlzq.activity.task.biz;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.activity.task.redis.TaskRedis;
import com.wlzq.activity.task.redis.UserTaskRedis;
import com.wlzq.activity.virtualfin.dao.ActTaskDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.wlzq.activity.task.redis.TaskRedis.TASKS_2023DOUBLE11;

/**
 * 2023年双十一攒积分活动任务池
 *
 * @author renjiyun
 */
@Service
public class Double11TaskPoolBizImpl implements TaskPoolBiz {

    @Autowired
    private ActTaskDao actTaskDao;

    @Autowired
    @Qualifier("userTaskSelectionByWeightRandomImpl")
    private UserTaskSelectionBiz userTaskSelectionBiz;


    /** 系统默认的任务列表, 用户首次进入活动页面时, 会展示这些任务 */
    private static final String[] DEFAULT_TASK_CODES = new String[]{
            "TASK.2023DOUBLE11.31",
            "TASK.2023DOUBLE11.03",
            "TASK.2023DOUBLE11.17",
            "TASK.2023DOUBLE11.37",
            "TASK.2023DOUBLE11.32",
            "TASK.2023DOUBLE11.29",
            "TASK.2023DOUBLE11.19",
            "TASK.2023DOUBLE11.22"
    };

    /** 自动为用户刷新任务列表时, 需要排除的任务 */
    private static final String[] AUTO_REFRESH_EXCLUDE_TASK_CODES = new String[]{
            "TASK.2023DOUBLE11.29",
            "TASK.2023DOUBLE11.30",
            "TASK.2023DOUBLE11.33",
            "TASK.2023DOUBLE11.34",
            "TASK.2023DOUBLE11.35",
            "TASK.2023DOUBLE11.36"
    };

    @Override
    public List<ActTask> getDefaultTaskList() {
        Map<String, ActTask> actTaskMap = getActTaskMap();
        List<ActTask> defaultTaskList = new ArrayList<>();
        for (String taskCode : DEFAULT_TASK_CODES) {
            ActTask actTask = actTaskMap.get(taskCode);
            if (actTask != null) {
                defaultTaskList.add(actTask);
            }
        }
        return defaultTaskList;
    }

    @Override
    public List<ActTask> getDefaultTaskList(AccTokenUser user, Customer customer) {
        return getDefaultTaskList();
    }


    public Map<String, ActTask> getActTaskMap() {
        TaskRedis.ActTaskMap actTaskMap = (TaskRedis.ActTaskMap) TASKS_2023DOUBLE11.get("taskmap");
        if (actTaskMap == null || CollectionUtil.isEmpty(actTaskMap.getActTaskMap())) {
            // 未设置缓存
            ActTask qryActTask = new ActTask();
            qryActTask.setActivityCode(Double11Biz.ACTIVITY_2023DOUBLE11_ZJF);
            qryActTask.setStatus(CodeConstant.CODE_YES);
            List<ActTask> actTaskList = actTaskDao.findList(qryActTask);
            if (CollectionUtil.isNotEmpty(actTaskList)) {
                Map<String, ActTask> newActTaskMap = new HashMap<>();
                for (ActTask actTask : actTaskList) {
                    newActTaskMap.put(actTask.getCode(), actTask);
                }

                actTaskMap = new TaskRedis.ActTaskMap();
                actTaskMap.setActTaskMap(newActTaskMap);
                TASKS_2023DOUBLE11.set("taskmap", actTaskMap);
                return newActTaskMap;
            } else {
                return Collections.emptyMap();
            }
        } else {
            return actTaskMap.getActTaskMap();
        }
    }


    @Override
    public Tuple getRemainTasks(List<ActGoodsFlow> actGoodsFlowList, AccTokenUser user, Customer customer) {
        Map<String, ActTask> actTaskMap = getActTaskMap();
        Set<String> remainTaskCodeSet = new HashSet<>(actTaskMap.keySet());

        // 若没有任何任务完成记录, 则返回所有任务
        if (CollectionUtil.isEmpty(actGoodsFlowList)) {
            return new Tuple(remainTaskCodeSet, remainTaskCodeSet);
        }
        // 排除已完成的任务
        Set<String> todayRemainTaskCodeSet = new HashSet<>(remainTaskCodeSet);
        Map<String, List<ActGoodsFlow>> actGoodsFlowMap = actGoodsFlowList.stream()
                .collect(Collectors.groupingBy(ActGoodsFlow::getTaskCode));
        Date now = new Date();
        Date todayStart = DateUtils.getDayStart(now);
        Date todayEnd = DateUtils.getDayEnd(now);
        for (Map.Entry<String, List<ActGoodsFlow>> entry : actGoodsFlowMap.entrySet()) {
            String taskCode = entry.getKey();
            List<ActGoodsFlow> goodsFlowList = entry.getValue();
            List<ActGoodsFlow> todayGoodsFlowList = goodsFlowList.stream()
                    .filter(goodsFlow -> goodsFlow.getCreateTime().after(todayStart) && goodsFlow.getCreateTime().before(todayEnd))
                    .collect(Collectors.toList());
            ActTask actTask = actTaskMap.get(taskCode);
            if (actTask == null) {
                continue;
            }

            int totalCompleteNum = goodsFlowList.size();
            if (totalCompleteNum >= actTask.getAllTotalNum()) {
                remainTaskCodeSet.remove(taskCode);
                todayRemainTaskCodeSet.remove(taskCode);
            }

            int todayCompleteNum = todayGoodsFlowList.size();
            if (todayCompleteNum >= actTask.getTotalNum()) {
                todayRemainTaskCodeSet.remove(taskCode);
            }
        }
        return new Tuple(remainTaskCodeSet, todayRemainTaskCodeSet);
    }

    @Override
    public List<ActTask> getNewUserTaskList(Set<String> candidateTaskCodes, boolean auto, AccTokenUser user, Customer customer) {
        // assert user != null
        if (auto) {
            // 自动刷新任务列表时, 排除指定的任务
            Arrays.asList(AUTO_REFRESH_EXCLUDE_TASK_CODES).forEach(candidateTaskCodes::remove);
        }

        List<ActTask> candidateTaskList = new ArrayList<>();
        Map<String, ActTask> actTaskMap = getActTaskMap();
        for (String taskCode : candidateTaskCodes) {
            ActTask actTask = actTaskMap.get(taskCode);
            if (actTask != null) {
                candidateTaskList.add(actTask);
            }
        }
        return userTaskSelectionBiz.select(candidateTaskList, user, customer);
    }

    @Override
    public void updateUserTaskList(List<ActTask> newUserTaskList, AccTokenUser user) {
        if (CollectionUtil.isEmpty(newUserTaskList)) {
            return;
        }
        String taskCodes = newUserTaskList.stream().map(ActTask::getCode).collect(Collectors.joining(","));
        UserTaskRedis.UserTask userTask = new UserTaskRedis.UserTask();
        userTask.setTaskCodes(taskCodes);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        userTask.setCreateDate(dateFormat.format(new Date()));
        UserTaskRedis.USER_TASK.set(user.getMobile(), userTask);
    }

    @Override
    public Tuple getUserTaskList(AccTokenUser user) {
        UserTaskRedis.UserTask userTask = (UserTaskRedis.UserTask) UserTaskRedis.USER_TASK.get(user.getMobile());
        if (userTask == null || ObjectUtils.isEmptyOrNull(userTask.getTaskCodes())) {
            return new Tuple(new ArrayList<>(), new Date());
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Map<String, ActTask> actTaskMap = getActTaskMap();
        List<ActTask> actTaskList = Arrays.stream(userTask.getTaskCodes().split(","))
                .map(code -> {
                    if (code.endsWith("*")) {
                        code = code.substring(0, code.length() - 1);
                        ActTask actTask = actTaskMap.get(code);
                        return new ActTask()
                                .setCode(actTask.getCode())
                                .setName(actTask.getName())
                                .setActivityCode(actTask.getActivityCode())
                                .setGoodsCode(actTask.getGoodsCode())
                                .setGoodsQuantity(actTask.getGoodsQuantity())
                                .setUrl(actTask.getUrl())
                                .setCustomerTask(actTask.getCustomerTask())
                                .setRemark(actTask.getRemark())
                                .setUserTaskStatus(1);
                    } else {
                        return actTaskMap.get(code);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            return new Tuple(actTaskList, dateFormat.parse(userTask.getCreateDate()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
