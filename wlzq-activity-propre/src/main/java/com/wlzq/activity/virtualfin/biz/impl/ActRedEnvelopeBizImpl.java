/**
 * 
 */
package com.wlzq.activity.virtualfin.biz.impl;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.ActivityConstant;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.redenvelope.biz.RedEnvelopeBiz;
import com.wlzq.activity.redenvelope.dto.RedEnvelopeDto;
import com.wlzq.activity.redenvelope.model.RedEnvelope;
import com.wlzq.activity.virtualfin.biz.ActRedEnvelopeBiz;
import com.wlzq.activity.virtualfin.biz.ActTaskExpGoldBiz;
import com.wlzq.activity.virtualfin.dao.ActFinOrderDao;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.dao.ActRedEnvelopeDao;
import com.wlzq.activity.virtualfin.dto.ActRedEnvelopeDto;
import com.wlzq.activity.virtualfin.dto.LastAmountFlowResDto;
import com.wlzq.activity.virtualfin.model.ActFinOrder;
import com.wlzq.activity.virtualfin.model.ActRedEnvelope;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.Page;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 活动红包服务类
 * @author zhaozx
 * @version 2020-07-28
 */
@Service
public class ActRedEnvelopeBizImpl implements ActRedEnvelopeBiz {
	/** 2021818体验金活动编码 */
	public static final String ACT_CODE_2021818_EXPERIENCE = "ACTIVITY.2021818.EXPERIENCE";
	public static final String REDPACK_2021818_EXPERIENCE = "REDPACK.2021818.EXPERIENCE";


	@Autowired
	private ActRedEnvelopeDao redEnvelopeDao;
	@Autowired
	private ActTaskExpGoldBiz taskBiz;
	@Autowired
	private RedEnvelopeBiz redEnvelopeBiz;
	@Autowired
	private ActFinOrderDao actFinOrderDao;
	@Autowired
	private ActGoodsFlowDao actGoodsFlowDao;
	@Autowired
	private ActivityBaseBiz activityBaseBiz;

	
	private final static DecimalFormat df = new DecimalFormat("#.####");
	
