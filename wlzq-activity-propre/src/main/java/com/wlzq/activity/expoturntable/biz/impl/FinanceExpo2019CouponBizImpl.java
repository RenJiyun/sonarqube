package com.wlzq.activity.expoturntable.biz.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.expoturntable.biz.FinanceExpo2019CouponBiz;
import com.wlzq.activity.redeem.dao.RedeemDao;
import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.common.base.CouponBiz;

@Service
public class FinanceExpo2019CouponBizImpl extends ActivityBaseBiz implements FinanceExpo2019CouponBiz {
	
	private static final String ACT_COUPON = "ACTIVITY.FINANCEEXPO2019COUPON";
	
	private static final String PRIZE_LEVEL2 = "PRIZE.FINANCEEXPO2019.LEVEL2-12";
	private static final String PRIZE_INV = "PRIZE.FINANCEEXPO2019.INVESTMENT-80";
	private static final String PRIZE_FIN = "PRIZE.FINANCEEXPO2019.FIN";
	
	private static final String COUPON_INV = "COUPON.FINANCEEXPO2019.INV-80";
	private static final String COUPON_FIN = "COUPON.FINANCEEXPO2019.FIN";
	
	private static final Integer ISUSED_YES = 1;
	private static final Integer ISUSED_NO = 2;
	private static final Integer ISUSED_EXPIRY = 3;
	
	@Autowired
	private ActPrizeDao actPrizeDao;
	@Autowired
	private RedeemDao redeemDao;
	
	@Autowired
	private CouponBiz couponBiz;
	@Autowired
	private ActPrizeBiz actPrizeBiz;

	@Override
	public StatusObjDto<Map<String, Object>> myCoupon(AccTokenUser user) {
//		StatusDto isValid = super.isValid(ACT_COUPON);
//		if (!isValid.isOk()) {
//			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
//		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("level2Coupon", null);
		resultMap.put("investmentCoupon", null);
		resultMap.put("financialCoupon", null);
		long now = System.currentTimeMillis();
		
		String userId = user.getUserId();
		ActPrize level2 = this.findCoupon(userId, PRIZE_LEVEL2, null);
		if (level2 != null) {
			Map<String, Object> level2Map = new HashMap<String, Object>();
			level2Map.put("isUsed", ISUSED_NO);
			level2Map.put("name", "Level-2增强行情");
			level2Map.put("expiryDate", "2019-06-25");
			level2Map.put("rule", null);
			level2Map.put("url", null);
			level2Map.put("code", level2.getRedeemCode());
			
			Redeem redeem = redeemDao.findByCode(level2.getRedeemCode());
			if (redeem != null) {
				if (redeem.getValidityDateTo() != null && redeem.getValidityDateTo().getTime() < now) {
					level2Map.put("isUsed", ISUSED_EXPIRY);
				}
				if (redeem.getStatus().intValue() == 2) {
					level2Map.put("isUsed", ISUSED_YES);
				}
			}
			
			resultMap.put("level2Coupon", level2Map);
		}
		
		ActPrize inv = this.findCoupon(userId, PRIZE_INV, null);
		if (inv != null) {
			Map<String, Object> invMap = new HashMap<String, Object>();
			invMap.put("isUsed", ISUSED_NO);
			invMap.put("name", "首席宏观组合策略实验室一号");
			invMap.put("expiryDate", "2019-06-25");
			invMap.put("rule", null);
			invMap.put("url", null);
			invMap.put("code", inv.getRedeemCode());
			
			StatusObjDto<CouponInfo> dto = couponBiz.couponInfo(COUPON_INV, null);
			if (dto != null && dto.getObj() != null) {
				CouponInfo info = dto.getObj();
				invMap.put("url", info.getOpenUrl());
				invMap.put("rule", info.getRegulation());
				if (info.getValidityDateTo() != null) {
					invMap.put("expiryDate", DateUtils.formate(info.getValidityDateTo(), "yyyy-MM-dd"));
					if (info.getValidityDateTo().getTime() < now) {
						invMap.put("isUsed", ISUSED_EXPIRY);
					}
				}
			}
			
			if (inv.getStatus().intValue() == 3) {
				invMap.put("isUsed", ISUSED_YES);
			}
			resultMap.put("investmentCoupon", invMap);
		}
		
		ActPrize fin = this.findCoupon(userId, PRIZE_FIN, null);
		if (fin != null) {
			Map<String, Object> finMap = new HashMap<String, Object>();
			finMap.put("isUsed", ISUSED_NO);
			finMap.put("name", "金交会理财产品");
			finMap.put("expiryDate", "2019-06-25");
			finMap.put("rule", null);
			finMap.put("url", null);
			finMap.put("code", fin.getRedeemCode());
			
			StatusObjDto<CouponInfo> dto = couponBiz.couponInfo(COUPON_FIN, null);
			if (dto != null && dto.getObj() != null) {
				CouponInfo info = dto.getObj();
				finMap.put("url", info.getOpenUrl());
				finMap.put("rule", info.getRegulation());
				if (info.getValidityDateTo() != null) {
					finMap.put("expiryDate", DateUtils.formate(info.getValidityDateTo(), "yyyy-MM-dd"));
					if (info.getValidityDateTo().getTime() < now) {
						finMap.put("isUsed", ISUSED_EXPIRY);
					}
				}
			}
			
			if (fin.getStatus().intValue() == 3) {
				finMap.put("isUsed", ISUSED_YES);
			}
			resultMap.put("financialCoupon", finMap);
		}
		
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}

