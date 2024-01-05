package com.wlzq.activity.task.dao;

import com.wlzq.activity.task.model.ActNoActBankTransfer;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author renjiyun
 */
@MybatisScan
public interface ActNoActBankTransferDao extends CrudDao<ActNoActBankTransfer> {
    /**
     * 查询指定日期区间内的流水数据
     *
     * @param lastHandleDate
     * @param lastSyncDate
     * @return
     */
    List<ActNoActBankTransfer> queryByDate(@Param("lastHandleDate") String lastHandleDate, @Param("lastSyncDate") String lastSyncDate);
}
