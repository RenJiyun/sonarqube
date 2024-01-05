/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.ActActivityVoucher;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动奖品凭证DAO接口
 * @author cjz
 * @version 2018-11-13
 */
@MybatisScan
public interface ActActivityVoucherDao extends CrudDao<ActActivityVoucher> {
	
}