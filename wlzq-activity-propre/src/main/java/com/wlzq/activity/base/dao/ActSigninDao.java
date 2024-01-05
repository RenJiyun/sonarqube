package com.wlzq.activity.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.model.ActSignin;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 活动签到DAO接口
 * @author cjz
 * @version 2018-06-12
 */
@MybatisScan
public interface ActSigninDao extends CrudDao<ActSignin> {
	
	/**
	 * 查找包含微信信息的签到列表
	 * @param actSignin
	 * @return
	 */
	List<ActSignin> findWechatInfoList(ActSignin actSignin);
	
	/**
	 * 查找所有有效的签到列表
	 * @param activityCode
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	List<ActSignin> findValidSignInList(@Param("activityCode") String activityCode, @Param("start") Integer pageIndex, @Param("end") Integer pageSize);
	
	Integer deleteByActCode(@Param("actCode") String actCode);
	
	/**
	 * 查找最小id
	 * @param actSignin
	 * @return
	 */
	Integer findMinId(ActSignin actSignin);
	
	List<ActSignin> findSigninListCodeNotNull(@Param("actCode") String actCode, @Param("maxOrder") Integer maxOrder, @Param("maxLength") Integer maxLength);
	
	int updateInvalid(ActSignin actSignin);
}