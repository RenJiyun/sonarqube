package com.wlzq.activity.task.biz;

/**
 * @author renjiyun
 */
public interface TaskBatchChkBiz {
    /**
     * 批量校验北交所权限
     *
     * @param checkDate
     */
    void checkBjsPerm(String checkDate);

    /**
     * 批量校验融金流水
     *
     * @param checkDate
     */
    void checkRjFlow(String checkDate);
}
