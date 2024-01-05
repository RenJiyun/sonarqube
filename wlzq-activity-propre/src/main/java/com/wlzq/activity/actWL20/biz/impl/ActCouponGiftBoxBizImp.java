package com.wlzq.activity.actWL20.biz.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.actWL20.biz.ActCouponGiftBoxBiz;
import com.wlzq.activity.actWL20.dao.GiftBoxDao;
import com.wlzq.activity.actWL20.dto.MyPrizeDto2;
import com.wlzq.activity.actWL20.model.ActGiftBox;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dao.ActShareDao;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActShare;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccAccountOpeninfo;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.AccUser;
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
import com.wlzq.remote.service.common.account.AccountUserBiz;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.remote.service.common.base.FsdpBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ActCouponGiftBoxBizImp extends ActivityBaseBiz implements ActCouponGiftBoxBiz {

	private Logger logger = LoggerFactory.getLogger(ActCouponGiftBoxBizImp.class);
	
	private static String ACTIVITY_CODE = "ACTIVITY.GIFTBOX.202111"; // 11月拆神秘礼盒活动
	
	private static HashMap<Integer, String> PRIZE_COUPON_TEMPLATES = new HashMap<Integer, String>() {
		{
			put(1, "COUPON.INVEST.GIFTBOX.RED.202111"); // 第一类用户-领现金红包1.88
			put(2, "COUPON.DTCP034.GIFTBOX.F7.202111"); // 第二类用户-领红山价投大师7天免费体验券
			put(3, "COUPON.DTCP034.GIFTBOX.F7.202111"); // 第三类用户-领红山价投大师7天免费体验券
			put(4, "COUPON.INVEST.GIFTBOX.A1.202111"); // 其他用户-领A股level-2一个月
			put(5, "COUPON.INVEST.GIFTBOX.RED2.202111"); // 第二次领取-领取红包0.5
			put(6, "COUPON.INVEST.GIFTBOX.RED888.202111"); // 第二次领取-领取红包888
			put(7, "COUPON.DTCP022.GIFTBOX.F7.202111"); // 第二次领取-复利成长先锋7天免费体验券
		}
	};

	@Autowired
	private	CouponBiz couponBiz;
	@Autowired
	private ActPrizeBiz actPrizeBiz;
	@Autowired
    private  AccountUserBiz userBiz;
	@Autowired
	private ActPrizeDao actPrizeDao;
	@Autowired
	private GiftBoxDao giftBoxDao;
	@Autowired
	private ActShareDao shareDao;
	@Autowired
	private FsdpBiz fsdpBiz;

	@Override
	public StatusObjDto<CouponRecieveStatusDto> recieve(AccTokenUser user, Customer customer, String recommendCode, String shareCode) {
		
		// 获取用户类型
		ActGiftBox gifgBox = new ActGiftBox();
		gifgBox.setMobile(user.getMobile());
		gifgBox.setActivityCode(ACTIVITY_CODE);
		List<ActGiftBox> gifgBoxList = giftBoxDao.findList(gifgBox);
		
		Integer userType=ActGiftBox.USER_TYPE_4;
		if(gifgBoxList != null && gifgBoxList.size() > 0) {
			userType = gifgBoxList.get(0).getUserType();
		}
		
		String prizeType = PRIZE_COUPON_TEMPLATES.get(userType);

		String customerId = customer == null ? null : customer.getCustomerId();

		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if (!isValidAct.isOk()) {
			return new StatusObjDto<CouponRecieveStatusDto>(false, isValidAct.getCode(), isValidAct.getMsg());
		}
		
		AccUser shareUser = getUserByShareCode(shareCode);
		
		Integer isHasCustomer = CodeConstant.CODE_NO;	//是否有客户号
		if(isHasCustomer(user.getMobile())) {
			isHasCustomer = CodeConstant.CODE_YES;
		}
		
		ActPrize prize = new ActPrize();
		//查询领取记录
		List<ActPrize> prizes = actPrizeBiz.findPrize(ACTIVITY_CODE, null, user.getUserId(), null, null, null, null);
		boolean isFirstOpen = prizes.size() == 0?true:false;	//是否第一次开礼盒
		if(isFirstOpen) {
			if (ObjectUtils.isNotEmptyOrNull(shareCode)) {
				ActShare share = new ActShare();
				share.setActivity(ACTIVITY_CODE);
				share.setUserId(shareUser.getUserId());
				List<ActShare> shareList = shareDao.findList(share);
				if(shareList.size() == 0) {
					throw BizException.COMMON_CUSTOMIZE_ERROR.format("分享码邀请记录不存在");
				}
				ActShare shareU = shareList.get(0);
				shareU.setIsUse(1);
				shareDao.update(shareU);
			}
		}else {
			//不是第一次开礼盒时，检查是否有通过分享增加一次抽奖机会
			ActShare share = new ActShare();
			share.setActivity(ACTIVITY_CODE);
			share.setUserId(user.getUserId());
			share.setIsUse(1);
			List<ActShare> shareList = shareDao.findList(share);
			boolean shareIsUse = shareList.size() > 0?true:false;	//是否分享成功
			
			if(shareIsUse) {
				//分享成功后可增加一次领取机会，最多只能开两次礼盒
				if (prizes.size() > 1) {
					throw ActivityBizException.ACT_GIFTBOX_RECIEVE_OK;
				}
			}else {
				throw ActivityBizException.ACT_GIFTBOX_RECIEVE_OK;
			}
			
			//第二次领取需要判断手机号是否有对于的客户号
			if(CodeConstant.CODE_YES.equals(isHasCustomer)) {
				//第二次领取并且有客户号
				prizeType = PRIZE_COUPON_TEMPLATES.get(5);
				prize = getOneAvailablePrize(ACTIVITY_CODE, prizeType);
				if (prize == null) {
					//0.5元红包发放完毕则发放888元红包
					prizeType = PRIZE_COUPON_TEMPLATES.get(6);
				}
			}else {
				//第二次领取并且没有客户号
				prizeType = PRIZE_COUPON_TEMPLATES.get(7);
			}
		}
		
		// region 开始领取奖品
		prize = getOneAvailablePrize(ACTIVITY_CODE, prizeType);
		if (prize == null) {
			throw ActivityBizException.ACT_EMPTY_PRIZE;
		}

		// 更新奖品状态
		Integer status = ActPrize.STATUS_SEND;
		if(ObjectUtils.isEmptyOrNull(customerId) && !isCoupon(prize.getType())) {
			status = ActPrize.STATUS_OCCUPY;
		}

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
		updatePrize(user.getUserId(), user.getOpenid(), customerId, prize.getId(), status, "", "");
		//end region
				
		prize.setUpdateTime(new Date());
		CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
		StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
		MyPrizeDto myPrizeDto = buildMyPrizeDto(prize, coupon.getObj());
		statusDto.setPrize(myPrizeDto);
		statusDto.setPrizeType(prize.getCode());
		statusDto.setStatus(ActPrize.STATUS_SEND);
		statusDto.setLeftRecieveTimes(0);
		statusDto.setIsHasCustomer(isHasCustomer);
		statusDto.setRecieveCount(prizes.size() + 1);
		
		return new StatusObjDto<CouponRecieveStatusDto>(true, statusDto, StatusDto.SUCCESS, "");
	}

	@Override
	public List<ActGiftBox> findGiftBoxes(ActGiftBox actGiftBox){
		ActGiftBox gifgBox = new ActGiftBox();
		gifgBox.setMobile(actGiftBox.getMobile());
		gifgBox.setActivityCode(actGiftBox.getActivityCode());
		return giftBoxDao.findList(gifgBox);
	}


	@Override
	public StatusObjDto<List<CouponRecieveStatusDto>> findRecieves(AccTokenUser user, Customer customer,String shareCode) {
		initPrizesToRedis();
		
		List<ActPrize> prizes = Lists.newArrayList();
		Integer isHasCustomer = CodeConstant.CODE_NO;	//是否有客户号
		Integer leftRecieveTimes = 1;	//剩余领取次数
		
		// 获取用户类型
		Integer userType=ActGiftBox.USER_TYPE_4;
		if(ObjectUtils.isNotEmptyOrNull(user)) {
			if (ObjectUtils.isEmptyOrNull(user.getMobile())) {
				throw ActivityBizException.ACT_GIFTBOX_NO_MOBILE;
			}
			
			ActGiftBox gifgBox = new ActGiftBox();
			gifgBox.setMobile(user.getMobile());
			gifgBox.setActivityCode(ACTIVITY_CODE);
			List<ActGiftBox> gifgBoxList = giftBoxDao.findList(gifgBox);
			
			if(gifgBoxList != null && gifgBoxList.size() > 0) {
				userType = gifgBoxList.get(0).getUserType();
			}
			
			//查询领取记录
			ActPrize prize = new ActPrize();
			prize.setActivityCode(ACTIVITY_CODE);
			prize.setUserId(user.getUserId());
			prizes = actPrizeDao.findList(prize);
			
			if(isHasCustomer(user.getMobile())) {
				isHasCustomer = CodeConstant.CODE_YES;
			}
		}
		
		//region 显示第二次领取奖品
		ActPrize secondP = new ActPrize();
		if(prizes.size() > 0) {
			secondP = prizes.get(0);
		}
		if(prizes.size() > 1) {
			for(ActPrize p : prizes) {
				if(p.getCode().equals(PRIZE_COUPON_TEMPLATES.get(5)) || p.getCode().equals(PRIZE_COUPON_TEMPLATES.get(6)) || p.getCode().equals(PRIZE_COUPON_TEMPLATES.get(7))) {
					secondP = p;
					break;
				}
			}
		}
		//end region 显示第二次领取奖品
		
		String prizeType = prizes.size()>0?secondP.getCode():PRIZE_COUPON_TEMPLATES.get(userType);
		Integer recieveStatus = prizes.size() > 0 ? CouponRecieveStatusDto.STATUS_RECIEVED : CouponRecieveStatusDto.STATUS_NOT_RECIEVED;
		
		if(recieveStatus.equals(CouponRecieveStatusDto.STATUS_RECIEVED)) {
			leftRecieveTimes = 0;
		}
		
		//查询分享记录
		if(ObjectUtils.isNotEmptyOrNull(user)) {
			ActShare share = new ActShare();
			share.setActivity(ACTIVITY_CODE);
			share.setUserId(user.getUserId());
			share.setIsUse(1);
			List<ActShare> shareList = shareDao.findList(share);
			boolean shareIsUse = shareList.size() > 0?true:false;	//是否分享成功
			if(shareIsUse) {
				//分享成功后可增加一次开礼盒机会
				if (prizes.size() < 2) {
					leftRecieveTimes = 1;
					
					if(CodeConstant.CODE_YES.equals(isHasCustomer)) {
						//第二次领取并且有客户号
						prizeType = PRIZE_COUPON_TEMPLATES.get(5);
						ActPrize prize5 = getOneAvailablePrize(ACTIVITY_CODE, prizeType);
						if (prize5 == null) {
							//0.5元红包发放完毕则发放888元红包
							prizeType = PRIZE_COUPON_TEMPLATES.get(6);
						}
					}else {
						//第二次领取并且没有客户号
						prizeType = PRIZE_COUPON_TEMPLATES.get(7);
					}
				}
			}
		}
		
		ActPrize aPrize = null;

		if (leftRecieveTimes > 0) {
			if(ObjectUtils.isEmptyOrNull(user)) {
				//用户未登录，随机返回一个奖品
				HashMap<Integer, String> prizeMaps = Maps.newHashMap(PRIZE_COUPON_TEMPLATES);
				Set<Map.Entry<Integer, String>> set = prizeMaps.entrySet();
				Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
				while(iterator.hasNext()) {
					Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) iterator.next();
					
					try {
						aPrize = actPrizeBiz.findOneAvailablePrize(ACTIVITY_CODE, entry.getValue());
					} catch(Exception e) {
						aPrize = null;
					}
					
					if(ObjectUtils.isNotEmptyOrNull(aPrize)) {
						prizeType = aPrize.getCode();
						break;
					}
				}
			}else {
				try {
					aPrize = actPrizeBiz.findOneAvailablePrize(ACTIVITY_CODE, prizeType);
				} catch(Exception e) {
					aPrize = null;
					logger.info(prizeType+"无可用的活动奖品.............................");
				}
			}
		}
		Integer notSend = actPrizeBiz.findPrizeCount(ACTIVITY_CODE, prizeType, null, null, null, null, ActPrize.STATUS_NOT_SEND, null, null);
		recieveStatus = notSend.equals(0)&&!recieveStatus.equals(CouponRecieveStatusDto.STATUS_RECIEVED)?CouponRecieveStatusDto.STATUS_RECIEVE_COMPLETE:recieveStatus;
		
		CouponRecieveStatusDto statusDto = new CouponRecieveStatusDto();
		statusDto.setLeftCount(notSend);
		statusDto.setPrizeType(prizeType);
		statusDto.setStatus(recieveStatus);
		statusDto.setLeftRecieveTimes(leftRecieveTimes);
		statusDto.setIsHasCustomer(isHasCustomer);
		statusDto.setRecieveCount(prizes.size());
		
		//通过分享码领取时需要返回分享人手机号
		AccUser shareUser = getUserByShareCode(shareCode);
		if(shareUser != null) {
			statusDto.setShareMobile(shareUser.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
		}

		ActPrize prizeInfo = new ActPrize();
		if (prizes.size() > 0 && recieveStatus.equals(CouponRecieveStatusDto.STATUS_RECIEVED)) {// 覆盖状态为已领取
			//region 显示第二次领取奖品
			ActPrize secondPrize = prizes.get(0);
			if(prizes.size() > 1) {
				for(ActPrize p : prizes) {
					if(p.getCode().equals(PRIZE_COUPON_TEMPLATES.get(5)) || p.getCode().equals(PRIZE_COUPON_TEMPLATES.get(6))) {
						secondPrize = p;
						break;
					}
				}
			}
			//end region 显示第二次领取奖品
			
			BeanUtils.copyProperties(secondPrize, prizeInfo);
		} else {
			prizeInfo = aPrize;
		}

		if (prizeInfo != null) {
			Integer type = prizeInfo.getType();
			MyPrizeDto mPrize = new MyPrizeDto();
			mPrize.setType(type);
			mPrize.setName(prizeInfo.getName());
			String code = prizes.size() > 0 ? prizeInfo.getRedeemCode() : null;
			mPrize.setCode(code);
			mPrize.setRecieveTime(prizeInfo.getUpdateTime());
			mPrize.setWorth(prizeInfo.getWorth());
			mPrize.setTime(prizeInfo.getTime());
			StatusObjDto<CouponInfo> couponStatus = couponBiz.couponInfo(prizeInfo.getCode(), prizeInfo.getRedeemCode());
			CouponInfo coupon = couponStatus.getObj();
			if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS) && ObjectUtils.isNotEmptyOrNull(coupon.getSendTime())) {
				mPrize.setValidityDateFrom(coupon.getSendTime());
				mPrize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(coupon.getSendTime(), coupon.getValidityDays())));
			} else if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_TIME_RANGE)) {
				mPrize.setValidityDateFrom(coupon.getValidityDateFrom());
				mPrize.setValidityDateTo(coupon.getValidityDateTo());
			}
			mPrize.setDiscount(coupon.getDiscount());
			mPrize.setDescription(coupon.getDescription());
			mPrize.setRegulation(coupon.getRegulation());
			mPrize.setOpenUrl(coupon.getOpenUrl());
			mPrize.setRecieveTime(coupon.getSendTime());
			mPrize.setStatus(coupon.getStatus());
			mPrize.setCouponType(coupon.getType());
			// 设置当前是否可用
			Integer isCurrentEnable = CodeConstant.CODE_NO;
			if (mPrize.getStatus() != null && MyPrizeDto.STATUS_NOT_USED.compareTo(mPrize.getStatus()) >= 0 && mPrize.getValidityDateFrom() != null && mPrize.getValidityDateTo() != null) {
				Date now = new Date();
				isCurrentEnable = now.getTime() > mPrize.getValidityDateFrom().getTime() && now.getTime() < mPrize.getValidityDateTo().getTime() ? CodeConstant.CODE_YES : CodeConstant.CODE_NO;
			}
			mPrize.setIsCurrentEnable(isCurrentEnable);
			statusDto.setPrize(mPrize);
		} else {
			/** 未获得奖品，且已无可用奖品，设置奖品信息 **/
			ActPrize ePrize = new ActPrize();
			ePrize.setActivityCode(ACTIVITY_CODE);
			ePrize.setCode(prizeType);
			List<ActPrize> ePrizeList = actPrizeDao.findList(ePrize);
			prizeInfo = ePrizeList.stream().findAny().orElse(null);
			MyPrizeDto mPrize = new MyPrizeDto();
			if(prizeInfo != null) {
				mPrize.setType(prizeInfo.getType());
				mPrize.setName(prizeInfo.getName());
				mPrize.setWorth(prizeInfo.getWorth());
				mPrize.setTime(prizeInfo.getTime());
			}
			mPrize.setIsCurrentEnable(CodeConstant.CODE_NO);
			statusDto.setPrize(mPrize);
		}
		List<CouponRecieveStatusDto> status = Lists.newArrayList();
		status.add(statusDto);

		return new StatusObjDto<List<CouponRecieveStatusDto>>(true,status,StatusDto.SUCCESS,"");
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
	
	private boolean isCoupon(Integer type) {
		//类型，1：折扣券，2：免单券，3：满减券,4:权益券，5：额度券，6-level2,7-京东卡,9-红包,10-融券费率
		if(type == 6 || type == 7 || type == 9) {
			return true;
		}
		return false;
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
		if (status < ActPrize.STATUS_NOT_SEND || status > ActPrize.STATUS_OCCUPY) {
			return;
		}

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
			if (prize.getStatus() != null && MyPrizeDto.STATUS_NOT_USED.compareTo(prize.getStatus()) >= 0 && prize.getValidityDateFrom() != null && prize.getValidityDateTo() != null) {
				Date now = new Date();
				isCurrentEnable = now.getTime() > prize.getValidityDateFrom().getTime() && now.getTime() < prize.getValidityDateTo().getTime() ? CodeConstant.CODE_YES : CodeConstant.CODE_NO;
			}
			prize.setIsCurrentEnable(isCurrentEnable);
		}
		return prize;
	}
	
	//判断手机号是是否有对应的客户号
	boolean isHasCustomer(String mobile) {
		logger.info("fsdp call to get customer property");
		StatusObjDto<List<AccAccountOpeninfo>> khkhxx = fsdpBiz.khkhxx(null,null,mobile);
		logger.info("return:" + JsonUtils.object2JSON(khkhxx));
		if(khkhxx.isOk() && khkhxx.getObj().size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public AccUser getUserByShareCode(String shareCode) {
		if (ObjectUtils.isNotEmptyOrNull(shareCode)) {
			//查询邀请用户
			AccUser shareUser = userBiz.findByShareCode(shareCode);
			if(shareUser == null) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("邀请用户不存在");
			}
			
			return shareUser;
		}
		
		return null;
	}
	
	/**
	 * 抽球模型，配置大转盘奖品
	 * @param turntableeParam
	 */
	public void initPrizes(String activityCode,String prizeTypeCode) {
		
		String redisKey = activityCode + ":" + prizeTypeCode;
		boolean hasInit = ActivityRedis.ACT_ACTVITY_PRIZE.exists(redisKey);
		if(hasInit) return;
		logger.info("初始化大转盘奖品.............................");
		
		/**中奖奖品初始化**/
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(activityCode, prizeTypeCode);
		if(prizes == null) {
			logger.info("活动奖品未配置.............................");
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动奖品未配置");
		}
		if(prizes.size() == 0) {
			logger.info("无可用的活动奖品.............................");
			throw ActivityBizException.ACT_PRIZE_NOT_VALID;
		}
		
		/**奖品**/
		int total = 0;
		for(int i = 0;i < prizes.size();i++) {
			ActivityRedis.ACT_ACTVITY_PRIZE.sadd(redisKey, prizes.get(i));
			total++;
		}
		logger.info("初始化大转盘奖品完毕.............................,total"+total+",prize:"+prizes.size());
	}
	
	/**
	 * 初始化奖品到redis
	 */
	public void initPrizesToRedis() {
		HashMap<Integer, String> prizeMaps = Maps.newHashMap(PRIZE_COUPON_TEMPLATES);
		Set<Map.Entry<Integer, String>> set = prizeMaps.entrySet();
		Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) iterator.next();
			
			initPrizes(ACTIVITY_CODE, entry.getValue());
		}
	}

	@Override
	public ResultDto mobilehascustomer(AccTokenUser user, Customer customer, String mobile) {
		StatusObjDto<List<AccAccountOpeninfo>> khkhxx = fsdpBiz.khkhxx(null,null,mobile);
		Map map = Maps.newHashMap();
		map.put("info", khkhxx.getObj());
		return new ResultDto(khkhxx.getCode(),map,khkhxx.getMsg());
	}
	
}
