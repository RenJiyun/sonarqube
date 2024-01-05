package com.wlzq.activity.festival38.biz.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.actWL20.dto.MyPrizeDto2;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.festival38.biz.Festival38Biz;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccAccountOpeninfo;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.remote.service.common.base.FsdpBiz;
import com.wlzq.remote.service.utils.RemoteUtils;

@Service
public class Festival38BizImp extends ActivityBaseBiz implements Festival38Biz {

	private Logger logger = LoggerFactory.getLogger(Festival38BizImp.class);

	private static List<String> ACT_GRAND_CODES = Lists.newArrayList(
			new String[] {
					"ACTIVITY.GRAND1.20220308",
					"ACTIVITY.GRAND2.20220308",
					"ACTIVITY.GRAND3.20220308",
					"ACTIVITY.GRAND4.20220308",
					"ACTIVITY.GRAND5.20220308"});

	private static Map<String,Integer> GRAND_PRIZE = new HashMap<String, Integer>() {
		{
			put("COUPON.INVEST.JD50.20220308", 990); // 京东卡50元概率0.01	需与990个祝福卡一起抽奖
			put("COUPON.INVEST.JD100.20220308", 995); // 京东卡100元概率0.005	需与995个祝福卡一起抽奖
			put("COUPON.INVEST.INSTAX.20220308", 999); // 富士instax拍立得相机概率0.001	需与999个祝福卡一起抽奖
			put("COUPON.INVEST.MORPHY.20220308", 999); // 摩飞多功能锅概率0.001	需与999个祝福卡一起抽奖
			put("COUPON.INVEST.DYSON.20220308", 999); // 戴森吹风机概率0.001	需与999个祝福卡一起抽奖
		}
	};

	private static String REDISKEYSTR_RED = "FESTIVAL38.RED";
	private static String REDISKEYSTR_L2 = "FESTIVAL38.L2";
	private static String REDISKEYSTR_GRAND = "FESTIVAL38.GRAND";

	@Autowired
	private ActPrizeBiz actPrizeBiz;

	@Autowired
	private CouponBiz couponBiz;

	@Autowired
	private ActPrizeDao actPrizeDao;

	@Autowired
	private FsdpBiz fsdpBiz;

