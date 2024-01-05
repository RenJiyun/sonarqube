/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.double11.dao;

import com.wlzq.activity.couponreceive.model.CouponRecieve;
import com.wlzq.activity.double11.dto.*;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 双11销售排行榜DAO接口
 * @author zhujt
 * @version 2022-10-27
 */
@MybatisScan
public interface Double11Dao extends CrudDao<Double11AchievementDto> {

	List<PersonalSprintAward> findPersonalSprintAwardList(@Param("beginDate") Date beginDate, @Param("endDate")Date endDate);

	List<PersonalOutbreakAward> findPersonalOutbreakAwardList(@Param("beginDate") Date beginDate, @Param("endDate")Date endDate);

	List<ThsAdviserTeamSprintAward> findThsAdviserTeamSprintAwardList(@Param("beginDate") Date beginDate, @Param("endDate")Date endDate);

	List<ThsAdviserTeamOutbreakAward> findThsAdviserTeamOutbreakAwardList(@Param("beginDate") Date beginDate, @Param("endDate")Date endDate);

	List<DecisionSaleCommunicationAward> findDecisionSaleCommunicationAwardList(@Param("beginDate") Date beginDate, @Param("endDate")Date endDate);
}