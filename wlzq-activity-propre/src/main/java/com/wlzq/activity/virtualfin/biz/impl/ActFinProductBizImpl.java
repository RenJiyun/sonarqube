package com.wlzq.activity.virtualfin.biz.impl;

import com.google.common.collect.Lists;
import com.wlzq.activity.virtualfin.biz.ActFinProductBiz;
import com.wlzq.activity.virtualfin.dao.ActAgreementRefDao;
import com.wlzq.activity.virtualfin.dao.ActFinOrderDao;
import com.wlzq.activity.virtualfin.dao.ActFinProductDao;
import com.wlzq.activity.virtualfin.model.ActAgreementRef;
import com.wlzq.activity.virtualfin.model.ActFinProduct;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class ActFinProductBizImpl implements ActFinProductBiz {
	@Autowired
	private ActFinProductDao actFinProductDao;
	@Autowired
	private ActAgreementRefDao actAgreementRefDao;
	@Autowired
	private ActFinOrderDao finOrderDao;
	
	@Override
	public List<ActFinProduct> findProducts(String activityCode, String productCodes) {
		ActFinProduct actFinProduct = new ActFinProduct();
		actFinProduct.setStatus(CodeConstant.CODE_YES);
		actFinProduct.setActivityCode(activityCode);
		if (ObjectUtils.isNotEmptyOrNull(productCodes)) {
			List<String> codeList = Lists.newArrayList(productCodes.split(","));
			actFinProduct.setCodeList(codeList);
		}
		List<ActFinProduct> list = actFinProductDao.findList(actFinProduct);

		String today = DateUtils.formate(new Date(), "yyyy-MM-dd");
		String startTimeStr = today + " 00:00:00";
		String endTimeStr = today + " 23:59:59";
		for (ActFinProduct product : list) {
			ActAgreementRef ref = new ActAgreementRef();
			ref.setActivityCode(product.getActivityCode());
			ref.setProductCode(product.getCode());
			List<ActAgreementRef> refs = actAgreementRefDao.findList(ref);
			product.setAgreements(refs);

			BigDecimal expGoldOrderEveryDay = finOrderDao.sumAmountByTimeAndActivityCode(activityCode, startTimeStr, endTimeStr, null,product.getCode());
			product.setTotalSold(expGoldOrderEveryDay);
		}
		return list;
	}
	
	@Override
	public StatusObjDto<List<ActFinProduct>> products(String activityCode, String productCodes) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
		List<ActFinProduct> list = findProducts(activityCode, productCodes);
		return new StatusObjDto<List<ActFinProduct>>(true, list, StatusDto.SUCCESS, "");
	}

}
