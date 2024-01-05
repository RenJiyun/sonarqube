package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.push.dto.StaffPushDto;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Activity DAO类
 * @author 
 * @version 1.0
 */
@MybatisScan
@Order(1)
public interface ActivityDao{
	
	Activity findActivityByCode(String code);
	
	void update(Activity activity);
	
	List<Activity> findList(@Param("code")String code);

	List<Activity> findListByGroupCode(@Param("groupCode")String groupCode);

	List<StaffPushDto> findStaffPushList(@Param("startTime") String startTime,@Param("endTime") String endTime);

	/**
	 * 找出非总部的员工列表
	 */
	List<StaffPushDto> findBranchStaff();

}
