/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.couponreceive.dao;

import com.wlzq.activity.couponreceive.model.CouponRecieve;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 优惠券领取DAO接口
 * @author louie
 * @version 2019-05-24
 */
@MybatisScan
public interface CouponRecieveDao extends CrudDao<CouponRecieve> {
	Integer findCount(CouponRecieve recieve);
}