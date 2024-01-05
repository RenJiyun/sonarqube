package com.wlzq.activity.actWL20.biz.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.actWL20.biz.ActCouponFDBiz;
import com.wlzq.activity.actWL20.dao.ActCouponFDDao;
import com.wlzq.activity.actWL20.dto.ActFDRecieveDto;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

@Service
public class ActCouponFDBizImp extends ActivityBaseBiz implements ActCouponFDBiz {

	private Logger log = LoggerFactory.getLogger(ActCouponFDBizImp.class);

	@Autowired
	private ActPrizeDao actPrizeDao;

	private String PRIZE_A1 = "COUPON.INVEST.FD.A1.2021818"; // A股level-2一个月
	private String PRIZE_A3 = "COUPON.INVEST.FD.A3.2021818"; // A股level-2三个月
	private String PRIZE_14 = "COUPON.INVEST.FD.14.2021818"; // 投顾产品14天免单券
	private String PRIZE_88 = "COUPON.INVEST.FD.88.2021818"; // 投顾产品8.8折券
	private String PRIZE_78 = "COUPON.INVEST.FD.78.2021818"; // 投顾产品7.8折券
	private String PRIZE_68 = "COUPON.INVEST.FD.68.2021818"; // 投顾产品6.8折券
	private HashMap<Integer, ArrayList<String>> PRIZE_COUPON_TEMPLATES = new HashMap<Integer, ArrayList<String>>() {
		{
			put(1, Lists.newArrayList(new String[] { PRIZE_A1, PRIZE_14, PRIZE_88 })); // 第一等级福袋
			put(2, Lists.newArrayList(new String[] { PRIZE_A1, PRIZE_14, PRIZE_78 })); // 第二等级福袋
			put(3, Lists.newArrayList(new String[] { PRIZE_A3, PRIZE_14, PRIZE_68 })); // 第三等级福袋
		}
	};

	private static String PRIZE_LR = "COUPON.INVEST.FD.R5.2021818"; // 融券限时费率5%

	private HashMap<String, String> PRIZE_COUPON_VALUE = new HashMap<String, String>() {
		{
			put(PRIZE_A1, "一个月");
			put(PRIZE_A3, "三个月");
			put(PRIZE_14, "天免单券");
			put(PRIZE_88, "8.8折券");
			put(PRIZE_78, "7.8折券");
			put(PRIZE_68, "6.8折券");
			put(PRIZE_LR, "5%");
		}
	};

	@Autowired
	private CouponCommonReceiveBiz couponRecieveBiz;

	@Autowired
	private ActCouponFDDao actCouponFDDao;

