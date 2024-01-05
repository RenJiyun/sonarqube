package com.wlzq.activity.virtualfin.dao;

import com.wlzq.activity.double11.dto.BranchRankingDto;
import com.wlzq.activity.double11.dto.MyRankingInfoDto;
import com.wlzq.activity.double11.dto.SaleRankingDto;
import com.wlzq.activity.double11.dto.TaskIntegralInfo;
import com.wlzq.activity.virtualfin.dto.LastAmountFlowResDto;
import com.wlzq.core.Page;
import org.apache.ibatis.annotations.Param;

import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

import java.util.Date;
import java.util.List;

/**
 * @author renjiyun
 */
@MybatisScan
public interface ActGoodsFlowDao extends CrudDao<ActGoodsFlow> {
	/**
	 * getBalance
	 * @param actGoodsFlow
	 * @return
	 */
	Double getBalance(ActGoodsFlow actGoodsFlow);

	/**
	 * getDoTaskCount
	 * @param activityCode
	 * @param taskCode
	 * @param mobile
	 * @param isDaily
	 * @return
	 */
	Integer getDoTaskCount(@Param("activityCode") String activityCode, @Param("taskCode") String taskCode,
			@Param("mobile") String mobile, @Param("isDaily") Integer isDaily);

	/**
	 * list
	 * @param goodsFlow
	 * @return
	 */
	List<ActGoodsFlow> list(ActGoodsFlow goodsFlow);

	/**
	 * getLastAmountFlow
	 * @param activityCode
	 * @return
	 */
	List<LastAmountFlowResDto> getLastAmountFlow(@Param("activityCode") String activityCode);

	/**
	 * getAllByBizCodes
	 * @param activityCode
	 * @param userId
	 * @param goodsCode
	 * @return
	 */
	List<String> getAllByBizCodes(@Param("activityCode") String activityCode, @Param("userId") String userId, @Param("goodsCode") String goodsCode);

	/**
	 * 一马当先奖
	 * @param startTime
	 * @param endTime
	 * @param activityCode
	 * @return
	 */
	List<SaleRankingDto> saleRanking(@Param("startTime")Date startTime, @Param("endTime")Date endTime, @Param("activityCode")String activityCode);

	/**
	 * 巅峰登顶奖
	 * @param startTime
	 * @param endTime
	 * @param activityCode
	 * @return
	 */
	List<BranchRankingDto> branchRanking(@Param("startTime")Date startTime, @Param("endTime")Date endTime, @Param("activityCode")String activityCode);

	/**
	 * 我的排名情况
	 * @param startTime
	 * @param endTime
	 * @param activityCode
	 * @param recommendMobile
	 * @return
	 */
	MyRankingInfoDto myRankingInfo(@Param("startTime")Date startTime, @Param("endTime")Date endTime, @Param("activityCode")String activityCode,@Param("recommendMobile")String recommendMobile);

	/**
	 * 我的积分明细
	 * @param startTime
	 * @param endTime
	 * @param activityCode
	 * @param recommendMobile
	 * @param page
	 * @return
	 */
    List<TaskIntegralInfo> myTaskIntegralInfo(@Param("startTime")Date startTime, @Param("endTime")Date endTime, @Param("activityCode")String activityCode,@Param("recommendMobile")String recommendMobile, @Param("page")Page page);
}
