package com.wlzq.activity.stockcourse.biz;

import com.wlzq.activity.stockcourse.model.StockCourseUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 股票课活动接口
 * @author zjt
 *
 */
public interface StockCourseBiz {
    /**
     * 注册
     * @param stockCourseUser
     * @return
     */
	StatusDto registration(StockCourseUser stockCourseUser,Integer type);
 
	/**
	 * 查询学习进度
	 * @param mobile
	 * @return
	 */
	StatusObjDto<StockCourseUser> learningprogress(String mobile);
    
	/**
	 * 检查领券用户
	 * @param mobile
	 * @return
	 */
	StatusDto stockCourseUserCheck(String mobile,Customer customer);
    
	/**
	 * 上课提醒微信推送
	 */
	void stockCourseWechatPush();
    
	/**
	 * 上课提醒app推送
	 */
	void stockCourseAppPush();
    
	/**
	 * 是否已经注册
	 * @param mobile
	 * @return
	 */
	StatusObjDto<Boolean> isRegister(String mobile);

}
