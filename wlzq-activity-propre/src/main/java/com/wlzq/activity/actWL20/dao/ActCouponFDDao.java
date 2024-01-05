package com.wlzq.activity.actWL20.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * @author jjw
 */
@MybatisScan
public interface ActCouponFDDao extends CrudDao<Customer> {
	
	//福袋领取查询
	List<ActPrize> findRecieves(@Param("activityCode") String activityCode, @Param("customerId") String customerId);
	
}