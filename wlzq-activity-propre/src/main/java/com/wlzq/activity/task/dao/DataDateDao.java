package com.wlzq.activity.task.dao;

import com.wlzq.activity.task.model.DataDate;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

/**
 * @author renjiyun
 */
@MybatisScan
public interface DataDateDao extends CrudDao<DataDate> {

    /**
     * 获取最新的数据日期
     *
     * @param type
     * @return
     */
    DataDate getLastDataDate(@Param("type") Integer type);
}
