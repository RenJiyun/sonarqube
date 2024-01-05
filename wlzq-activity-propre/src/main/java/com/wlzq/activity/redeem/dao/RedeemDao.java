package com.wlzq.activity.redeem.dao;

import java.util.Map;

import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 兑换码DAO接口
 * @author louie
 * @version 2017-09-25
 */
@MybatisScan
public interface RedeemDao extends CrudDao<Redeem> {
	Redeem findNotOccupy(String typeCode);

	Redeem findByCode(String code);
	
	Redeem findByMobile(Map params);
}