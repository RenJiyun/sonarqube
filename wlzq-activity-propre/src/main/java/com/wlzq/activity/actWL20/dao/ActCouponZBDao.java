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
public interface ActCouponZBDao extends CrudDao<Customer> {
	
	//领取奖品查询
	List<ActPrize> findPrizeByUserId(@Param("activityCode") String activityCode, @Param("userId") String userId, @Param("customerId") String customerId);
	
	//查询活动奖品
	List<ActPrize> findPrizeTypeCode(@Param("activityCode") String activityCode);
	
}