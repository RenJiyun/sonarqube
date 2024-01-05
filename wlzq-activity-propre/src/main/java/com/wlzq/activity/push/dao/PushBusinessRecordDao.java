/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.push.dao;

import com.wlzq.activity.push.model.PushBusinessRecord;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 业务推送纪录DAO接口
 * @author cjz
 * @version 2021-04-21
 */
@MybatisScan
public interface PushBusinessRecordDao extends CrudDao<PushBusinessRecord> {
	
}