package com.wlzq.activity.actWL20.dao;

import com.wlzq.activity.actWL20.model.ActGiftBox;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * @author jjw
 */
@MybatisScan
public interface GiftBoxDao extends CrudDao<ActGiftBox> {
	
}