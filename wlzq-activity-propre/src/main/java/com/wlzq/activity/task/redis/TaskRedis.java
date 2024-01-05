package com.wlzq.activity.task.redis;

import com.wlzq.activity.base.redis.CouponCommonRedis;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.service.base.sys.RedisFacadeAbstract;

import java.io.Serializable;
import java.util.Map;

/**
 * @author qiaofeng
 */
public class TaskRedis extends RedisFacadeAbstract {

    /** 2023年双十一任务缓存 */
    public static final TaskRedis TASKS_2023DOUBLE11 = new TaskRedis("activity:task:2023double11:", 30 * 24 * 3600);

    /** 完成任务锁, 防止用户多次点击完成任务 */
    public static final TaskRedis TASK_LOCK = new TaskRedis("activity:task:lock",60);

    private String redisPrefix;
    private int timeoutSeconds;

    public TaskRedis(String redisPrefix, int timeoutSeconds) {
        this.redisPrefix = redisPrefix;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    protected String getRedisPrefix() {
        return redisPrefix;
    }

    @Override
    protected int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public static class ActTaskMap implements Serializable {

        private static final long serialVersionUID = -7659950551748211958L;
        private Map<String, ActTask> actTaskMap;

        public Map<String, ActTask> getActTaskMap() {
            return actTaskMap;
        }

        public void setActTaskMap(Map<String, ActTask> actTaskMap) {
            this.actTaskMap = actTaskMap;
        }
    }
}
