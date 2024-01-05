package com.wlzq.activity.center.dao;

import com.wlzq.activity.center.model.ActCenter;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MybatisScan
public interface ActCenterDao extends CrudDao<ActCenter> {

    /*未删除、当前客户端可见的、活动结束时间不超过90天*/
    List<ActCenter> getActlist(@Param("visibility") List<Integer> visibility, @Param("days") Integer days);
}
