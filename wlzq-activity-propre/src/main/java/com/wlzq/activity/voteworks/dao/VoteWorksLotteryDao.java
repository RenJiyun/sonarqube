/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.voteworks.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.base.dto.WinDto;
import com.wlzq.activity.voteworks.model.VoteWorksLottery;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 抽奖记录DAO接口
 * @author louie
 * @version 2018-08-10
 */
@MybatisScan
public interface VoteWorksLotteryDao extends CrudDao<VoteWorksLottery> {
	/**
	 * 抽奖次数查询
	 * @param lottery
	 * @return
	 */
	Integer count(VoteWorksLottery lottery);
	/**
	 * openid与id查询抽奖记录
	 * @param lottery
	 * @return
	 */
	VoteWorksLottery findByOpenIdAndCode(VoteWorksLottery lottery);
	
	/**
	 * 中奖列表
	 * @param start
	 * @param end
	 * @return
	 */
	List<WinDto> findPrizes(@Param("activitycode") String activitycode, @Param("start")Integer start,@Param("end")Integer end);
	
	/**
	 * 查询未发送消息中奖信息
	 * @param start
	 * @param end
	 * @return
	 */
	List<VoteWorksLottery> findNotSend(@Param("activitycode") String activitycode, @Param("start")Integer start,@Param("end")Integer end);
	
	/**
	 * 更新状态为已发送短信提示
	 * @param lottery
	 * @return
	 */
	int updateToSend(VoteWorksLottery lottery);
}