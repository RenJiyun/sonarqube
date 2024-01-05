package com.wlzq.activity.virtualfin.biz.impl;

import com.google.common.collect.Lists;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.ActivityConstant;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.virtualfin.biz.ActFinOrderBiz;
import com.wlzq.activity.virtualfin.biz.ActGoodsFlowBiz;
import com.wlzq.activity.virtualfin.biz.ActRedEnvelopeBiz;
import com.wlzq.activity.virtualfin.dao.ActFinOrderDao;
import com.wlzq.activity.virtualfin.dao.ActFinProductDao;
import com.wlzq.activity.virtualfin.model.ActFinOrder;
import com.wlzq.activity.virtualfin.model.ActFinProduct;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ActFinOrderBizImpl extends ActivityBaseBiz implements ActFinOrderBiz {
	@Autowired
	private ActFinOrderDao finOrderDao;
	@Autowired
	private ActFinProductDao finProductDao;
	@Autowired
	private ActGoodsFlowBiz flowBiz;
	@Autowired
	private ActRedEnvelopeBiz redEnvelopeBiz;

	private final static DecimalFormat df = new DecimalFormat("#.####");



	@Override
	public StatusObjDto<List<ActFinOrder>> orders(String activityCode, String mobile) {
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<>(true, Lists.newArrayList(), StatusDto.SUCCESS, "");
		}
		ActFinOrder order = new ActFinOrder();
		order.setActivityCode(activityCode);
		order.setMobile(mobile);
		List<ActFinOrder> list = finOrderDao.findList(order);
		for (ActFinOrder actFinOrder : list) {
			Double incomeBalance = caculateIncome(actFinOrder);
			actFinOrder.setIncomeBalance(incomeBalance);
			Integer leftDays = DateUtils.daysBetween(new Date(), actFinOrder.getCloseDateEnd());
			Integer period = DateUtils.daysBetween(actFinOrder.getCloseDateStart(), actFinOrder.getCloseDateEnd()) + 1;
			leftDays = leftDays <= 0 ? 0 : leftDays;
			actFinOrder.setLeftDays(leftDays);
			actFinOrder.setPeriod(period);
		}
		return new StatusObjDto<List<ActFinOrder>>(true, list, StatusDto.SUCCESS, "");
	}

	@Override
	@Transactional
	public StatusObjDto<ActFinOrder> buy(String activityCode, String mobile, String productCode, Double price, String userId, String openId, String customerId) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<ActFinOrder>(true, new ActFinOrder(), StatusDto.SUCCESS, "");
		}
		if (ObjectUtils.isEmptyOrNull(price)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("price");
		}

		backList(mobile);

		StatusDto isValid = isValid(activityCode);
		if (!StatusDto.SUCCESS.equals(isValid.getCode())) {
			return new StatusObjDto<>(false, isValid.getCode(), isValid.getMsg());
		}

		Date now = new Date();
		//查询产品
		ActFinProduct product = getProduct(activityCode, productCode);
		if (price.compareTo(product.getMinBuy()) < 0) {
			throw ActivityBizException.ACT_ORDER_NOT_MIN_BUY;
		}
		if (product.getCloseDate().getTime() < now.getTime()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("超出产品购买时间。");
		}


		//每人每日购买上限为2万
		String today = DateUtils.formate(now, "yyyy-MM-dd");
		String startTimeStr = today + " 00:00:00";
		String endTimeStr = today + " 23:59:59";
		BigDecimal priceBD = new BigDecimal(price);
		BigDecimal expGoldOrderEveryOne = finOrderDao.sumAmountByTimeAndActivityCode(activityCode, startTimeStr, endTimeStr, mobile,null);
		if (ActivityConstant.EXP_GOLD_21818_ORDER_MAX_EVERY_ONE.compareTo(expGoldOrderEveryOne.add(priceBD)) < 0) {
			throw ActivityBizException.ACT_BEYOND_ORDER_MAX_EVERY_ONE.format(ActivityConstant.EXP_GOLD_21818_ORDER_MAX_EVERY_ONE);
		}

		//产品每天额度
		BigDecimal expGoldOrderEveryDay = finOrderDao.sumAmountByTimeAndActivityCode(activityCode, startTimeStr, endTimeStr, null,productCode);
		String max = AppConfigUtils.get(ActivityConstant.EXP_GOLD_21818_ORDER_MAX_EVERY_DAY_CONFIG);
		BigDecimal maxBD;
		if (StringUtils.isNotBlank(max)) {
			maxBD = new BigDecimal(max);
		}else {
			maxBD = ActivityConstant.EXP_GOLD_21818_ORDER_MAX_EVERY_DAY;
		}
		if (maxBD.compareTo(expGoldOrderEveryDay.add(priceBD)) < 0) {
			throw ActivityBizException.ACT_BEYOND_ORDER_MAX_EVERY_DAY;
		}

		//体验金余额
		Double goodsBalance = flowBiz.balance(activityCode, mobile, null);
		if (goodsBalance == null || goodsBalance.compareTo(price) < 0) {
			throw ActivityBizException.ACT_ORDER_NOT_ENOUGH_GOLG;
		}

		ActFinOrder order = new ActFinOrder();
		order.setUserId(userId);
		order.setOpenId(openId);
		order.setMobile(mobile);
		order.setCustomerId(customerId);
		order.setFlag(ActFinOrder.ORDER_FLAG_BUY);
		order.setProfit(product.getProfit());
		order.setProductCode(productCode);
		order.setActivityCode(activityCode);
		order.setGoodsCode(product.getGoodsCode());
		order.setPrice(price);
		String orderId = UUID.randomUUID().toString().replace("-", "");
		order.setOrderId(orderId);
		order.setStatus(ActFinOrder.ORDER_STATUS_UNCOMFIRMED);
		/**目前产品封闭期开始时间为购买的下一天**/

		Date nextDayStart = DateUtils.addDay(DateUtils.getDayStart(now), 1);
