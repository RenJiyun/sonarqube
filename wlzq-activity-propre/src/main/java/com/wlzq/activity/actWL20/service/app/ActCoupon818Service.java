package com.wlzq.activity.actWL20.service.app;

import com.wlzq.activity.actWL20.biz.ActCoupon818Biz;
import com.wlzq.activity.actWL20.dto.ActSubscribeDto;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 万联20周年活动-【理财模块-入金客户享8.18%理财】
 * 
 * @author jjw
 */
@Service("activity.actcoupon818")
public class ActCoupon818Service extends BaseService {

	@Autowired
	private ActCoupon818Biz actCoupon818Biz;

	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto recieve(RequestParams params, AccTokenUser user, Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String prizeType = (String) params.get("prizeType");
		String userId = user == null ? null : user.getUserId();
		String openId = user == null ? null : user.getOpenid();
		String recommendCode = (String) params.get("recommendCode");

		StatusObjDto<CouponRecieveStatusDto> result = actCoupon818Biz.recieve(activityCode, prizeType, userId, openId, customer, recommendCode);

		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		ResultDto back = new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");
		return back;
	}

	/**
	 * 记录入金客户act_fundin_go
	 */
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto addfundingo(RequestParams params, AccTokenUser user, Customer customer) {
		if (ObjectUtils.isEmptyOrNull(customer)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("customer");
		}

		int count = actCoupon818Biz.addFundinGo(customer);

		int returnCode = count > 0 ? 0 : 1;
		String msg = returnCode == 0 ? "" : "入金登记失败";
		ResultDto result = new ResultDto();
		result.setCode(returnCode);
		result.setMsg(msg);
		return result;
	}

	/**
	 * 活动订阅
	 * 
	 * @param params
	 * @param user
	 * @param customer
	 * @return
	 */
	@Signature(true)
	@MustLogin(true)
	public ResultDto subscribe(RequestParams params, AccTokenUser user, Customer customer) {
		String activityCode = params.getString("activityCode");
		if (ObjectUtils.isEmptyOrNull(user)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("user");
		}

		int count = actCoupon818Biz.subscribe(activityCode, user.getMobile(), customer == null ? "" : customer.getCustomerId());
		int returnCode = count > 0 ? 0 : 1;
		String msg = returnCode == 0 ? "" : "订阅失败";
		ResultDto result = new ResultDto();
		result.setCode(returnCode);
		result.setMsg(msg);
		return result;
	}

	/**
	 * 活动订阅查询
	 * 
	 * @param params
	 * @param user
	 * @param customer
	 */
	@Signature(true)
	@MustLogin(true)
	public ResultDto subscribecheck(RequestParams params, AccTokenUser user, Customer customer) {
		String activityCode = params.getString("activityCode");
		if (ObjectUtils.isEmptyOrNull(user)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("user");
		}
		
		StatusObjDto<ActSubscribeDto> result = actCoupon818Biz.subscribeCheck(activityCode, user.getMobile(), customer == null ? "" : customer.getCustomerId());
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		ResultDto back = new ResultDto(0, BeanUtils.beanToMap(result.getObj()), result.getMsg());
		return back;
	}
}
