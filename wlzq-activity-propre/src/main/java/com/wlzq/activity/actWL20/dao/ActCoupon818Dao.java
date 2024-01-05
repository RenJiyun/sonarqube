package com.wlzq.activity.actWL20.dao;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.actWL20.model.ActFundinGo;
import com.wlzq.activity.actWL20.model.ActSubscribe;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * @author jjw
 */
@MybatisScan
public interface ActCoupon818Dao extends CrudDao<Customer> {
	
	//入金登记
	int addFundinGo(Customer customer);
	
	//入金登记查询
	ActFundinGo findFundinGo(@Param("customerId") String customerId);
	
	//活动订阅
	int addSubscribe(@Param("activityCode") String activityCode, @Param("mobile") String mobile, @Param("customerId") String customerId);
	
	//活动订阅查询
	ActSubscribe findSubscribe(@Param("activityCode") String activityCode, @Param("mobile") String mobile);
}