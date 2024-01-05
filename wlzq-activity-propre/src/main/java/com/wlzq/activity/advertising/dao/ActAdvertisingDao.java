/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.advertising.dao;

import com.wlzq.activity.advertising.model.ActAdvertising;
import com.wlzq.activity.guess.dto.MarketUserDto;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import java.util.List;
/**
 * 收集投放广告手机号记录DAO接口
 * @author pjw
 * @version 2021-07-16
 */
@MybatisScan
public interface ActAdvertisingDao extends CrudDao<ActAdvertising> {
    List<MarketUserDto> findListMarketUser(MarketUserDto marketUserDto);
}