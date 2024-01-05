package com.wlzq.activity.virtualfin.biz.impl;

import com.wlzq.activity.redenvelope.dto.RedEnvelopeNotifyDto;
import com.wlzq.activity.redenvelope.observer.RedEnvelopeObserver;
import com.wlzq.activity.virtualfin.biz.ActGoodsFlowBiz;
import com.wlzq.activity.virtualfin.biz.ActRedEnvelopeBiz;
import com.wlzq.activity.virtualfin.biz.ActTaskExpGoldBiz;
import com.wlzq.activity.virtualfin.biz.ExpGoldBiz;
import com.wlzq.activity.virtualfin.dao.ActFirstLoginDao;
import com.wlzq.activity.virtualfin.dao.ActRedEnvelopeDao;
import com.wlzq.activity.virtualfin.dto.ExpGoldOverviewDto;
import com.wlzq.activity.virtualfin.model.ActFirstLogin;
import com.wlzq.activity.virtualfin.model.ActRedEnvelope;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.common.utils.StarReplaceUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ExpGoldBizImpl implements ExpGoldBiz, RedEnvelopeObserver {
	@Autowired
	private ActGoodsFlowBiz goodsFlowBiz;
	@Autowired
	private ActRedEnvelopeBiz redEnvelopeBiz;
	@Autowired
	private ActRedEnvelopeDao redEnvelopeDao;
	@Autowired 
	private ActTaskExpGoldBiz taskBiz;
	@Autowired
	private ActFirstLoginDao firstLoginDao;

	@Override
	public StatusObjDto<ExpGoldOverviewDto> overview(String activityCode, String mobile, String goodsCode, String taskCodes, String productCodes) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
		ExpGoldOverviewDto dto = new ExpGoldOverviewDto();
		Double expGold = goodsFlowBiz.balance(activityCode, mobile, goodsCode);
		Double redEnvelope = redEnvelopeBiz.balance(activityCode, mobile);
		List<ActTask> tasks = taskBiz.getTaskStatus(activityCode, mobile, taskCodes);
//		List<ActFinProduct> products = productBiz.findProducts(activityCode, productCodes);
		dto.setExpGold(expGold);
		dto.setRedEnvelope(redEnvelope);
		dto.setTasks(tasks);
		dto.setActivityCode(activityCode);
//		dto.setProducts(products);
		if (ObjectUtils.isNotEmptyOrNull(mobile)) {
			String mobileWithStar = StarReplaceUtils.replaceStarAction(mobile);
			dto.setMobile(mobileWithStar);
		}
		int doTaskCount = goodsFlowBiz.taskCount(activityCode, mobile);
		Integer firstLogin = doTaskCount > 0 ? CodeConstant.CODE_NO : CodeConstant.CODE_YES;
		dto.setIsFirstLogin(firstLogin);
		return new StatusObjDto<ExpGoldOverviewDto>(true, dto, StatusDto.SUCCESS, "");
	}

	@Override
	public void notify(RedEnvelopeNotifyDto notifyDto) {
		try {
			ActRedEnvelope redEnvelope = redEnvelopeDao.getByOrderId(notifyDto.getBusinessNo());
			if(redEnvelope != null) {
				redEnvelope.setStatus(CodeConstant.CODE_YES);
				redEnvelope.setUpdateTime(new Date());
				redEnvelopeDao.update(redEnvelope);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public StatusObjDto<ActFirstLogin> loginStatus(String activityCode, String mobile) {
		ActFirstLogin login = new ActFirstLogin();
		login.setMobile(mobile);
		login.setActivityCode(activityCode);
		List<ActFirstLogin> list = firstLoginDao.findList(login);
		Integer isFirstLogin = list.isEmpty() ? CodeConstant.CODE_YES : CodeConstant.CODE_NO;
		login.setIsFirstLogin(isFirstLogin);
		if (CodeConstant.CODE_YES.equals(isFirstLogin)) {
			login.setCreateTime(new Date());
			firstLoginDao.insert(login);
		} 
		return new StatusObjDto<>(true, login, StatusDto.SUCCESS, "");
	}
	
}
