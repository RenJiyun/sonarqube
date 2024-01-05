package com.wlzq.activity.bill.dao;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.bill.model.ActBill;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * @author jjw
 */
@MybatisScan
public interface ActBillDao extends CrudDao<Customer> {
	
	ActBill findBillByCustomerId(@Param("clientId") String customerId);
	
	void updateBillByCustomerId(ActBill actBill);
	
}