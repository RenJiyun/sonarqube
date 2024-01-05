package com.wlzq.activity.task.dao;

import com.wlzq.activity.task.model.ActBjsOpenRecord;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author renjiyun
 */
@MybatisScan
public interface ActBjsOpenRecordDao extends CrudDao<ActBjsOpenRecord> {
    /**
     * 查询交易日期在指定范围内的开通记录, 范围为: (lastHandleDate, lastSyncDate]
     *
     * @param lastHandleDate
     * @param lastSyncDate
     * @return
     */
    List<ActBjsOpenRecord> queryByDate(@Param("lastHandleDate") String lastHandleDate, @Param("lastSyncDate") String lastSyncDate);
}
