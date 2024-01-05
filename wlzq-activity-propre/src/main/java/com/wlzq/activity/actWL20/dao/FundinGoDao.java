package com.wlzq.activity.actWL20.dao;

import java.util.List;

import com.wlzq.activity.actWL20.model.ActFundinGo;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * @author jjw
 */
@MybatisScan
public interface FundinGoDao extends CrudDao<Customer> {
	
	//查询不满足条件的入金记录
	List<ActFundinGo> findNotAvailableFundinGo();
	
	//更新入金记录
	void update(ActFundinGo actFundinGo);
}