//		Date closeDateEnd = DateUtils.addDay(DateUtils.getDayEnd(nextDayStart), product.getPeriod() - 1);
		Date closeDateEnd = DateUtils.getDayEnd(DateUtils.addDay(nextDayStart, product.getPeriod() - 1));
		order.setCloseDateStart(nextDayStart);
		order.setCloseDateEnd(closeDateEnd);
		order.setCreateTime(now);
		order.setPeriod(product.getPeriod());
		order.setProductName(product.getName());
		order.setLeftDays(product.getPeriod());
		Double incomeBalance = caculateIncome(order);
		order.setIncomeBalance(incomeBalance);
		finOrderDao.insert(order);
		flowBiz.consume(activityCode, productCode, mobile, userId, openId, customerId, orderId, product.getGoodsCode(), 0 - price);
		return new StatusObjDto<ActFinOrder>(true, order, StatusDto.SUCCESS, "");
	}


	private ActFinProduct getProduct(String activityCode, String productCode) {
		ActFinProduct product = new ActFinProduct();
		product.setActivityCode(activityCode);
		product.setCode(productCode);
		product = finProductDao.findByCode(activityCode, productCode);
		if (product == null || CodeConstant.CODE_NO.equals(product.getStatus())) {
			throw ActivityBizException.ACT_PRODUCT_NOT_EXIST;
		}
		return product;
	}
	
	private Double caculateIncome(ActFinOrder order) {
		if (order == null) {
			return Double.valueOf(df.format(0.0));
		}
		Integer status = order.getStatus();
		//未确认也要做个预计收益
//		if (ActFinOrder.ORDER_STATUS_UNCOMFIRMED.equals(status)) {
//			return Double.valueOf(df.format(0.0));
//		}
		Date now = new Date();
		Integer days = DateUtils.daysBetween(order.getCloseDateStart(), now) + 1;
		Integer period = DateUtils.daysBetween(order.getCloseDateStart(), order.getCloseDateEnd()) + 1;
		/**计息天数**/
		days = days >= period ? period : days;
		Double income = order.getPrice() * order.getProfit() * period / 365 / 100;
		return Double.valueOf(df.format(income));
	}

	@Override
	@Transactional
	public StatusObjDto<List<ActFinOrder>> updStaus() {
		List<ActFinOrder> toConfirmList = orderConfirm();
		List<ActFinOrder> toRedeemList = orderRedeem();
		List<ActFinOrder> list = Lists.newArrayList();
		list.addAll(toConfirmList);
		list.addAll(toRedeemList);
		redEnvelopeBiz.addRedEnvelope(toRedeemList);
		return new StatusObjDto<List<ActFinOrder>>(true, list, StatusDto.SUCCESS, "");
	}

	private List<ActFinOrder> orderConfirm() {
		ActFinOrder entity = new ActFinOrder();
		entity.setNow(new Date());
		entity.setStatus(ActFinOrder.ORDER_STATUS_UNCOMFIRMED);
		List<ActFinOrder> list = finOrderDao.findList(entity);
		for (ActFinOrder actFinOrder : list) {
			actFinOrder.setStatus(ActFinOrder.ORDER_STATUS_COMFIRMED);
			actFinOrder.setUpdateTime(new Date());
			finOrderDao.update(actFinOrder);
		}
		return list;
	}
	
	private List<ActFinOrder> orderRedeem() {
		ActFinOrder entity = new ActFinOrder();
		entity.setStatus(ActFinOrder.ORDER_STATUS_COMFIRMED);
		entity.setNow(new Date());
		List<ActFinOrder> list = finOrderDao.findList(entity);
		for (ActFinOrder actFinOrder : list) {
			actFinOrder.setStatus(ActFinOrder.ORDER_STATUS_DUE);
			actFinOrder.setUpdateTime(new Date());
			finOrderDao.update(actFinOrder);
		}
		return list;
	}
	
}
