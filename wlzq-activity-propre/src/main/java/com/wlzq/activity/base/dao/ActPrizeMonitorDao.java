package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.ActPrizeMonitor;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MybatisScan
public interface ActPrizeMonitorDao extends CrudDao<ActPrizeMonitor> {
    /**
     * 查询所有设置了警戒值的状态为1的优惠券
     * @return
     */
    List<ActPrizeMonitor> selectAll();

    /**
     *
     * @param code
     * @return
     */
    List<String> selectNameByCode(@Param("code")String code);
    /**
     * 已发短信进行提示的优惠券状态修改为2
     * @param template
     * @return
     */
    Integer setStatus(@Param("code") String code);
}
