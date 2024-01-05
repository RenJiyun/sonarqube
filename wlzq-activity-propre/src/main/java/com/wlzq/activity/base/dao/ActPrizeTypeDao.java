/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * @author louie
 */
@MybatisScan
public interface ActPrizeTypeDao extends CrudDao<ActPrizeType> {
    ActPrizeType findByCode(String code);
}