package com.wlzq.activity.expoturntable.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.expoturntable.model.ActFinexpo19Shake;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 金交会2019摇一摇DAO接口
 * @author cjz
 * @version 2019-06-13
 */
@MybatisScan
public interface ActFinexpo19ShakeDao extends CrudDao<ActFinexpo19Shake> {
	
	Integer getSum(ActFinexpo19Shake actFinexpo19Shake);
	
	int updateInvalid();
	
	List<Map<String, Object>> findListByPlayerIds(ActFinexpo19Shake actFinexpo19Shake);
	
	List<Map<String, Object>> findSigninListCodeNotNull(@Param("scene") Integer scene, @Param("maxOrder") Integer maxOrder, @Param("maxLength") Integer maxLength);
}