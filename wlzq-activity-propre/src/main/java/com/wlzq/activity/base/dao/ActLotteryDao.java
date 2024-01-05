/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.dao;

import java.util.List;

import com.wlzq.activity.base.model.ActPrize;
import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.dto.WinDto;
import com.wlzq.activity.base.model.ActLottery;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 抽奖记录DAO接口
 * @author louie
 * @version 2018-11-30
 */
@MybatisScan
public interface ActLotteryDao extends CrudDao<ActLottery> {
	int findCount(ActLottery lottery);
	ActLottery findByRecieveCode(String recieveCode);
	
	/**
	 * 中奖列表
	 * @param start
	 * @param end
	 * @return
	 */
	List<WinDto> findPrizes(@Param("activitycode") String activitycode, @Param("start")Integer start,@Param("end")Integer end);


}