	@Override
	public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, String userId, String openId, Customer customer, String recommendCode) {

		List<ActPrize> list = actCouponFDDao.findRecieves(activityCode, customer.getCustomerId());
		if (list.size() > 0) {
			throw ActivityBizException.ACT_FD_RECIEVE_OK;
		}

		// 获取用户等级
		int cusLevel = getCusLevel(customer);

		List<String> prizeTypeList = PRIZE_COUPON_TEMPLATES.get(cusLevel);

		StatusObjDto<CouponRecieveStatusDto> result = null;
		for (String prizeType : prizeTypeList) {
			result = couponRecieveBiz.receive(activityCode, prizeType, userId, openId, customer.getCustomerId(), recommendCode, null);
			if (!result.isOk()) {
				return new StatusObjDto<CouponRecieveStatusDto>(false, result.getCode(), result.getMsg());
			}
		}

		// 判断是否两融
		if (isLRCus(customer)) {
			result = couponRecieveBiz.receive(activityCode, PRIZE_LR, userId, openId, customer.getCustomerId(), recommendCode, null);
			if (!result.isOk()) {
				return new StatusObjDto<CouponRecieveStatusDto>(false, result.getCode(), result.getMsg());
			}
		}

		return result;
	}

	@Override
	public StatusObjDto<List<ActFDRecieveDto>> findRecieves(String activityCode, String customerId) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("customerId");
		}

		List<ActPrize> list = actCouponFDDao.findRecieves(activityCode, customerId);

		// region 将福袋奖品初始化到缓存中
		if (list == null || list.size() == 0) {
			for (Entry<Integer, ArrayList<String>> entry : PRIZE_COUPON_TEMPLATES.entrySet()) {
				List<String> prizeTypeList = entry.getValue();
				for (String prizeTypeCode : prizeTypeList) {
					List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, prizeTypeCode);
					initPrizes(activityCode, prizeTypeCode, prizeList);
				}
			}
			List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, PRIZE_LR);
			initPrizes(activityCode, PRIZE_LR, prizeList);
		}
		// end region

		List<ActFDRecieveDto> dtoList = new ArrayList<>();
		for (ActPrize act : list) {
			ActFDRecieveDto dto = new ActFDRecieveDto();
			dto.setCode(act.getCode());
			dto.setName(act.getName());
			dto.setValue(AppConfigUtils.get(act.getCode(), PRIZE_COUPON_VALUE.get(act.getCode())));
			dtoList.add(dto);
		}

		return new StatusObjDto<List<ActFDRecieveDto>>(true, dtoList, StatusDto.SUCCESS, "");
	}

	public int getCusLevel(Customer customer) {

		Double property = getCustomerProperty(customer.getCustomerId());

		property = property == null ? 0 : property;

		int level = 1;
		if (property.doubleValue() <= 100000) {
			level = 1;
		}
		if (property.doubleValue() > 100000 && property.doubleValue() <= 500000) {
			level = 2;
		}
		if (property.doubleValue() > 500000) {
			level = 3;
		}

		return level;
	}

	private Double getCustomerProperty(String customerId) {
		Map<String, Object> busparams = Maps.newHashMap();
		String serviceId = new String("ext.crm.dspt.khrjzc");
		Integer isNeedLogin = new Integer(1);
		JSONObject params = new JSONObject();
		params.put("I_KHH", customerId);
		busparams.put("serviceId", serviceId);
		busparams.put("isNeedLogin", isNeedLogin);
		busparams.put("params", params.toJSONString());
		log.info("fsdp call to get customer property");
		ResultDto result = RemoteUtils.call("base.fsdp.callservice", ApiServiceTypeEnum.APP, busparams, false);
		log.info("return:" + result.getData());
		if (!ResultDto.SUCCESS.equals(result.getCode()) || result.getData() == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData().get("O_RESULT");

		Double property = null;
		if (list == null || list.isEmpty()) {
			return property;
		}
		try {
			Map<String, Object> map = list.get(0);
			property = Double.valueOf(String.valueOf(map.get("RJJZC")));
			log.info("property:" + property);
		} catch (Exception e) {
			log.error("返回数据非Double：", e.getMessage());
			return null;
		}

		return property;
	}

	public boolean isLRCus(Customer customer) {
		//region 检查是否两融潜在客户
		Map<String, Object> busparams = Maps.newHashMap();
		String serviceId = new String("ext.sjzx.qzlrkh");
		Map<String, Object> params = Maps.newHashMap();
		params.put("I_KHH", customer.getCustomerId());
		busparams.put("serviceId", serviceId);
		busparams.put("isNeedLogin", 1);
		busparams.put("params", JsonUtils.map2Json(params));
		ResultDto resultDto = RemoteUtils.call("base.fsdpcoopration.callservice", ApiServiceTypeEnum.COOPERATION, busparams, false);
		if (!ResultDto.SUCCESS.equals(resultDto.getCode()) || resultDto.getData() == null || resultDto.getData().isEmpty()) {
			return false;
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>)resultDto.getData().get("O_RESULT");
		if (list.size() > 0) {
			log.error("RQ-+++++"+JsonUtils.object2JSON(resultDto.getData().get("O_RESULT")));
			return true;
		}
		//end region
		
		//region 检查是否开通了两融账户
		Map<String, Object> busparams2 = new HashMap<String, Object>();
		busparams2.put("funcNo", "331155");
		busparams2.put("op_branch_no", customer.getBranchNo());
		busparams2.put("op_entrust_way", "7");
		busparams2.put("branch_no", customer.getBranchNo());
		busparams2.put("client_id", customer.getCustomerId());
		busparams2.put("user_token", customer.getUserToken());
		ResultDto result = RemoteUtils.t2call(busparams2);
		if (!result.getCode().equals(StatusDto.SUCCESS)) {
			log.error("异常，" + JsonUtils.object2JSON(result));
			return false;
		}
		List<Map<String, Object>> stockAccounts = (List<Map<String, Object>>) result.getData().get("0");
		if(stockAccounts == null || stockAccounts.size() == 0) {
			return false;
		}
		
		for(Map<String, Object> map : stockAccounts) {
			if("7".equals(map.get("asset_prop"))) {
				return true;
			}
		}
		//end region
		
		return false;
	}

	/**
	 * 抽球模型，配置大转盘奖品
	 * 
	 * @param turntableeParam
	 */
	private void initPrizes(String activityCode, String prizeTypeCode, List<Long> prizes) {

		String redisKey = activityCode + ":" + prizeTypeCode;
		boolean hasInit = ActivityRedis.ACT_ACTVITY_PRIZE.exists(redisKey);
		if (hasInit)
			return;
		log.info("初始化大转盘奖品.............................");

		/** 中奖奖品初始化 **/
		if (prizes == null) {
			log.info("活动奖品未配置.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动奖品未配置");
		}
		if (prizes.size() == 0) {
			log.info("无可用的活动奖品.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("无可用奖品");
		}

		/** 奖品 **/
		int total = 0;
		for (int i = 0; i < prizes.size(); i++) {
			ActivityRedis.ACT_ACTVITY_PRIZE.sadd(redisKey, prizes.get(i));
			total++;
		}
		log.info("初始化大转盘奖品完毕.............................,total" + total + ",prize:" + prizes.size());
	}

}
