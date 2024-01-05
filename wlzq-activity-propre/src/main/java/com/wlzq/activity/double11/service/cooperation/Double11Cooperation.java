
package com.wlzq.activity.double11.service.cooperation;

import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Double11Service服务类
 * @author 
 * @version 1.0
 */
@Service("activity.double11cooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION})
public class Double11Cooperation {
	
    @Autowired
    private Double11Biz double11Biz;

	@Signature(false)
	public ResultDto updatepersonalawardranking(RequestParams params,AccTokenUser user,Customer customer) {
    	StatusDto result = double11Biz.updatePersonalAwardRanking();
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	return new ResultDto(0,"");
	}

	@Signature(false)
	public ResultDto updatethsadviserteamawardranking(RequestParams params,AccTokenUser user,Customer customer) {
		StatusDto result = double11Biz.updateThsAdviserTeamAwardRanking();
		if(!result.isOk()) {
			return new ResultDto(result.getCode() ,result.getMsg());
		}
		return new ResultDto(0,"");
	}

	@Signature(false)
	public ResultDto updatedecisionsaleawardranking(RequestParams params,AccTokenUser user,Customer customer) {
		StatusDto result = double11Biz.updateDecisionSaleAwardRanking();
		if(!result.isOk()) {
			return new ResultDto(result.getCode() ,result.getMsg());
		}
		return new ResultDto(0,"");
	}

}