	@Override
	public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, AccTokenUser user, String openId, Customer customer, String recommendCode) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("acitivityCode");
		}

		String customerId = customer == null ? null : customer.getCustomerId();

		boolean hasCustomerId = false;
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			customerId = getCustomerByMobile(user.getMobile());
			if(ObjectUtils.isNotEmptyOrNull(customerId)) {
				hasCustomerId = true;
			}
		}

		StatusDto isValidAct = isValid(activityCode);
		if (!isValidAct.isOk()) {
			return new StatusObjDto<CouponRecieveStatusDto>(false, isValidAct.getCode(), isValidAct.getMsg());
		}

		// 检查是否已领取过奖品
		List<ActPrize> prizes = actPrizeBiz.findPrize(activityCode, null, user.getUserId(), null, null, null, null);
		if(prizes.size() == 0) {
			if(ObjectUtils.isNotEmptyOrNull(customerId)) {
				prizes = actPrizeBiz.findPrize(activityCode, customerId, null, null, null, null, null);
			}

			//查看缓存，是否已领取到祝福卡
			if (prizes.size() == 0) {
				String key = activityCode+":ZFK:"+user.getUserId();
				boolean hasInit = ActivityRedis.ACT_ACTIVITY_INFO.exists(key);
				if (hasInit) {
					throw ActivityBizException.DOUBLE_RECIEVE_ALREADY;
				}
			}
		}

		if (prizes.size() > 0) {
			throw ActivityBizException.DOUBLE_RECIEVE_ALREADY;
		}

		// 开始领奖
		ActPrize prize = null;
		if(!ACT_GRAND_CODES.contains(activityCode)) {
			//抽普惠奖
			if(ObjectUtils.isNotEmptyOrNull(customerId) || (ObjectUtils.isEmptyOrNull(customerId) && hasCustomerId)) {
				// 有客户号的中奖概率  1.21元红包: 49.95%、L2: 49.95%、1000元红包： 0.10%
				if (Math.random() <= 0.4995) {
					prize = getOneAvailablePrize(activityCode, REDISKEYSTR_L2);
				} else {
				prize = getOneAvailablePrize(activityCode, REDISKEYSTR_RED);
				}
			}else {
				//无客户号，返回level2
				prize = getOneAvailablePrize(activityCode, REDISKEYSTR_L2);
			}
		}else {
			//抽大奖（实物奖品）
			if((ObjectUtils.isNotEmptyOrNull(customerId) && !isRecieveDelivered(activityCode, customerId, user.getUserId())) || (ObjectUtils.isEmptyOrNull(customerId) && hasCustomerId)) {
				prize = getOneAvailablePrize(activityCode, REDISKEYSTR_GRAND);
			}
		}

		// 缓存中没有，返回祝福卡
		if (prize == null) {
			String key = activityCode+":ZFK:"+user.getUserId();
			boolean hasInit = ActivityRedis.ACT_ACTIVITY_INFO.exists(key);
			if (hasInit) {
				throw ActivityBizException.DOUBLE_RECIEVE_ALREADY;
			}
			//将领取到祝福卡的用户记录到缓存中(缓存30天)
			ActivityRedis.ACT_ACTIVITY_INFO.set(key, user.getUserId());

			//提示获得一张祝福卡
			CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
			statusDto.setPrizeType("ZFK");
			statusDto.setPrizeName("祝福卡");
			statusDto.setStatus(ActPrize.STATUS_SEND);
			return new StatusObjDto<CouponRecieveStatusDto>(true, statusDto, StatusDto.SUCCESS, "");
		}

		// 更新奖品状态
		Integer status = ActPrize.STATUS_SEND;
		if(ObjectUtils.isEmptyOrNull(customerId) && !isCoupon(prize.getType())) {
			status = ActPrize.STATUS_OCCUPY;
		}

		// 若为优惠券，更新优惠券状态
		String redeemCode = prize.getRedeemCode();
		Map<String, Object> busparams = Maps.newHashMap();
		busparams.put("userId", user.getUserId());
		busparams.put("code", redeemCode);
		busparams.put("recommendCode", recommendCode);
		busparams.put("customerId", customerId);
		ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
		// 调用失败
		if (result.getCode() != 0) {
			logger.error("receivecoupon error: " + result.getMsg() + "|prizeId:" + prize.getId());
			throw new ActivityBizException(result.getCode(),result.getMsg());
		}

		MyPrizeDto2 dto = com.wlzq.common.utils.BeanUtils.mapToBean(result.getData(), MyPrizeDto2.class);
		if (ObjectUtils.isEmptyOrNull(prize.getRedEnvelopeAmt())) {
			prize.setRedEnvelopeAmt(dto.getRedenvelopeAmt());
		}

		prize.setStatus(status);
		updatePrize(user.getUserId(), openId, customerId, prize.getId(), status, "", "");

		prize.setUpdateTime(new Date());
		CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
		StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
		MyPrizeDto myPrizeDto = buildMyPrizeDto(prize, coupon.getObj());
		statusDto.setPrize(myPrizeDto);
		statusDto.setPrizeType(prize.getCode());
		statusDto.setStatus(ActPrize.STATUS_SEND);
		return new StatusObjDto<CouponRecieveStatusDto>(true, statusDto, StatusDto.SUCCESS, "");
	}

	@Override
	public StatusObjDto<List<CouponRecieveStatusDto>> status(String activityCode, AccTokenUser user, String customerId, String recommendCode) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}

		// 查看是否领取
		List<ActPrize> prizes = Lists.newArrayList();
		if(ObjectUtils.isNotEmptyOrNull(user)) {
			prizes = actPrizeBiz.findPrize(activityCode, null, user.getUserId(), null, null, null, null);
			if(prizes.size() == 0) {
				if(ObjectUtils.isNotEmptyOrNull(customerId)) {
					prizes = actPrizeBiz.findPrize(activityCode, customerId, null, null, null, null, null);
				}
				//查看缓存，是否已领取到祝福卡
				if (prizes.size() == 0) {
					String key = activityCode+":ZFK:"+user.getUserId();
					boolean hasInit = ActivityRedis.ACT_ACTIVITY_INFO.exists(key);
					if (hasInit) {
						List<CouponRecieveStatusDto> status = Lists.newArrayList();
						CouponRecieveStatusDto dto = new CouponRecieveStatusDto();
						dto.setStatus(CouponRecieveStatusDto.STATUS_RECIEVED);
						dto.setPrizeType("ZFK");
						dto.setPrizeName("祝福卡");
						status.add(dto);
						return new StatusObjDto<List<CouponRecieveStatusDto>>(true, status, StatusDto.SUCCESS, "");
					}
				}
			}
		}

		//初始化奖品到缓存中(level2与其他奖品需要分别缓存)
		int notSend = 0;
		List<Long> prizeL2List = Lists.newArrayList();
		List<Long> prizeRedList = Lists.newArrayList();
		if(!ACT_GRAND_CODES.contains(activityCode)) {
			//初始化普惠奖
			List<ActPrize> prizeTypes = actPrizeDao.findDistinctPrizes(activityCode, null);
			for(ActPrize p : prizeTypes) {
				if(p.getType() == 6) {
					//level2
					List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, p.getCode());
					prizeL2List.addAll(prizeList);
				}
				if(p.getType() == 9) {
					//红包
					List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, p.getCode());
					prizeRedList.addAll(prizeList);
				}
			}

			notSend = prizeL2List.size() + prizeRedList.size();
			if(prizeL2List.size() > 0) {
				initPrizes(activityCode, REDISKEYSTR_L2, prizeL2List);
			}
			if(prizeRedList.size() > 0) {
				initPrizes(activityCode, REDISKEYSTR_RED, prizeRedList);
			}
		}else {
			//初始化大奖
			List<ActPrize> prizeTypes = actPrizeDao.findDistinctPrizes(activityCode, null);
			for(ActPrize p : prizeTypes) {
				List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, p.getCode());
				if(prizeList.size() > 0) {
					for(Map.Entry<String, Integer> entry : GRAND_PRIZE.entrySet()) {
						if(entry.getKey().equals(p.getCode())) {
							List<Long> pList = Lists.newArrayList();
							for(int i=0; i<entry.getValue(); i++) {
								pList.add((0L-i));
							}
							pList.addAll(prizeList);
							notSend = prizeList.size();
							if(pList.size() > 0) {
								initPrizes(activityCode, REDISKEYSTR_GRAND, pList);
							}
							break;
						}
					}
				}
			}
		}

		boolean hasCustomerId = false;
		if (ObjectUtils.isEmptyOrNull(customerId) && ObjectUtils.isNotEmptyOrNull(user)) {
			customerId = getCustomerByMobile(user.getMobile());
			if(ObjectUtils.isNotEmptyOrNull(customerId)) {
				hasCustomerId = true;
			}
		}

		// 领取状态
		List<CouponRecieveStatusDto> status = Lists.newArrayList();
		Integer recieveStatus = prizes.size() > 0 ? CouponRecieveStatusDto.STATUS_RECIEVED : CouponRecieveStatusDto.STATUS_NOT_RECIEVED;

		ActPrize prizeInfo = null;
		if (prizes.size() > 0) {// 覆盖状态为已领取
			BeanUtils.copyProperties(prizes.get(0), prizeInfo = new ActPrize());
			recieveStatus = CouponRecieveStatusDto.STATUS_RECIEVED;
		} else {
			String redisKey = "";
			if(!ACT_GRAND_CODES.contains(activityCode)) {
				//抽普惠奖
				if(ObjectUtils.isNotEmptyOrNull(customerId) || (ObjectUtils.isEmptyOrNull(customerId) && hasCustomerId)) {
					//有客户号，返回红包
					redisKey = activityCode +":" + REDISKEYSTR_RED;
				}else {
					//无客户号，返回level2
					redisKey = activityCode +":" + REDISKEYSTR_L2;
				}
			}else {
				//抽大奖（实物奖品）
				if((ObjectUtils.isNotEmptyOrNull(customerId) && !isRecieveDelivered(activityCode, customerId, user.getUserId())) || (ObjectUtils.isEmptyOrNull(customerId) && hasCustomerId)) {
					redisKey = activityCode +":" + REDISKEYSTR_GRAND;
				}
			}

			String id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sRandomMember(redisKey));
			if (ObjectUtils.isNotEmptyOrNull(id) && Integer.valueOf(id) > 0) {
				prizeInfo =  actPrizeDao.get(id);
			}
		}

		CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
		statusDto.setLeftCount(notSend);
		//recieveStatus = prizeInfo == null ? CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE : recieveStatus;
		recieveStatus = (prizeInfo == null && notSend == 0)? CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE : recieveStatus;

		statusDto.setPrizeType(ObjectUtils.isNotEmptyOrNull(prizeInfo)?prizeInfo.getCode():"ZFK");
		statusDto.setStatus(recieveStatus);

		if (prizeInfo != null) {
			Integer type = prizeInfo.getType();
			MyPrizeDto prize = new MyPrizeDto();
			prize.setType(type);
			prize.setName(prizeInfo.getName());
			String code = prizes.size() > 0 ? prizeInfo.getRedeemCode() : null;
			prize.setCode(code);
			prize.setRecieveTime(prizeInfo.getUpdateTime());
			prize.setWorth(prizeInfo.getWorth());
			prize.setTime(prizeInfo.getTime());
			StatusObjDto<CouponInfo> couponStatus = couponBiz.couponInfo(null, prizeInfo.getRedeemCode());
			CouponInfo coupon = couponStatus.getObj();
			if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS) && ObjectUtils.isNotEmptyOrNull(coupon.getSendTime())) {
				prize.setValidityDateFrom(coupon.getSendTime());
				prize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(coupon.getSendTime(), coupon.getValidityDays())));
			} else if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_TIME_RANGE)) {
				prize.setValidityDateFrom(coupon.getValidityDateFrom());
				prize.setValidityDateTo(coupon.getValidityDateTo());
			}
			prize.setDiscount(coupon.getDiscount());
			prize.setDescription(coupon.getDescription());
			prize.setRegulation(coupon.getRegulation());
			prize.setOpenUrl(coupon.getOpenUrl());
			prize.setRecieveTime(coupon.getSendTime());
			prize.setStatus(coupon.getStatus());
			prize.setCouponType(coupon.getType());
			// 设置当前是否可用
			Integer isCurrentEnable = CodeConstant.CODE_NO;
			if (prize.getStatus() != null && MyPrizeDto.STATUS_NOT_USED.compareTo(prize.getStatus()) >= 0 && prize.getValidityDateFrom() != null && prize.getValidityDateTo() != null) {
				Date now = new Date();
				isCurrentEnable = now.getTime() > prize.getValidityDateFrom().getTime() && now.getTime() < prize.getValidityDateTo().getTime() ? CodeConstant.CODE_YES : CodeConstant.CODE_NO;
			}
			prize.setIsCurrentEnable(isCurrentEnable);

			prize.setIsDeliveredInfo(0);
			if(CouponRecieveStatusDto.STATUS_RECIEVED.equals(recieveStatus) && ActPrize.TYPE_DELIVERED.equals(type)) {
				Integer findDeliveredCount = actPrizeDao.findDeliveredCount(prizeInfo.getRedeemCode());
				if(findDeliveredCount > 0) {
					prize.setIsDeliveredInfo(1);
				}
			}

			statusDto.setPrize(prize);
		}

		status.add(statusDto);
		return new StatusObjDto<List<CouponRecieveStatusDto>>(true, status, StatusDto.SUCCESS, "");
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
		logger.info("初始化大转盘奖品.............................");

		/** 中奖奖品初始化 **/
		if (prizes == null) {
			logger.info("活动奖品未配置.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动奖品未配置");
		}
		if (prizes.size() == 0) {
			logger.info("无可用的活动奖品.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("无可用奖品");
		}

		/** 奖品 **/
		int total = 0;
		for (int i = 0; i < prizes.size(); i++) {
			ActivityRedis.ACT_ACTVITY_PRIZE.sadd(redisKey, prizes.get(i));
			total++;
		}
		logger.info("初始化大转盘奖品完毕.............................,total" + total + ",prize:" + prizes.size());
	}

	public ActPrize getOneAvailablePrize(String activityCode, String prizeTypeCode) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			return null;
		}
		if (ObjectUtils.isEmptyOrNull(prizeTypeCode)) {
			return null;
		}
		String redisKey = activityCode + ":" + prizeTypeCode;
		String id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sPop(redisKey));
		if (ObjectUtils.isEmptyOrNull(id)) {
			return null;
		}
		ActPrize prize = actPrizeDao.get(id);

		return prize;
	}

	public static MyPrizeDto buildMyPrizeDto(ActPrize prizeInfo, CouponInfo coupon) {
		MyPrizeDto prize = new MyPrizeDto();
		if (prize != null) {
			prize.setType(prizeInfo.getType());
			prize.setName(prizeInfo.getName());
			prize.setCode(prizeInfo.getRedeemCode());
			prize.setRecieveTime(prizeInfo.getUpdateTime());
			prize.setWorth(prizeInfo.getWorth());
			prize.setTime(prizeInfo.getTime());
			prize.setRedEnvelopeAmt(prizeInfo.getRedEnvelopeAmt());
		}
		if (coupon != null) {
			if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS) && ObjectUtils.isNotEmptyOrNull(coupon.getSendTime())) {
				prize.setValidityDateFrom(coupon.getSendTime());
				prize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(coupon.getSendTime(), coupon.getValidityDays())));
			} else if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_TIME_RANGE)) {
				prize.setValidityDateFrom(coupon.getValidityDateFrom());
				prize.setValidityDateTo(coupon.getValidityDateTo());
			}
			prize.setDiscount(coupon.getDiscount());
			prize.setDescription(coupon.getDescription());
			prize.setRegulation(coupon.getRegulation());
			prize.setOpenUrl(coupon.getOpenUrl());
			prize.setRecieveTime(coupon.getSendTime());
			prize.setStatus(coupon.getStatus());
			prize.setCouponType(coupon.getType());
			// 设置当前是否可用
			Integer isCurrentEnable = CodeConstant.CODE_NO;
			if (prize.getStatus() != null && MyPrizeDto.STATUS_NOT_USED.compareTo(prize.getStatus()) >= 0 && prize.getValidityDateFrom() != null
					&& prize.getValidityDateTo() != null) {
				Date now = new Date();
				isCurrentEnable = now.getTime() > prize.getValidityDateFrom().getTime() && now.getTime() < prize.getValidityDateTo().getTime() ? CodeConstant.CODE_YES
						: CodeConstant.CODE_NO;
			}
			prize.setIsCurrentEnable(isCurrentEnable);
		}
		return prize;
	}

	/**
	 * 更新奖品状态
	 *
	 * @param userId
	 * @param openId
	 * @param customerId
	 * @param id
	 * @param status
	 * @param mobile
	 *            TODO
	 * @param remark
	 *            TODO
	 */
	public void updatePrize(String userId, String openId, String customerId, Long id, Integer status, String mobile, String remark) {
		if (ObjectUtils.isEmptyOrNull(id) || ObjectUtils.isEmptyOrNull(status)) {
			return;
		}
		if (status < ActPrize.STATUS_NOT_SEND || status > ActPrize.STATUS_OCCUPY)
			return;

		ActPrize prize = actPrizeDao.get(id.toString());
		prize.setUserId(userId);
		prize.setOpenId(openId);
		prize.setStatus(status);
		prize.setCustomerId(customerId);
		prize.setUpdateTime(new Date());
		prize.setMobile(mobile);
		prize.setRemark(remark);
		actPrizeDao.update(prize);
	}

	private boolean isCoupon(Integer type) {
		//类型，1：折扣券，2：免单券，3：满减券,4:权益券，5：额度券，6-level2,7-京东卡,9-红包,10-融券费率
		if(type == 6 || type == 7 || type == 9) {
			return true;
		}
		return false;
	}

	//判断手机号是是否有对应的客户号
	@Override
    public String getCustomerByMobile(String mobile) {
		logger.info("fsdp call to get customer property");
		StatusObjDto<List<AccAccountOpeninfo>> khkhxx = fsdpBiz.khkhxx(null,null,mobile);
		logger.info("return:" + JsonUtils.object2JSON(khkhxx));
		if(khkhxx.isOk() && khkhxx.getObj().size() > 0) {
			return khkhxx.getObj().get(0).getCustomerId();
		}

		return null;
	}

	// 本次活动是否已领取过实物类奖品、京东卡
	boolean isRecieveDelivered(String activityCode, String customerId, String userId) {
		if(ObjectUtils.isNotEmptyOrNull(customerId)) {
			List<ActPrize> customerPrizes = actPrizeBiz.findCustomerPrizes(ACT_GRAND_CODES, customerId);
			for(ActPrize p : customerPrizes) {
				//实物类奖品、京东卡
				if(ActPrize.TYPE_DELIVERED.equals(p.getType()) || ActPrize.TYPE_AIQY.equals(p.getType())) {
					return true;
				}
			}
		}

		if(ObjectUtils.isNotEmptyOrNull(userId)) {
			List<ActPrize> customerPrizes = actPrizeBiz.findUserPrizes(ACT_GRAND_CODES, null, userId);
			for(ActPrize p : customerPrizes) {
				//实物类奖品、京东卡
				if(ActPrize.TYPE_DELIVERED.equals(p.getType()) || ActPrize.TYPE_AIQY.equals(p.getType())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public StatusObjDto<String> checkmobile(String mobile) {
		if(ObjectUtils.isEmptyOrNull(mobile)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
		}
		String customerId = getCustomerByMobile(mobile);

		String result = mobile + "无客户号";
		if(ObjectUtils.isNotEmptyOrNull(customerId)) {
			result = mobile + "对应的客户号为" + customerId;
		}

		return new StatusObjDto<String>(true, result, StatusDto.SUCCESS, "");
	}

}
