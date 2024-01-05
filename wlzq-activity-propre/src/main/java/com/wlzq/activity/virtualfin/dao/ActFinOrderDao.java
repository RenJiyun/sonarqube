package com.wlzq.activity.virtualfin.dao;

import com.wlzq.activity.virtualfin.dto.LastAmountFlowResDto;
import com.wlzq.activity.virtualfin.model.ActFinOrder;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@MybatisScan
public interface ActFinOrderDao extends CrudDao<ActFinOrder> {
    List<LastAmountFlowResDto> getLastAmountFlow(@Param("activityCode") String activityCode);

    BigDecimal sumAmountByTimeAndActivityCode(@Param("activityCode") String activityCode,
                                              @Param("startTimeStr") String startTimeStr, @Param("endTimeStr") String endTimeStr,
                                              @Param("mobile") String mobile, @Param("productCode") String productCode);
}
