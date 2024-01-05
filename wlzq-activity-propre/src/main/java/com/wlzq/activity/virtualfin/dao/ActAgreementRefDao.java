/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.virtualfin.dao;

import com.wlzq.activity.virtualfin.model.ActAgreementRef;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动理财订单DAO接口
 * @author zhaozx
 * @version 2020-07-27
 */
@MybatisScan
public interface ActAgreementRefDao extends CrudDao<ActAgreementRef> {
	
	Integer deleteByProductCode(ActAgreementRef actAgreementRef);
}