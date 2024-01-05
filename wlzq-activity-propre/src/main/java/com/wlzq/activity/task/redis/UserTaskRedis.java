package com.wlzq.activity.task.redis;

import com.wlzq.service.base.sys.RedisFacadeAbstract;

import java.io.Serializable;

/**
 * 用户任务列表缓存
 *
 * @author renjiyun
 */
public class UserTaskRedis extends RedisFacadeAbstract {

    public static final UserTaskRedis USER_TASK = new UserTaskRedis("activity:task:user:", 30 * 24 * 3600);

    private String redisPrefix;
    private int timeoutSeconds;

    public UserTaskRedis(String redisPrefix, int timeoutSeconds) {
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

    public static class UserTask implements Serializable {
        private static final long serialVersionUID = -2394894446084467768L;
        private String taskCodes;
        private String createDate;

        public String getTaskCodes() {
            return taskCodes;
        }

        public void setTaskCodes(String taskCodes) {
            this.taskCodes = taskCodes;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }
    }

}
