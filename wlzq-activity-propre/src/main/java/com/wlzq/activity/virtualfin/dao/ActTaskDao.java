package com.wlzq.activity.virtualfin.dao;

import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wlzq
 */
@MybatisScan
public interface ActTaskDao extends CrudDao<ActTask> {

    /**
     * 根据活动编码和任务编码查询任务
     *
     * @param activityCode
     * @param taskCode
     * @return
     */
    ActTask findByCode(@Param("activityCode") String activityCode, @Param("taskCode") String taskCode);

    /**
     * 获取指定活动下的所有任务
     *
     * @param activityCode
     * @return
     */
    List<ActTask> findByActivityCode(@Param("activityCode") String activityCode);

}
