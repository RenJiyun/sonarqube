package com.wlzq.activity.virtualfin.biz.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.virtualfin.biz.ActGoodsFlowBiz;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.dto.ActGoodsFlowDto;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.Page;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;

@Service
public class ActGoodsFlowBizImpl implements ActGoodsFlowBiz {
	@Autowired
	private ActGoodsFlowDao actGoodsFlowDao;
	
	@Override
	public Double balance(String activityCode, String mobile, String goodsCode) {
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return 0.0;
		}
		ActGoodsFlow flow = new ActGoodsFlow();
		flow.setActivityCode(activityCode);
		flow.setMobile(mobile);
		flow.setGoodsCode(goodsCode);
		return actGoodsFlowDao.getBalance(flow);
	}

	@Override
	public StatusObjDto<ActGoodsFlowDto> goodsFlow(String activityCode, String mobile, String goodsCode, Page page) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<>(true, new ActGoodsFlowDto(), StatusDto.SUCCESS, "");
		}
		ActGoodsFlow flow = new ActGoodsFlow();
		flow.setActivityCode(activityCode);
		flow.setMobile(mobile);
		flow.setGoodsCode(goodsCode);
		List<ActGoodsFlow> all = actGoodsFlowDao.findList(flow);
		Double totalIncome = all.stream().filter(e -> ActGoodsFlow.FLOW_FLAG_GET.equals(e.getFlag())).mapToDouble(ActGoodsFlow :: getGoodsQuantity).sum();
		Double totalExpand = all.stream().filter(e -> ActGoodsFlow.FLOW_FLAG_CONSUME.equals(e.getFlag())).mapToDouble(ActGoodsFlow :: getGoodsQuantity).sum();
		flow.setPage(page);
		List<ActGoodsFlow> list = actGoodsFlowDao.findList(flow);
		ActGoodsFlowDto dto = new ActGoodsFlowDto();
		dto.setInfo(list);
		dto.setTotalIncome(totalIncome);
		dto.setTotalExpand(totalExpand);
		return new StatusObjDto<>(true, dto, StatusDto.SUCCESS, "");
	}

	@Override
	public ActGoodsFlow consume(String activityCode, String productCode, String mobile, String userId, String openId, String customerId, String orderId, String goodsCode, Double quantity) {
		ActGoodsFlow flow = new ActGoodsFlow();
		flow.setActivityCode(activityCode);
		flow.setProductCode(productCode);
		flow.setMobile(mobile);
		flow.setGoodsCode(goodsCode);
		flow.setUserId(userId);
		flow.setOpenId(openId);
		flow.setCustomerId(customerId);
		flow.setFlag(ActGoodsFlow.FLOW_FLAG_CONSUME);
		flow.setGoodsQuantity(quantity);
		flow.setOrderId(orderId);
		flow.setCreateTime(new Date());
		actGoodsFlowDao.insert(flow);
		return flow;
	}

	@Override
	public int taskCount(String activityCode, String mobile) {
		if (ObjectUtils.isEmptyOrNull(activityCode) || ObjectUtils.isEmptyOrNull(mobile)) {
			return 0;
		}
		ActGoodsFlow flow = new ActGoodsFlow();
		flow.setActivityCode(activityCode);
		flow.setMobile(mobile);
		List<ActGoodsFlow> list = actGoodsFlowDao.findList(flow);
		return (int)list.stream().filter(e -> ObjectUtils.isEmptyOrNull(e.getTaskCode())).count();
	}

}
