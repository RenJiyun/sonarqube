package com.wlzq.activity.redeem.dao;

import com.wlzq.activity.redeem.model.RedeemReceiveRecord;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 兑换码领取记录DAO接口
 * @author louie
 * @version 2017-09-25
 */
@MybatisScan
public interface RedeemReceiveRecordDao extends CrudDao<RedeemReceiveRecord> {
}