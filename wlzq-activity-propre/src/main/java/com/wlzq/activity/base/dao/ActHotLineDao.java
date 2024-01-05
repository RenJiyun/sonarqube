package com.wlzq.activity.base.dao;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.model.ActHotLine;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

@MybatisScan
public interface ActHotLineDao extends CrudDao<ActHotLine> {
	
	ActHotLine getByCustomerId(@Param("customerId") String customerId);
	
}
