package com.wlzq.activity.task.dao;

import com.wlzq.activity.task.model.ActReturnVisitRecord;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author renjiyun
 */
@MybatisScan
public interface ActReturnVisitRecordDao extends CrudDao<ActReturnVisitRecord> {
    /**
     * 根据客户号和回访任务编码查询回访记录
     *
     * @param customerId
     * @param rvTaskNo
     * @return
     */
    List<ActReturnVisitRecord> findByCustomerIdAndTaskNo(@Param("customerId") String customerId, @Param("rvTaskNo") String rvTaskNo);
}
