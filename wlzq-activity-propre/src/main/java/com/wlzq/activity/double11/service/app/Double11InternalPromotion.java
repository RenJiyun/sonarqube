
package com.wlzq.activity.double11.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.double11.biz.Double11InternalPromotionBiz;
import com.wlzq.activity.double11.dto.BranchRankingDto;
import com.wlzq.activity.double11.dto.MyRankingInfoDto;
import com.wlzq.activity.double11.dto.SaleRankingDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.Page;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Double11InternalPromotion服务类
 * @author zhujt
 * @version 1.0
 */
@Service("activity.double11internalpromotion")
public class Double11InternalPromotion extends BaseService {

    @Autowired
    private Double11InternalPromotionBiz double11InternalPromotionBiz;


    @Signature(true)
    @MustLogin(true)
	public ResultDto saleranking(RequestParams params,AccTokenUser user,Customer customer) {
		String staffNo = user.getStaffNo();
		StatusObjDto<List<SaleRankingDto>> result = double11InternalPromotionBiz.saleRanking(staffNo);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		Map<String,Object> data = Maps.newHashMap();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
		return new ResultDto(0,data,"");
	}

	@Signature(true)
	@MustLogin(true)
	public ResultDto branchranking(RequestParams params,AccTokenUser user,Customer customer) {
		String staffNo = user.getStaffNo();
		StatusObjDto<List<BranchRankingDto>> result = double11InternalPromotionBiz.branchRanking(staffNo);
		if(!result.isOk()) {
			return new ResultDto(result.getCode() ,result.getMsg());
		}
		Map<String,Object> data = Maps.newHashMap();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
		return new ResultDto(0,data,"");
	}

	@Signature(true)
	@MustLogin(true)
	public ResultDto myrankinginfo(RequestParams params,AccTokenUser user,Customer customer) {
		Page page = buildPageNew(params);
		StatusObjDto<MyRankingInfoDto> result = double11InternalPromotionBiz.myRankingInfo(user,page);
		if(!result.isOk()) {
			return new ResultDto(result.getCode() ,result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}

}
