/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.redenvelope.dao;

import com.wlzq.activity.redenvelope.model.RedEnvelope;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 红包DAO接口
 * @author louie
 * @version 2018-04-17
 */
@MybatisScan
public interface RedEnvelopeDao extends CrudDao<RedEnvelope> {
	RedEnvelope findByOrderNo(String orderNo);
	RedEnvelope findByBusinessNo(String businessNo);
}