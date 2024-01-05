package com.wlzq.activity.actWL20.biz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.actWL20.biz.ActCouponZBBiz;
import com.wlzq.activity.actWL20.dao.ActCouponZBDao;
import com.wlzq.activity.actWL20.dto.MyPrizeDto2;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.remote.service.utils.RemoteUtils;

@Service
public class ActCouponZBBizImp extends ActivityBaseBiz implements ActCouponZBBiz {

	private Logger logger = LoggerFactory.getLogger(ActCouponZBBizImp.class);

	private static String REDISKEYSTR = "COUPON.INVEST.ZB.FREE.RED";

	@Autowired
	private ActPrizeBiz actPrizeBiz;

	@Autowired
	private CouponBiz couponBiz;

	@Autowired
	private ActCouponZBDao actCouponZBDao;

	@Autowired
	private ActPrizeDao actPrizeDao;

	@Override
	public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, String userId, String openId, Customer customer, String recommendCode) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("acitivityCode");
		}

		String customerId = customer == null ? null : customer.getCustomerId();

		StatusDto isValidAct = isValid(activityCode);
		if (!isValidAct.isOk()) {
			return new StatusObjDto<CouponRecieveStatusDto>(false, isValidAct.getCode(), isValidAct.getMsg());
		}

		// 不可重复， 判断有没有获取过奖品
		List<ActPrize> prizes = actPrizeBiz.findPrize(activityCode, customerId, null, null, null, null, null);
		if (prizes.size() > 0) {
			throw ActivityBizException.DOUBLE_RECIEVE_ALREADY;
		}

		// 开始领奖
		ActPrize prize = new ActPrize();
		// 1.优先领取红包和免单券
		prize = getOneAvailablePrize(activityCode, REDISKEYSTR);
		if (prize == null) {
			// 2.红包和免单券领完后领取5折券
			List<ActPrize> prizeTypes = actCouponZBDao.findPrizeTypeCode(activityCode);

			for (ActPrize actP : prizeTypes) {
				if (actP.getType() == 2 || actP.getType() == 9) {
					// 红包和免单券跳过
					continue;
				}
				prize = getOneAvailablePrize(activityCode, actP.getCode());
				if (prize != null) {
					break;
				}
			}
		}

		// 缓存中没有，返回祝福卡
		if (prize == null) {
			String key = activityCode+":ZFK:"+userId;
			boolean hasInit = ActivityRedis.ACT_ACTIVITY_INFO.exists(key);
			if (hasInit) {
				throw ActivityBizException.DOUBLE_RECIEVE_ALREADY;
			}
			//将领取到祝福卡的用户记录到缓存中(缓存30天)
			ActivityRedis.ACT_ACTIVITY_INFO.set(key, userId);
			
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
		busparams.put("userId", userId);
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
		updatePrize(userId, openId, customerId, prize.getId(), status, "", "");

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
	public StatusObjDto<List<CouponRecieveStatusDto>> status(String activityCode, String userId, String customerId, String recommendCode) {
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}

		// 查看是否领取
		List<ActPrize> prizes = actCouponZBDao.findPrizeByUserId(activityCode, null, customerId);

		//有领取记录则跳过初始化产品逻辑
		int notSend = 0;
		List<ActPrize> prizeTypes = actCouponZBDao.findPrizeTypeCode(activityCode);
		String prizeTypeCode = REDISKEYSTR;
		if (prizes == null || prizes.size() == 0) {
			
			//查看缓存是否已领取到祝福卡
			String key = activityCode+":ZFK:"+userId;
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
			
			// 初始化直播奖品到redis
			// 去重奖品prizeType
			Set<ActPrize> typeUnique = new TreeSet<>((o1, o2) -> o1.getCode().compareTo(o2.getCode()));
			typeUnique.addAll(prizeTypes);
			prizeTypes = new ArrayList<>(typeUnique);
			
			List<Long> tempPrizeList = new ArrayList();
			for (ActPrize prize : prizeTypes) {
				prizeTypeCode = prize.getCode();
				List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, prizeTypeCode);
				
				List<ActPrize> notSendList = actPrizeBiz.findPrize(activityCode, prizeTypeCode, null, null, null, null, ActPrize.STATUS_NOT_SEND, null, null);
				notSend = notSend + notSendList.size();
				
				// 红包和免单券要优先同时抽奖
				if (prize.getType() == 2 || prize.getType() == 9) {
					tempPrizeList.addAll(prizeList);
					prizeTypeCode = REDISKEYSTR; // 用于redis的key
				} else {
					initPrizes(activityCode, prizeTypeCode, prizeList);
				}
				
			}
			// 红包和免单券放入同一个缓存
			initPrizes(activityCode, prizeTypeCode, tempPrizeList);
		}

		// 领取状态
		List<CouponRecieveStatusDto> status = Lists.newArrayList();
		CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
		statusDto.setLeftCount(notSend);
		Integer recieveStatus = prizes.size() > 0 ? CouponRecieveStatusDto.STATUS_RECIEVED : CouponRecieveStatusDto.STATUS_NOT_RECIEVED;
		ActPrize prizeInfo = null;
		if (prizes.size() > 0) {// 覆盖状态为已领取
			BeanUtils.copyProperties(prizes.get(0), prizeInfo = new ActPrize());
			recieveStatus = CouponRecieveStatusDto.STATUS_RECIEVED;
		} else {
			// 优先返回 红包和免单券			
			String redisKey = activityCode +":" + REDISKEYSTR;
			String id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sRandomMember(redisKey));
			if (!ObjectUtils.isEmptyOrNull(id) ) {
				prizeInfo =  actPrizeDao.get(id);
			}
			
			if (prizeInfo == null) {
				for (ActPrize prize : prizeTypes) {
					// 返回其他类型的奖品（2：免单券 9：红包）
					if (prize.getType() != 2 && prize.getType() != 9) {
						List<Long> prizeList = actPrizeDao.findAvailablePrizesByType(activityCode, prizeTypeCode);
						if (prizeList.size() > 0) {
							ActPrize ePrize = new ActPrize();
							ePrize.setActivityCode(activityCode);
							ePrize.setCode(prize.getCode());
							List<ActPrize> ePrizeList = actPrizeDao.findList(ePrize);
							prizeInfo = ePrizeList.stream().findAny().orElse(null);
						}
					}

				}
			}
		}

		recieveStatus = prizeInfo == null ? CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE : recieveStatus;
		
		statusDto.setPrizeType(prizeInfo.getCode());
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

	private static MyPrizeDto buildMyPrizeDto(ActPrize prizeInfo, CouponInfo coupon) {
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

}
