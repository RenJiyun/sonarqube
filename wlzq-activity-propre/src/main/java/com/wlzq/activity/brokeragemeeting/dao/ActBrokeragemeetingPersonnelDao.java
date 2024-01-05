/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.brokeragemeeting.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingPersonnel;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 经纪业务会议人员列表DAO接口
 * @author cjz
 * @version 2019-01-17
 */
@MybatisScan
public interface ActBrokeragemeetingPersonnelDao extends CrudDao<ActBrokeragemeetingPersonnel> {
	
	public Integer findMaxId();
	
	public void alterStep(@Param("step") Integer step);
	
	public List<ActBrokeragemeetingPersonnel> findNeedUnveilingSigninList();
	
	public List<ActBrokeragemeetingPersonnel> findListSigninList(@Param("maxOrder") Integer maxOrder, @Param("maxLength") Integer maxLength);
	
	public Integer clearSigninCode();
	
	public Integer clearUnveiled();
}