/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.stockcourse.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.stockcourse.model.StockCourseStatus;
import com.wlzq.activity.stockcourse.model.StockCourseUser;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 股票课活动DAO接口
 * @author zjt
 * @version 2021-10-25
 */
@MybatisScan
public interface StockCourseUserDao extends CrudDao<StockCourseUser> {

	StockCourseUser getByMobile(String mobile);

	StockCourseUser getByMobileAndTemplateCode(@Param("mobile")String mobile, @Param("templateCode")String templateCode);

	List<StockCourseUser> getUserToPush(@Param("classOpenDate")Date classOpenDate,@Param("pushTime")Date pushTime);

	int insertStockCourseStatus(StockCourseStatus stockCourseStatus);

	int updateStockCourseStatus(StockCourseStatus stockCourseStatus);

	List<StockCourseStatus> findCouseStatusList(@Param("mobile")String mobile,@Param("classOpenDate")Date classOpenDate);
	
}