package com.wlzq.activity.virtualfin.dao;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.virtualfin.model.ActFinProduct;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

@MybatisScan
public interface ActFinProductDao extends CrudDao<ActFinProduct> {
	
	ActFinProduct findByCode(@Param("activityCode")String activityCode, @Param("productCode")String productCode);
	
}
