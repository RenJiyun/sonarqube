
package com.wlzq.activity.base.service.cooperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActCacheBiz;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dto.AcReceivePriceVO;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.dto.PrizeReceiveSimpDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.utils.RemoteUtils;

/**
 * CommonService服务类
 * @author
 * @version 1.0
 */
@Service("activity.couponcooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION,ApiServiceTypeEnum.APP})
public class CommonCooperation extends BaseService {

	private Logger logger = LoggerFactory.getLogger(CommonCooperation.class);

    @Autowired
    private ActPrizeBiz actPrizeBiz;

    @Autowired
    private CouponCommonReceiveBiz couponRecieveBiz;

	@Autowired
	private ActCacheBiz actCacheBiz;

	@Signature(true)
	public ResultDto giveoutprize(RequestParams params) {
		String activityCode = params.getString("activityCode");
		String prizeTypeCode = params.getString("prizeTypeCode");
		ActPrize prize = actPrizeBiz.getOneAvailablePrize(activityCode, prizeTypeCode);
		if (prize == null) {
			return new ResultDto(ActivityBizException.ACT_EMPTY_PRIZE.getCode(), ActivityBizException.ACT_EMPTY_PRIZE.getMsg());
		}
		//更新奖品状态
		String userId = params.getString("userId");
		String openId = params.getString("openId");
		String customerId = params.getString("customerId");
		actPrizeBiz.updatePrize(userId, openId, customerId, prize.getId(), ActPrize.STATUS_SEND, null, null);
		//更新优惠券状态
		String redeemCode = prize.getRedeemCode();
		Map<String, Object> busparams = Maps.newHashMap();
		busparams.put("userId", userId);
		busparams.put("customerId", customerId);
		busparams.put("code", redeemCode);
		ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
		//调用失败
		if (result.getCode() != 0) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(prize), "");
	}

	@Signature(true)
	public ResultDto findavailableprizes(RequestParams params) {
		String activityCode = params.getString("activityCode");
		String prizeTypeCode = params.getString("prizeTypeCode");
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(activityCode, prizeTypeCode);
		Map<String, Object> data = Maps.newHashMap();
		data.put("count", prizes.size());
		return new ResultDto(0, data, "");
	}

	/**
	 * 领取奖品
	 * @param params
	 * @return
	 */
    @Signature(false)
	public ResultDto receive(RequestParams params) {
		String userId = params.getString("userId");
		String customerId = params.getString("customerId");//非必填
		String activityCode = params.getString("activityCode");
		String prizeType = params.getString("prizeType");
		Integer prizeAmount = params.getInt("prizeAmount");//奖品金额，单位分。非必填
		String openId = params.getString("openId");;
		String recommendCode = params.getString("recommendCode");
		Integer needUserId = params.getInt("needUserId");
		String mobile = params.getString("mobile");
		Integer isMobileDimension = params.getInt("isMobileDimension");

		
		AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO().setUserId(userId)
				.setCustomerId(customerId).setActivityCode(activityCode)
				.setPrizeType(prizeType).setOpenId(openId).setPrizeAmount(prizeAmount)
				.setRecommendCode(recommendCode).setMobile(mobile).setNeedUserId(needUserId);
		if (CodeConstant.CODE_YES.equals(isMobileDimension)) {
			acReceivePriceVO.setCustomerDimension(false);
			acReceivePriceVO.setMobileDimension(true);
		}

		//领取奖品例如 ： level-2兑换码、京东卡、优惠券。现金红包兑换码（绩牛）
		// 领取人：手机号、客户号（优惠券需要）
		StatusObjDto<List<PrizeReceiveSimpDto>> result = couponRecieveBiz.receivePriceCoop(acReceivePriceVO);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
		JSONArray jsonObject = (JSONArray) JSON.toJSON(result.getObj());
		Map<String,Object> data = new HashMap<>();
		data.put("list",jsonObject);
		return new ResultDto(0,data,"");
	}

	/**
	 * 活动平台删除缓存协作服务，支持活动平台任意缓存的删除
	 */
	@Signature(true)
	public ResultDto deletecache(RequestParams params) {
		/*缓存的前缀  #入参不能为空*/
		String prefix = params.getString("prefix");
		/*缓存的key  #入参可以为空*/
		String key = params.getString("key");

		StatusDto result = actCacheBiz.deleteCache(prefix, key);

		return new ResultDto(result.getCode());
	}

	@Signature(true)
	public ResultDto findprefix(RequestParams params) {

		StatusObjDto<List<String>> result = actCacheBiz.findPrefix();

		Map<String,Object> data = new HashMap<>();
		data.put("info", result.getObj());

		return new ResultDto(0, data, "");
	}
	
	/**
	 * 领取奖品
	 * @param params
	 * @return
	*/
	@Signature(false)
	public ResultDto status(RequestParams params) {
		String userId = params.getString("userId");
		String customerId = params.getString("customerId");//非必填
		String activityCode = params.getString("activityCode");
		String prizeTypes = params.getString("prizeTypes");
		String recommendCode = params.getString("recommendCode");
		Integer isMobileDimension = params.getInt("isMobileDimension");
		String mobile = params.getString("mobile");
		Integer needUserId = params.getInt("needUserId");
		AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO().setUserId(userId)
			.setCustomerId(customerId).setActivityCode(activityCode)
			.setPrizeType(prizeTypes)
			.setRecommendCode(recommendCode).setMobile(mobile).setNeedUserId(needUserId);
		if (CodeConstant.CODE_YES.equals(isMobileDimension)) {
			acReceivePriceVO.setCustomerDimension(false);
			acReceivePriceVO.setMobileDimension(true);
		}
	
	
		StatusObjDto<List<CouponRecieveStatusDto>> result = couponRecieveBiz.status(acReceivePriceVO);
		if(!result.isOk()) {
			return new ResultDto(result.getCode() ,result.getMsg());
		}
		Map<String,Object> data = Maps.newHashMap();
		data.put("total",result.getObj().size());
		data.put("info", result.getObj());
		ResultDto back = new ResultDto(0,data,"");
		return back;
	}
}
