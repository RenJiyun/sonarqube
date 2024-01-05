package com.wlzq.activity.virtualfin.biz;

import java.util.List;

import com.wlzq.activity.virtualfin.model.ActFinProduct;
import com.wlzq.core.dto.StatusObjDto;

public interface ActFinProductBiz {

	/**
	 * 获取在售产品列表
	 * @param activityCode
	 * @param productCodes
	 * @return
	 */
	List<ActFinProduct> findProducts(String activityCode, String productCodes);
	
	/**
	 * 产品列表
	 * @param activityCode
	 * @param productCodes
	 * @return
	 */
	StatusObjDto<List<ActFinProduct>> products(String activityCode, String productCodes);
}