	@Override
	public Double balance(String activityCode, String mobile) {
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return 0.0;
		}
		ActRedEnvelope flow = new ActRedEnvelope();
		flow.setActivityCode(activityCode);
		flow.setMobile(mobile);
		return redEnvelopeDao.getBalance(flow);
	}

	@Override
	public StatusObjDto<ActRedEnvelopeDto> redEnvelopeFlow(String activityCode, String mobile, Page page) {
		ActRedEnvelopeDto dto = new ActRedEnvelopeDto();
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<>(true, dto, StatusDto.SUCCESS, "");
		}
		ActRedEnvelope flow = new ActRedEnvelope();
		flow.setActivityCode(activityCode);
		flow.setMobile(mobile);
		List<ActRedEnvelope> list = redEnvelopeDao.findList(flow);
		Double totalIncome = list.stream().filter(e -> ActRedEnvelope.FLOW_FLAG_GET.equals(e.getFlag())).mapToDouble(ActRedEnvelope :: getQuantity).sum();
		Double totalExpand = list.stream().filter(e -> ActRedEnvelope.FLOW_FLAG_CONSUME.equals(e.getFlag())).mapToDouble(ActRedEnvelope :: getQuantity).sum();
		dto.setInfo(list);

		BigDecimal totalIncomeBigDecimal = new BigDecimal(totalIncome).setScale(2,BigDecimal.ROUND_HALF_UP);
		BigDecimal totalExpandBigDecimal = new BigDecimal(totalExpand).setScale(2,BigDecimal.ROUND_HALF_UP);
		dto.setTotalIncome(totalIncomeBigDecimal);
		dto.setTotalExpand(totalExpandBigDecimal);
		return new StatusObjDto<>(true, dto, StatusDto.SUCCESS, "");
	}


	@Override
	public List<LastAmountFlowResDto> getLastAmountFlow(String activityCode){
		List<LastAmountFlowResDto> lastAmountFlow = redEnvelopeDao.getLastAmountFlow(activityCode);
		if (CollectionUtils.isEmpty(lastAmountFlow)) {
//			lastAmountFlow = actFinOrderDao.getLastAmountFlow(activityCode);
			lastAmountFlow = actGoodsFlowDao.getLastAmountFlow(activityCode);
		}

		if (!CollectionUtils.isEmpty(lastAmountFlow)) {
			for (LastAmountFlowResDto dto : lastAmountFlow) {
				dto.setMobile(replaceMobile(dto.getMobile()));
			}
		}
		return lastAmountFlow;
	}

	public static String replaceMobile(String mobile){
		String re = null;
		if (StringUtils.isNotBlank(mobile) ) {
			if (mobile.length() == 11) {
				re = mobile.substring(0,3) + "****" + mobile.substring(7,11);
			}else if(mobile.length()>=4){
				re = "****" + mobile.substring(mobile.length()-4);
			}
		}
		return re;
	}


	@Override
	@Transactional
	public StatusObjDto<ActRedEnvelope> withdraw(String activityCode, String mobile, String userId, String openId, String customerId, Double quantity) {
		if (ACT_CODE_2021818_EXPERIENCE.equals(activityCode)) {
			//2021818活动
			ActRedEnvelope actRedEnvelope = withdrawRedPack(activityCode, mobile, userId, openId, customerId, quantity);
			return new StatusObjDto<>(true, actRedEnvelope, StatusDto.SUCCESS, "");
		}

		if (ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<>(true, new ActRedEnvelope(), StatusDto.SUCCESS, "");
		}
		if (ObjectUtils.isEmptyOrNull(quantity) || quantity.compareTo(0.0) <=0 ) {
			return new StatusObjDto<>(true, new ActRedEnvelope(), StatusDto.SUCCESS, "");
		}
		Double balance = balance(activityCode, mobile);
		/**金额小于2元不给提现**/
		if (balance.compareTo(2.0) < 0) {
			throw ActivityBizException.ACT_NOT_MIN_WITHDRAW;
		}
		if (balance.compareTo(quantity) < 0) {
			throw ActivityBizException.ACT_BEYONG_BALANCE;
		}
		/**检查是否有异常任务**/
		boolean tasksOk = taskBiz.checkTask(activityCode, mobile, null);
		if (!tasksOk) {
			throw ActivityBizException.ACT_ILL_TASK;
		}
		/**新增提现流水**/
		ActRedEnvelope flow = new ActRedEnvelope();
		flow.setFlag(ActRedEnvelope.FLOW_FLAG_CONSUME);
		flow.setBusinessCode("WITHDRAW");
		flow.setBusinessName("红包提现");
		flow.setQuantity(0 - quantity);
		flow.setMobile(mobile);
		flow.setUserId(userId);
		flow.setOpenId(openId);
		flow.setCustomerId(customerId);
		flow.setActivityCode(activityCode);
		flow.setStatus(0);		// 提现成功后处理
		flow.setCreateTime(new Date());
		flow.setOrderId(UUID.randomUUID().toString().replace("-", ""));
		
		RedEnvelope redEnvelope = new RedEnvelope();
		redEnvelope.setUserId(userId);
		redEnvelope.setOpenId(openId);
		redEnvelope.setBusinessNo(flow.getOrderId());
		redEnvelope.setBusinessCode(activityCode);
//		quantity = quantity * 100;
		BigDecimal amount = new BigDecimal(String.valueOf(quantity));
		amount = amount.multiply(new BigDecimal(100));
		redEnvelope.setAmount(amount.intValue());
		redEnvelope.setNotifyUrl(new String("Hello, red envelope"));
		StatusObjDto<RedEnvelopeDto> result = redEnvelopeBiz.create(redEnvelope);
		if (!result.isOk()) {
			throw BizException.NETWORK_ERROR;
		}
		
		String redEnvelopeUrl = result.getObj().getRecieveUrl();
		flow.setRedEnvelopeUrl(redEnvelopeUrl);
		redEnvelopeDao.insert(flow);
		return new StatusObjDto<ActRedEnvelope>(true, flow, StatusDto.SUCCESS, "");
	}

	/**
	 * 818活动
	 * 提现，获取红包兑换码
	 * （对接绩牛的获取红包兑换码接口）
	 */
	@Transactional(rollbackFor = Exception.class)
	public ActRedEnvelope withdrawRedPack(String activityCode, String mobile, String userId, String openId, String customerId, Double quantity) {
		//校验
		checkParam(activityCode, mobile, quantity);

		activityBaseBiz.backList(mobile);

		long currentTimeMillis = System.currentTimeMillis();
		BigDecimal quantityDecimal = new BigDecimal(String.valueOf(quantity)).setScale(2, BigDecimal.ROUND_HALF_UP);
		String flowOrderId = UUID.randomUUID().toString().replace("-", "");


		//region 调用提现接口
		LocalDateTime localDate = LocalDateTime.now();
		//提现操作时间控制
		Duration between = Duration.between(localDate, ActivityConstant.ACT_21818_RED_PACK_END_DATE);
		long expireDays = between.toDays();
		if (between.toMillis() < 0) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("不在提现时间范围。");
		}

		Map<String, Object> bizParams = Maps.newHashMap();
		bizParams.put("timestamp",currentTimeMillis);
		bizParams.put("amount",quantityDecimal.multiply(new BigDecimal("100")).intValue());
		bizParams.put("tradeId",flowOrderId);
		bizParams.put("template",REDPACK_2021818_EXPERIENCE);
		//兑换码有效期时间控制
		bizParams.put("expireTime","2021-09-10 23:59:59");
		bizParams.put("description","万联瓜分1亿体验金活动收益");
		ResultDto resultDto = RemoteUtils.call("base.couponcooperation.withdraw", ApiServiceTypeEnum.COOPERATION, bizParams, true);
		if(!resultDto.getCode().equals(ResultDto.SUCCESS)) {
			throw new ActivityBizException(resultDto.getCode(),resultDto.getMsg());
		}
		String redeemCode = null;
		if (resultDto.getCode() == 0) {
			redeemCode = (String)resultDto.getData().get("redeemCode");
		}
		//endregion


		//region 新增提现流水
		Date now = new Date(currentTimeMillis);
		ActRedEnvelope flow = new ActRedEnvelope().setFlag(ActRedEnvelope.FLOW_FLAG_CONSUME)
			.setBusinessCode("WITHDRAW").setBusinessName("红包提现")
			.setQuantity(0 - quantity).setMobile(mobile)
			.setUserId(userId).setOpenId(openId)
			.setCustomerId(customerId).setActivityCode(activityCode)
			.setStatus(0)// 提现成功后处理
			.setFlag(2)
			.setCreateTime(now).setOrderId(flowOrderId)
			.setRedEnvelopeUrl(redeemCode);
		redEnvelopeDao.insert(flow);
		//endregion

		ActRedEnvelope actRedEnvelope = new ActRedEnvelope().setRedeemCode(redeemCode);
		return actRedEnvelope;
	}

	private void checkParam(String activityCode, String mobile, Double quantity) {
		if (ObjectUtils.isEmptyOrNull(mobile)) {
			throw ActivityBizException.ACTIVITY_PARAMS_EXCEPTION.format("mobile");
		}
		if (ObjectUtils.isEmptyOrNull(quantity) || quantity.compareTo(0.0) <=0 ) {
			throw ActivityBizException.ACTIVITY_PARAMS_EXCEPTION.format("quantity");
		}
		Double balance = balance(activityCode, mobile);
		/**金额小于2元不给提现**/
		if (balance.compareTo(2.0) < 0) {
			throw ActivityBizException.ACT_NOT_MIN_WITHDRAW;
		}
		if (balance.compareTo(quantity) < 0) {
			throw ActivityBizException.ACT_BEYONG_BALANCE;
		}
	}


	
	@Override
	@Transactional
	public void addRedEnvelope(List<ActFinOrder> toRedeemList) {
		if (toRedeemList == null) {
			return;
		}
		for (ActFinOrder actFinOrder : toRedeemList) {
			ActRedEnvelope flow = BeanUtils.copyToNewBean(actFinOrder, ActRedEnvelope.class);
			flow.setFlag(ActRedEnvelope.FLOW_FLAG_GET);
			flow.setBusinessCode(actFinOrder.getProductCode());
			flow.setBusinessName(actFinOrder.getProductName() + "收益");
			Integer period = DateUtils.daysBetween(actFinOrder.getCloseDateStart(), actFinOrder.getCloseDateEnd()) + 1;
			Double incomeBalance = actFinOrder.getPrice() * actFinOrder.getProfit() / 100 * period / 365; 
			incomeBalance = Double.valueOf(df.format(incomeBalance));
			flow.setQuantity(incomeBalance);
			flow.setStatus(1);
			flow.setCreateTime(new Date());
			redEnvelopeDao.insert(flow);
		}
	}

}
