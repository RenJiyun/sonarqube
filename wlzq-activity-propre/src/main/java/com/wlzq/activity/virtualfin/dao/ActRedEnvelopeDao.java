package com.wlzq.activity.virtualfin.dao;

import com.wlzq.activity.virtualfin.dto.LastAmountFlowResDto;
import com.wlzq.activity.virtualfin.model.ActRedEnvelope;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MybatisScan
public interface ActRedEnvelopeDao extends CrudDao<ActRedEnvelope> {
	
	Double getBalance(ActRedEnvelope entity);

	ActRedEnvelope getByOrderId(@Param("orderId") String OrderId);

	List<LastAmountFlowResDto> getLastAmountFlow(@Param("activityCode") String activityCode);
}