	private ActPrize findCoupon(String userId, String prizeCode, Integer status) {
		ActPrize qry = new ActPrize();
		qry.setUserId(userId);
		qry.setCode(prizeCode);
		qry.setStatus(status);
		qry.setActivityCode(ACT_COUPON);
		List<ActPrize> list = actPrizeDao.findList(qry);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public StatusObjDto<Map<String, Object>> getCoupon(AccTokenUser user, Customer customer) {
//		couponBiz.receiveAvailableCoupon(template, customerId, userId)
		StatusDto isValid = super.isValid(ACT_COUPON);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("resultCode", 0);
		resultMap.put("resultMsg", "");
		
		String userId = user.getUserId();
		String customerId = customer==null?null:customer.getCustomerId();
		List<ActPrize> prizes = this.getPrizeList(userId);
		
		if (!(prizes != null && prizes.size() > 0)) {
			resultMap.put("resultCode", ActivityBizException.ACTIVITY_DATABASE_DATANOTFOUND.getCode());
			resultMap.put("resultMsg", "没有可用优惠券");
			return new StatusObjDto<Map<String, Object>>(false, resultMap, ActivityBizException.ACTIVITY_DATABASE_DATANOTFOUND.getCode(), "没有可用优惠券");
		}
		
		for (ActPrize actPrize : prizes) {
			if (ObjectUtils.isEmptyOrNull(customerId)) {//更新奖品状态为相应用户占用
				if (PRIZE_LEVEL2.equals(actPrize.getCode())) {
					actPrizeBiz.updatePrize(userId, null, null, actPrize.getId(), ActPrize.STATUS_SEND, null, null);
				} else {
					actPrizeBiz.updatePrize(userId, null, null, actPrize.getId(), ActPrize.STATUS_OCCUPY, null, null);
				}
			} else {
				if (PRIZE_LEVEL2.equals(actPrize.getCode())) {
					actPrizeBiz.updatePrize(userId, user.getOpenid(), customerId, actPrize.getId(), ActPrize.STATUS_SEND, null, null);
				} else {
					StatusObjDto<CouponInfo> recieveStatus = couponBiz.receiveCoupon(customerId, userId, actPrize.getRedeemCode());
					if(!recieveStatus.getCode().equals(StatusDto.SUCCESS)) {//更新奖品为占用状态,下次客户登录进入自动领取
						actPrizeBiz.updatePrize(userId, user.getOpenid(), customerId, actPrize.getId(), ActPrize.STATUS_OCCUPY, null, null);
					} else {
						actPrizeBiz.updatePrize(userId, user.getOpenid(), customerId, actPrize.getId(), ActPrize.STATUS_SEND, null, null);
					}
				}
			}
		}
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	
	private List<ActPrize> getPrizeList(String userId) {
		List<ActPrize> prizes = new ArrayList<ActPrize>();
		
		List<ActPrize> level2Prizes = actPrizeBiz.findPrize(ACT_COUPON, null, userId, null, PRIZE_LEVEL2, null, null);
		if (!(level2Prizes != null && level2Prizes.size() > 0)) {
			ActPrize prize = actPrizeBiz.findOneAvailablePrize(ACT_COUPON, PRIZE_LEVEL2);
    		prizes.add(prize);
		}
		
		List<ActPrize> investPrizes = actPrizeBiz.findPrize(ACT_COUPON, null, userId, null, PRIZE_INV, null, null);
		if (!(investPrizes != null && investPrizes.size() > 0)) {
			ActPrize prize = actPrizeBiz.findOneAvailablePrize(ACT_COUPON, PRIZE_INV);
    		prizes.add(prize);
		}
		
		List<ActPrize> finPrizes = actPrizeBiz.findPrize(ACT_COUPON, null, userId, null, PRIZE_FIN, null, null);
		if (!(finPrizes != null && finPrizes.size() > 0)) {
			ActPrize prize = actPrizeBiz.findOneAvailablePrize(ACT_COUPON, PRIZE_FIN);
    		prizes.add(prize);
		}
		return prizes;
	}

	@Override
	public StatusObjDto<Map<String, Object>> useCoupon(AccTokenUser user, Customer customer) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("resultCode", 0);					
		resultMap.put("resultMsg", "");
		
		String userId = user.getUserId();
		String customerId = customer.getCustomerId();
		List<ActPrize> couponPrizes = actPrizeBiz.findPrize(ACT_COUPON, null, userId, null, "", null, null);
		List<ActPrize> hasReceviePrizes = Lists.newArrayList();
		List<ActPrize> notReceviePrizes = Lists.newArrayList();
		for (ActPrize prize : couponPrizes) {
			Integer status = prize.getStatus();
			if (status.equals(ActPrize.STATUS_OCCUPY)) {
				notReceviePrizes.add(prize);
			} else {
				hasReceviePrizes.add(prize);
			}	
		}
		
		int total = hasReceviePrizes.size() + notReceviePrizes.size();
		if (total > 3) {
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("奖品总数异常，大于3");
		}
		
		if (notReceviePrizes.size() == 0) { //若无奖品可领取
			resultMap.put("resultCode", 0);					
			resultMap.put("resultMsg", "无奖品可领取");
			return new StatusObjDto<Map<String, Object>>(true, resultMap, StatusDto.SUCCESS, "无奖品可领取");
		}
		
		for (ActPrize prize : notReceviePrizes) {
			Integer type = prize.getType();
			if (type.equals(ActPrize.TYPE_COUPON)) {
				List<ActPrize> list = actPrizeBiz.findPrize(ACT_COUPON, customerId, null, null, prize.getCode(), null, null);
				if (list != null && list.size() > 0) {
					continue;
				}
				StatusObjDto<CouponInfo> recieveStatus = couponBiz.receiveCoupon(customerId, userId, prize.getRedeemCode());
	    		if (!recieveStatus.getCode().equals(StatusDto.SUCCESS)) {
	    			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format(recieveStatus.getMsg());
	    		}
			}
			actPrizeBiz.updatePrize(userId, user.getOpenid(), customerId, prize.getId(), ActPrize.STATUS_SEND, null, null);
		}
		
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	
}
