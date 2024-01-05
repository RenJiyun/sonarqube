package com.wlzq.activity.base.biz;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.actWL20.dto.MyPrizeDto2;
import com.wlzq.activity.base.biz.impl.CouponCommonReceiveBizImpl;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dao.ActPrizeTypeDao;
import com.wlzq.activity.base.dto.*;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.redeem.biz.RedeemBiz;
import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.activity.redeem.observers.RedeemObserver;
import com.wlzq.activity.redenvelope.biz.RedEnvelopeBiz;
import com.wlzq.activity.redenvelope.dto.RedEnvelopeDto;
import com.wlzq.activity.redenvelope.dto.RedEnvelopeNotifyDto;
import com.wlzq.activity.redenvelope.model.RedEnvelope;
import com.wlzq.activity.redenvelope.observer.RedEnvelopeObserver;
import com.wlzq.activity.task.biz.impl.FreeCourseBizImpl;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.Page;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.wlzq.activity.double11.biz.Double11Biz.ACTIVITY_2023DOUBLE11_XTJJDS;

/**
 * 活动奖品业务类
 *
 * @author
 * @version 1.0
 */
@Service
public class ActPrizeBiz extends ActivityBaseBiz implements RedeemObserver, RedEnvelopeObserver {
    @Autowired
    private ActPrizeDao prizeDao;
    @Autowired
    private ActPrizeTypeDao prizeTypeDao;
    @Autowired
    private RedeemBiz redeemBiz;
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private CouponBiz couponBiz;
    @Autowired
    CouponCommonReceiveBiz couponRecieveBiz;
    @Autowired
    private RedEnvelopeBiz redEnvelopeBiz;
    @Autowired
    private ActPrizeTypeBiz actPrizeTypeBiz;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private OrderCheckBiz orderCheckBiz;

    private Logger logger = LoggerFactory.getLogger(ActPrizeBiz.class);
    public static final String ACT_COUPON_FEEDBACK = "activity.coupon.feedback"; //优惠券回赠活动
    //	public static final String ACT_PRIZE_REPEATABLE = "activity.prize.repeatable";		// 奖品可重复设置
    public static final String ACT_COUPON_FEEDBACK_STRING = "activity.coupon.feedback.string";    //优惠券回赠活动设置
    /*奖品每日限量领取数量，配置key*/
    private static final String ACT_PRIZES_DAILY_LIMIT = "activity.prizes.daily.limit";
    private static final String DOUBLE11_INVEST_ACTIVITY = "ACTIVITY.2022DOUBLE11.VAS.COUPON";
    private static final String DOUBLE11_INVEST_COUPONS = "PRIZE.2022DOUBLE11.VAS.ZTZS,PRIZE.2022DOUBLE11.VAS.CYJJ";

    /**
     * 根据奖品编码查询可使用的奖品
     *
     * @param prizeCode
     * @return
     */
    public ActPrize findAvailablePrize(String prizeCode) {
        if (ObjectUtils.isEmptyOrNull(prizeCode)) return null;
        ActPrizeType prizeType = prizeTypeDao.findByCode(prizeCode);
        if (prizeType == null) return null;

        ActPrize prize = null;
        if (prizeType.getType().equals(ActPrizeType.TYPE_POINT)) {
            prize = new ActPrize();
            prize.setType(prizeType.getType());
            prize.setWorth(prizeType.getWorth());
        } else {
            prize = prizeDao.findAvailablePrize(prizeCode);
        }

        return prize;
    }

    /**
     * 根据奖品编码查询可使用的奖品
     *
     * @param prizeCode
     * @return
     */
    public ActPrize findPrizeByRedeem(String redeemCode) {
        if (ObjectUtils.isEmptyOrNull(redeemCode)) return null;

        return prizeDao.findByRedeem(redeemCode);
    }

    /**
     * 查询活动可使用奖品（id）
     *
     * @param activityCode
     * @return
     */
    public List<Long> findAvailablePrizes(String activityCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) return null;

        List<Long> prizes = prizeDao.findAvailablePrizes(activityCode);

        return prizes;
    }

    /**
     * 查询活动可使用奖品（id）
     *
     * @param activityCode
     * @param prizeCode
     * @return
     */
    public List<Long> findAvailablePrizes(String activityCode, String prizeTypeCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) return null;
        if (ObjectUtils.isEmptyOrNull(prizeTypeCode)) return null;
        List<Long> prizes = prizeDao.findAvailablePrizesByType(activityCode, prizeTypeCode);

        return prizes;
    }

    /**
     * 查询活动可使用奖品（id）
     *
     * @param activityCode
     * @param prizeCode
     * @return
     */
    public ActPrize findOneAvailablePrize(String activityCode, String prizeTypeCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) return null;
        if (ObjectUtils.isEmptyOrNull(prizeTypeCode)) return null;

        // if(!"ACTIVITY.OLDCUS.RETURNVISIT2022".equals(activityCode)) {
        // 	initPrizes(activityCode,prizeTypeCode);
        // }
        /* 客户回访初始化奖池的key使用 activityCode:activityCode */
        if (!activityCode.equals(prizeTypeCode)) {
            initPrizes(activityCode, prizeTypeCode);
        }

        String redisKey = activityCode + ":" + prizeTypeCode;
        String id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sRandomMember(redisKey));
        //		String id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sPop(redisKey));
        if (ObjectUtils.isEmptyOrNull(id)) {
            return null;
        }
        ActPrize prize = prizeDao.get(id);

        return prize;
    }

    public ActPrize getOneAvailablePrize(String activityCode, String prizeTypeCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) return null;
        if (ObjectUtils.isEmptyOrNull(prizeTypeCode)) return null;

        // if(!"ACTIVITY.OLDCUS.RETURNVISIT2022".equals(activityCode)) {
        // 	initPrizes(activityCode,prizeTypeCode);
        // }
        /* 客户回访初始化奖池的key使用 activityCode:activityCode */
        if (!activityCode.equals(prizeTypeCode)) {
            initPrizes(activityCode, prizeTypeCode);
        }

        String redisKey = activityCode + ":" + prizeTypeCode;
        String id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sPop(redisKey));
        if (ObjectUtils.isEmptyOrNull(id)) {
            return null;
        }
        ActPrize prize = prizeDao.get(id);

        return prize;
    }

    /**
     * 查询活动奖品
     *
     * @param id
     * @return
     */
    public ActPrize findPrize(Long id) {
        ActPrize prize = prizeDao.get(id.toString());
        return prize;
    }

    /**
     * 查询活动奖品
     *
     * @param activity
     * @param customerId
     * @param userId
     * @param type
     * @return
     */
    public List<ActPrize> findPrize(String activity, String customerId, String userId, String openId, Integer type, Integer status) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activity);
        prize.setCustomerId(customerId);
        prize.setUserId(userId);
        prize.setOpenId(openId);
        prize.setType(type);
        prize.setStatus(status);
        return prizeDao.findList(prize);
    }

    /**
     * 查询活动奖品
     *
     * @param activity
     * @param customerId
     * @param userId
     * @param type
     * @return
     */
    public List<ActPrize> findPrize(String activity, String prizeTypeCode, String customerId, String userId, String openId, Integer type, Integer status, Date updateTimeFrom, Date updateTimeTo) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activity);
        prize.setCustomerId(customerId);
        prize.setCode(prizeTypeCode);
        prize.setUserId(userId);
        prize.setOpenId(openId);
        prize.setType(type);
        prize.setStatus(status);
        prize.setUpdateTimeFrom(updateTimeFrom);
        prize.setUpdateTimeTo(updateTimeTo);
        return prizeDao.findList(prize);
    }

    /**
     * 查询活动奖品数量
     *
     * @param activity
     * @param customerId
     * @param userId
     * @param type
     * @return
     */
    public Integer findPrizeCount(String activity, String prizeTypeCode, String customerId, String userId, String openId, Integer type, Integer status, Date updateTimeFrom, Date updateTimeTo) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activity);
        prize.setCustomerId(customerId);
        prize.setCode(prizeTypeCode);
        prize.setUserId(userId);
        prize.setOpenId(openId);
        prize.setType(type);
        prize.setStatus(status);
        prize.setUpdateTimeFrom(updateTimeFrom);
        prize.setUpdateTimeTo(updateTimeTo);
        return prizeDao.findPrizeCount(prize);
    }

    public Integer findPrizeCount(String activityCode, String prizeTypeCode, Integer status, Date updateTimeFrom, Date updateTimeTo,
                                  AccTokenUser user, Customer customer) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activityCode);
        prize.setCode(prizeTypeCode);
        prize.setCustomerId(customer != null ? customer.getCustomerId() : null);
        prize.setUserId(user != null ? user.getUserId() : null);
        prize.setOpenId(user != null ? user.getOpenid() : null);
        prize.setStatus(status);
        prize.setUpdateTimeFrom(updateTimeFrom);
        prize.setUpdateTimeTo(updateTimeTo);
        return prizeDao.findPrizeCount(prize);
    }

    /**
     * 查询活动奖品
     *
     * @param activity
     * @param customerId
     * @param userId
     * @param openId
     * @param prizeTypeCode
     * @param status
     * @param remark        TODO
     * @return
     */
    public List<ActPrize> findPrize(String activity, String customerId, String userId, String openId, String prizeTypeCode, Integer status, String remark) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activity);
        prize.setCustomerId(customerId);
        prize.setUserId(userId);
        prize.setOpenId(openId);
        prize.setCode(prizeTypeCode);
        prize.setStatus(status);
        prize.setRemark(remark);
        return prizeDao.findList(prize);
    }


    /**
     * 查询活动奖品
     */
    public List<ActPrize> findPrize(AcReceivePriceVO acReceivePriceVO, String prizeTypeCode, Integer status, String remark, Boolean globalUniquePrizeType) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(acReceivePriceVO.getActivityCode());
        if (acReceivePriceVO.isCustomerDimension()) {
            prize.setCustomerId(acReceivePriceVO.getCustomerId());
        }
        if (acReceivePriceVO.isMobileDimension()) {
            prize.setMobile(acReceivePriceVO.getMobile());
        }
        prize.setCode(prizeTypeCode);
        prize.setStatus(status);
        prize.setRemark(remark);
        prize.setGlobalUniquePrizeType(globalUniquePrizeType);
        return prizeDao.findList(prize);
    }

    /**
     * 获取用户在活动中领取的奖品
     */
    public List<ActPrize> getUserPrizeList(String activityCode, String prizeTypeCode,
                                           AccTokenUser user, Customer customer) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activityCode);
        prize.setUserId(user.getUserId());
        prize.setMobile(user.getMobile());
        prize.setCustomerId(customer != null ? customer.getCustomerId() : null);
        prize.setCode(prizeTypeCode);
        return prizeDao.getUserPrizeList(prize);
    }

    public List<ActPrize> queryPrize(String activityCode, String customerId, String userId, String mobile, String prizeTypeCode, Integer uniqueType) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activityCode);
        prize.setCustomerId(customerId);
        prize.setUserId(userId);
        prize.setMobile(mobile);
        prize.setCode(prizeTypeCode);
        prize.setUniqueType(uniqueType);
        return prizeDao.findList(prize);
    }


    /**
     * 查询用户/客户活动奖品
     *
     * @param customerId
     * @param userId
     * @return
     */
    public List<ActPrize> findUserPrizes(List<String> activities, String customerId, String userId) {
        ActPrize prize = new ActPrize();
        prize.setActivityCodes(activities);
        prize.setCustomerId(customerId);
        prize.setUserId(userId);
        prize.setUnionOccupy(CodeConstant.CODE_NO);
        return prizeDao.findPrizes(prize);
    }

    /**
     * 查询用户/客户活动奖品
     *
     * @param customerId
     * @param userId
     * @return
     */
    public List<ActPrize> findUserPrizesUnionOccupy(List<String> activities, String customerId, String userId, Integer unionOccupy) {
        ActPrize prize = new ActPrize();
        prize.setActivityCodes(activities);
        prize.setCustomerId(customerId);
        prize.setUserId(userId);
        prize.setUnionOccupy(unionOccupy);
        return prizeDao.findPrizes(prize);
    }

    public List<ActPrize> findCustomerPrizes(List<String> activities, String customerId) {
        ActPrize prize = new ActPrize();
        prize.setActivityCodes(activities);
        prize.setCustomerId(customerId);
        return prizeDao.findCustomerPrizes(prize);
    }

    /**
     * 查询用户活动奖品
     *
     * @param activitiy
     * @param userId
     * @return
     */
    public List<ActPrize> findUserPrizes(String activitiy, String userId, List<String> prizeTypeCodes, Integer status) {
        ActPrize prize = new ActPrize();
        prize.setActivityCode(activitiy);
        prize.setUserId(userId);
        prize.setCodes(prizeTypeCodes);
        prize.setStatus(status);
        return prizeDao.findUserPrizes(prize);
    }


    /**
     * 更新奖品状态
     *
     * @param id
     * @param status
     */
    public void updatePrize(String userId, Long id, Integer status) {
        if (ObjectUtils.isEmptyOrNull(id) || ObjectUtils.isEmptyOrNull(status)) {
            return;
        }
        if (status < ActPrize.STATUS_NOT_SEND || status > ActPrize.STATUS_OCCUPY) return;

        ActPrize prize = prizeDao.get(id.toString());
        prize.setUserId(userId);
        prize.setStatus(status);
        prize.setUpdateTime(new Date());
        prizeDao.update(prize);
        //若为兑换码，更新兑换码发出时间
        //		if(prize.getType().equals(ActPrize.TYPE_REDEEM) && status.equals(ActPrize.STATUS_SEND)) {
        //			redeemBiz.out(prize.getRedeemCode());
        //		}
    }

    /**
     * 更新奖品状态
     *
     * @param id
     * @param status
     */
    public void updatePrize(String userId, String openId, Long id, Integer status) {
        if (ObjectUtils.isEmptyOrNull(id) || ObjectUtils.isEmptyOrNull(status)) {
            return;
        }
        if (status < ActPrize.STATUS_NOT_SEND || status > ActPrize.STATUS_OCCUPY) return;

        ActPrize prize = prizeDao.get(id.toString());
        if (prize == null) {
            return;
        }
        prize.setUserId(userId);
        prize.setOpenId(openId);
        prize.setStatus(status);
        prize.setUpdateTime(new Date());
        prizeDao.update(prize);
        //若为兑换码，更新兑换码发出时间
        //		if(prize.getType().equals(ActPrize.TYPE_REDEEM) && status.equals(ActPrize.STATUS_SEND)) {
        //			redeemBiz.out(prize.getRedeemCode());
        //		}
    }

    /**
     * 更新奖品状态
     *
     * @param userId
     * @param openId
     * @param customerId
     * @param id
     * @param status
     * @param mobile     TODO
     * @param remark     TODO
     */
    public void updatePrize(String userId, String openId, String customerId, Long id, Integer status, String mobile, String remark) {
        if (ObjectUtils.isEmptyOrNull(id) || ObjectUtils.isEmptyOrNull(status)) {
            return;
        }
        if (status < ActPrize.STATUS_NOT_SEND || status > ActPrize.STATUS_OCCUPY) return;

        ActPrize prize = prizeDao.get(id.toString());
        prize.setUserId(userId);
        prize.setOpenId(openId);
        prize.setStatus(status);
        prize.setCustomerId(customerId);
        Date now = new Date();
        prize.setUpdateTime(now);
        prize.setReceiveTime(now);
        prize.setMobile(mobile);
        prize.setRemark(remark);
        prize.setActivityGroupCode("");
        prizeDao.update(prize);
    }

    /**
     * 兑换码领取成功通知
     *
     * @param redeemCode
     * @return
     */
    @Override
    public StatusDto notify(String userId, String redeemCode) {
        ActPrize prize = prizeDao.findByRedeem(redeemCode);
        if (prize == null) {
            return new StatusDto(false, StatusDto.FAIL_COMMON);
        }
        //		prize.setUserId(userId);
        prize.setStatus(ActPrize.STATUS_USED);
        prize.setUpdateTime(new Date());
        prizeDao.update(prize);
        return new StatusDto(true, StatusDto.SUCCESS);
    }

    @Transactional
    public ActPrize giveOutPrize(String activityCode, String remark, Long prizeId, String prizeTypeCode, String userId, String openId, String customerId, String mobile) {
        ActPrize prize = null;
        if (prizeId != null) {
            prize = prizeDao.get(String.valueOf(prizeId));
        } else {
            prize = getOneAvailablePrize(activityCode, prizeTypeCode);
        }
        if (prize == null) {
            throw ActivityBizException.ACT_EMPTY_PRIZE;
        }
        //更新奖品状态
        Integer status = ActPrize.STATUS_SEND;
        //若为优惠券，更新优惠券状态
        if (isCoupon(prize.getType())) {
            String redeemCode = prize.getRedeemCode();
            Map<String, Object> busparams = Maps.newHashMap();
            busparams.put("userId", userId);
            busparams.put("code", redeemCode);
            //优惠券只能发客户，若为空，则先占用
            if (ActPrize.TYPE_COUPON.equals(prize.getType()) && ObjectUtils.isEmptyOrNull(customerId)) {
                status = ActPrize.STATUS_OCCUPY;
                RemoteUtils.call("base.couponcooperation.occupycoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
            } else {
                busparams.put("customerId", customerId);
                ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
                //调用失败
                if (result.getCode() != 0) {
                    /**处理无效券问题，更新奖品***/
                    String msg = result.getMsg();
                    if ("无效优惠券".equals(msg)) {
                        //						Map<String, Object> couponParam = Maps.newHashMap();
                        //						couponParam.put("code", redeemCode);
                        //						ResultDto couponDto = RemoteUtils.call("base.couponcooperation.findcouponbycode", ApiServiceTypeEnum.COOPERATION, couponParam, true);
                        //						logger.info("couponDto:,{}", BeanUtils.beanToMap(couponDto.getData()));
                        //						if (couponDto.getCode().equals(ResultDto.SUCCESS) && ObjectUtils.isNotEmptyOrNull(couponDto.getData())) {
                        //							String couponUserId = String.valueOf(couponDto.getData().get("userid"));
                        //							String couponCustomerId = String.valueOf(couponDto.getData().get("customerId"));
                        //							Integer couponStatus = ActPrize.STATUS_SEND;
                        //							updatePrize(couponUserId, "", couponCustomerId, prize.getId(), couponStatus, "", "");
                        //						}
                        updUnvailablePrice(redeemCode, prize.getId());
                        return null;
                    }
                    throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
                }
            }
        }
        //若为兑换码，更新兑换码发出时间
        //		if(prize.getType().equals(ActPrize.TYPE_REDEEM)) {
        //			redeemBiz.out(prize.getRedeemCode());
        //		}
        if (prize.getType().equals(ActPrize.TYPE_REDENVELOPE)) {

            RedEnvelope redEnvelope = new RedEnvelope();
            redEnvelope.setUserId(userId);
            redEnvelope.setOpenId(openId);
            redEnvelope.setBusinessNo(prize.getId().toString());
            redEnvelope.setBusinessCode(activityCode);
            redEnvelope.setAmount(prize.getRedEnvelopeAmt());
            redEnvelope.setNotifyUrl(new String("Hello, red envelope"));
            StatusObjDto<RedEnvelopeDto> result = redEnvelopeBiz.create(redEnvelope);
            if (!result.isOk()) {
                throw BizException.NETWORK_ERROR;
            }
            prize.setRedEnvelopeUrl(result.getObj().getRecieveUrl());
            status = ActPrize.STATUS_NOT_SEND;
        }
        prize.setStatus(status);
        updatePrize(userId, openId, customerId, prize.getId(), status, mobile, remark);
        return prize;
    }


    @Transactional
    public ActPrize giveOutPrizeNew(String activityCode, String remark, Long prizeId, String prizeTypeCode,
                                    String userId, String openId, String customerId, String mobile) {
        ActPrize prize = null;
        if (prizeId != null) {
            prize = prizeDao.get(String.valueOf(prizeId));
        } else {
            prize = getOneAvailablePrize(activityCode, prizeTypeCode);
        }

        if (prize == null) {
            throw ActivityBizException.ACT_EMPTY_PRIZE;
        }
        //更新奖品状态
        Integer status = ActPrize.STATUS_SEND;
        String redeemCode = prize.getRedeemCode();
        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("userId", userId);
        busparams.put("code", redeemCode);
        //优惠券只能发客户，若为空，则先占用
        if (ActPrize.TYPE_COUPON.equals(prize.getType()) && ObjectUtils.isEmptyOrNull(customerId)) {
            status = ActPrize.STATUS_OCCUPY;
            RemoteUtils.call("base.couponcooperation.occupycoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
        } else {
            busparams.put("customerId", customerId);
            ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
            //调用失败
            if (result.getCode() != 0) {
                /**处理无效券问题，更新奖品***/
                String msg = result.getMsg();
                if ("无效优惠券".equals(msg)) {
                    updUnvailablePrice(redeemCode, prize.getId());
                    return null;
                }
                throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
            }
        }
        if (prize.getType().equals(ActPrize.TYPE_REDENVELOPE)) {

            RedEnvelope redEnvelope = new RedEnvelope();
            redEnvelope.setUserId(userId);
            redEnvelope.setOpenId(openId);
            redEnvelope.setBusinessNo(prize.getId().toString());
            redEnvelope.setBusinessCode(activityCode);
            redEnvelope.setAmount(prize.getRedEnvelopeAmt());
            redEnvelope.setNotifyUrl("Hello, red envelope");
            StatusObjDto<RedEnvelopeDto> result = redEnvelopeBiz.create(redEnvelope);
            if (!result.isOk()) {
                throw BizException.NETWORK_ERROR;
            }
            prize.setRedEnvelopeUrl(result.getObj().getRecieveUrl());
            status = ActPrize.STATUS_NOT_SEND;
        }
        prize.setStatus(status);
        updatePrize(userId, openId, customerId, prize.getId(), status, mobile, remark);
        return prize;
    }


    @Transactional
    public ActPrize giveOutPrize(String activityCode, ActPrize prize, AccTokenUser user, Customer customer, String remark) {

        // todo
        // 以下代码需要根据实际情况进行修改
        if (ActPrize.TYPE_COUPON.equals(prize.getType())) {
            // 如果奖品类型是优惠券, 则需要调用优惠券系统相关接口, 以及需要区分当前是否登录了客户号
        } else if (ActPrize.TYPE_REDENVELOPE.equals(prize.getType())) {
        } else {
        }

        prize.setUserId(user.getUserId())
                .setOpenId(user.getOpenid())
                .setCustomerId(customer != null ? customer.getCustomerId() : null)
                .setStatus(ActPrize.STATUS_SEND)
                .setUpdateTime(new Date())
                .setMobile(user.getMobile())
                .setRemark(remark)
                .setReceiveTime(new Date());
        prizeDao.update(prize);
        return prize;
    }


    /**
     * 发放优惠券
     */
    @Transactional
    public ActPrize giveOutCoupon(Activity activity, String prizeTypeCode, AccTokenUser user, Customer customer, String remark) {
        ActPrizeType prizeType = actPrizeTypeBiz.getPrizeType(prizeTypeCode);
        if (prizeType == null) {
            throw ActivityBizException.ACT_PRIZE_TYPE_NOT_EXIST;
        }

        // 该方法内部会初始化奖品池
        ActPrize prize = actPrizeBiz.getOneAvailablePrize(activity.getCode(), prizeType.getCode());
        // actPrizeTypeBiz.initPrizes(activity.getCode(), prizeType);
        // ActPrize prize = actPrizeTypeBiz.getOneAvailablePrize(activity.getCode(), prizeType);
        if (prize == null) {
            throw ActivityBizException.ACT_EMPTY_PRIZE;
        }

        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("userId", user.getUserId())
                .put("code", prize.getRedeemCode())
                .put("customerId", customer != null ? customer.getCustomerId() : "")
                .put("mobile", user.getMobile())
                .put("activityCode", activity.getCode())
                .build();
        ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, params, true);

        if (result.getCode() != 0) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
        }

        String groupCode = ObjectUtils.isEmptyOrNull(activity.getGroupCode())
                ? "" : activity.getGroupCode();
        prize.setActivityGroupCode(groupCode);
        prize.setStatus(ActPrize.STATUS_SEND);
        prize.setUserId(user.getUserId());
        prize.setOpenId(user.getOpenid());
        prize.setCustomerId(customer != null ? customer.getCustomerId() : null);
        prize.setMobile(user.getMobile());
        prize.setRemark(remark);
        prize.setUpdateTime(new Date());
        // 设置领取时间
        prize.setReceiveTime(new Date());
        prizeDao.update(prize);

        return prize;
    }


    @Transactional
    public ActPrize giveOutPrize(AcReceivePriceVO acReceivePriceVO, Long prizeId, String mobile) {
        ActPrize prize;
        String userId = acReceivePriceVO.getUserId();
        String customerId = acReceivePriceVO.getCustomerId();
        if (prizeId != null) {
            prize = prizeDao.get(String.valueOf(prizeId));
        } else {
            prize = getOneAvailablePrize(acReceivePriceVO.getActivityCode(), acReceivePriceVO.getPrizeType());
        }
        if (prize == null) {
            throw ActivityBizException.ACT_EMPTY_PRIZE;
        }
        //更新奖品状态
        Integer status = ActPrize.STATUS_SEND;

        String redeemCode = prize.getRedeemCode();
        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("userId", userId);
        busparams.put("code", redeemCode);
        busparams.put("recommendCode", acReceivePriceVO.getRecommendCode());
        Integer prizeAmount = acReceivePriceVO.getPrizeAmount();
        if (prizeAmount != null) {
            busparams.put("couponAmount", prizeAmount);
        }

        Integer noNeedCustomerLogin = acReceivePriceVO.getNoNeedCustomerLogin();

        //优惠券只能发客户，若为空，则先占用
        if (ActPrize.TYPE_COUPON.equals(prize.getType()) && ObjectUtils.isEmptyOrNull(customerId) && !Objects.equals(noNeedCustomerLogin, CodeConstant.CODE_YES)) {
            status = ActPrize.STATUS_OCCUPY;
            ResultDto result = RemoteUtils.call("base.couponcooperation.occupycoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
            //调用失败
            if (result.getCode() != 0) {
                logger.error("receivecoupon error: " + result.getMsg() + "|prizeId:" + prize.getId());
                throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
            }
        } else {
            if (ObjectUtils.isEmptyOrNull(customerId) && !isCoupon2(prize.getType()) && !Objects.equals(noNeedCustomerLogin, CodeConstant.CODE_YES)) {
                status = ActPrize.STATUS_OCCUPY;
            }
            if (ObjectUtils.isNotEmptyOrNull(customerId)) {
                busparams.put("customerId", customerId);
            }
            busparams.put("noNeedCustomerLogin", noNeedCustomerLogin);
            busparams.put("mobile", mobile);
            busparams.put("activityCode", acReceivePriceVO.getActivityCode());
            String onceDaily = AppConfigUtils.get("act.receive.once.daily");
            if (StringUtils.isNotBlank(onceDaily)) {
                String[] split = onceDaily.split(",");
                if (Arrays.stream(split).allMatch(e -> StringUtils.equals(e, acReceivePriceVO.getActivityCode()))) {
                    busparams.put("redeemRestrict", "ND1");
                }
            }

            ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
            //调用失败
            if (result.getCode() != 0) {
                logger.error("receivecoupon error: " + result.getMsg() + "|prizeId:" + prize.getId());

                ActPrizeType actPrizeType = findPrizeType(prize.getCode());
                if (actPrizeType.getDailyLimit() != null && result.getCode() == 2010001) {
                    /*每日限量领取出现异常后，再把奖品补回缓存*/
                    String redisKey = prize.getActivityCode() + ":" + prize.getCode();
                    ActivityRedis.ACT_ACTVITY_PRIZE.sadd(redisKey, prize.getId());
                }
                /**处理无效券问题，更新奖品***/
                String msg = result.getMsg();
                if ("无效优惠券".equals(msg)) {
                    updUnvailablePrice(redeemCode, prize.getId());
                    return null;
                }
                throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
            }

            MyPrizeDto2 dto = BeanUtils.mapToBean(result.getData(), MyPrizeDto2.class);
            if (ObjectUtils.isEmptyOrNull(prize.getRedEnvelopeAmt())) {
                prize.setRedEnvelopeAmt(dto.getRedenvelopeAmt());
            }
        }

        //更新 ActPrize
        prize.setStatus(status);
        updatePrize(userId, acReceivePriceVO.getOpenId(), customerId, prize.getId(), status, mobile, acReceivePriceVO.getRemark());
        return prize;
    }

    private void updUnvailablePrice(String redeemCode, Long priceId) {
        Map<String, Object> couponParam = Maps.newHashMap();
        couponParam.put("code", redeemCode);
        ResultDto couponDto = RemoteUtils.call("base.couponcooperation.findcouponbycode", ApiServiceTypeEnum.COOPERATION, couponParam, true);
        logger.info("couponDto:,{}", BeanUtils.beanToMap(couponDto.getData()));
        if (couponDto.getCode().equals(ResultDto.SUCCESS) && ObjectUtils.isNotEmptyOrNull(couponDto.getData())) {
            String couponUserId = String.valueOf(couponDto.getData().get("userid"));
            String couponCustomerId = String.valueOf(couponDto.getData().get("customerId"));
            Integer couponStatus = ActPrize.STATUS_SEND;
            updatePrize(couponUserId, "", couponCustomerId, priceId, couponStatus, "", "");
        }
    }

    public StatusObjDto<List<MyPrizeDto>> findPrizes(String activityCodes, String customerId, String userId, String openId, Page page) {
        //参数检查
        if (ObjectUtils.isEmptyOrNull(activityCodes)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCodes");
        }
        if (ObjectUtils.isEmptyOrNull(customerId) && ObjectUtils.isEmptyOrNull(userId)) {
            return new StatusObjDto<List<MyPrizeDto>>(true, Lists.newArrayList(), StatusDto.SUCCESS, "");
        }

        List<ActPrize> myPrizes = actPrizeBiz.findUserPrizesUnionOccupy(Lists.newArrayList(activityCodes.split(",")), customerId, userId, new Integer(1));
        List<MyPrizeDto> prizes = Lists.newArrayList();
        for (ActPrize myPrize : myPrizes) {
            MyPrizeDto prizeDto = getMyPrizeDto(myPrize);
            if (prizeDto != null) {
                prizes.add(prizeDto);
            }
        }

        return new StatusObjDto<List<MyPrizeDto>>(true, prizes, StatusDto.SUCCESS, "");
    }

    private MyPrizeDto getMyPrizeDto(ActPrize myPrize) {
        Integer type = myPrize.getType();
        MyPrizeDto prize = new MyPrizeDto();
        prize.setId(myPrize.getId());
        prize.setType(type);
        prize.setName(myPrize.getName());
        prize.setCode(myPrize.getRedeemCode());
        prize.setRecieveTime(myPrize.getUpdateTime());
        prize.setCardNo(myPrize.getCardNo());
        prize.setWorth(myPrize.getWorth());
        prize.setTime(myPrize.getTime());
        prize.setStatus(myPrize.getStatus());
        prize.setCardPassword(myPrize.getCardPassword());
        if (prizeIsRedeem(type)) { //有效期获取
            StatusObjDto<Redeem> redeemStatus = redeemBiz.findRedeemByCode(myPrize.getRedeemCode());
            if (!redeemStatus.getCode().equals(StatusDto.SUCCESS)) return null;
            Redeem redeem = redeemStatus.getObj();
            if (redeem.getValidityType().equals(1)) { //时间范围类型兑换码
                prize.setValidityDateFrom(redeem.getValidityDateFrom());
                prize.setValidityDateTo(DateUtils.getDayEnd(redeem.getValidityDateTo()));
            } else {
                prize.setValidityDateFrom(redeem.getOutTime());
                prize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(redeem.getOutTime(), redeem.getValidityDay())));
            }
            Integer status = myPrize.getStatus();
            Date now = new Date();
            if (status.equals(ActPrize.STATUS_SEND) && now.getTime() > prize.getValidityDateTo().getTime()) {
                status = MyPrizeDto.STATUS_EXPIRE;
            }
            prize.setDescription(" Level-2增强行情 " + redeem.getGoodsTime() + "个月");
            prize.setRecieveTime(redeem.getOutTime());
            prize.setStatus(status);
        } else if (prizeIsCoupon(type)) { //有效期获取
            StatusObjDto<CouponInfo> couponStatus = couponBiz.couponInfo(null, myPrize.getRedeemCode());
            if (!couponStatus.getCode().equals(StatusDto.SUCCESS)) {
                return null;
            }
            CouponInfo coupon = couponStatus.getObj();
            if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS)) {
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
            Integer status = myPrize.getStatus().equals(ActPrize.STATUS_OCCUPY) ? ActPrize.STATUS_SEND : coupon.getStatus();
            prize.setStatus(status);
            prize.setCouponType(coupon.getType());
            prize.setAmountSatisfy(coupon.getAmountSatisfy());
            prize.setAmountReducation(coupon.getAmountReducation());
        }
        //设置当前是否可用
        Integer isCurrentEnable = CodeConstant.CODE_NO;
        if (prize.getValidityDateFrom() != null && prize.getValidityDateTo() != null) {
            Date now = new Date();
            isCurrentEnable = now.getTime() > prize.getValidityDateFrom().getTime() && now.getTime() < prize.getValidityDateTo().getTime() ?
                    CodeConstant.CODE_YES : CodeConstant.CODE_NO;
        }
        prize.setIsCurrentEnable(isCurrentEnable);
        return prize;
    }

    /**
     * 是否为对换码
     */
    private boolean prizeIsRedeem(Integer type) {
        return type == 6;
    }

    /**
     * 是否为优惠券
     */
    private boolean prizeIsCoupon(Integer type) {
		/*
		 1:折扣券 2:免单券 3:代金券 4:权益券 5:额度券 6:Level-2 7:京东卡 9:红包
		 10:融券费率 13:折扣券（增值服务）14:免单券（增值服务）15:实物奖品 16:代金券（增值服务）
		*/
        return type == 1 || type == 2 || type == 3 || type == 4 || type == 13 || type == 14 || type == 16;
    }

    public StatusObjDto<List<ShowPrizeDto>> prizes(String activityCode, Page page) {
        //参数检查
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        List<ShowPrizeDto> prizes = prizeDao.findAllReceivedPrizes(activityCode, page.getStart(), page.getEnd());

        if (ActivityBiz.ACTIVITY_GGQZD.equals(activityCode)) {
            String[] specialPrizeTypes = {"PRIZE.GGQZD.IQIYI", "PRIZE.GGQZD.RED.PACKET.888"};
            List<ShowPrizeDto> specialPrizes = prizeDao.findSpecialPrizes(activityCode, specialPrizeTypes);
            if (specialPrizes == null) {
                specialPrizes = Lists.newArrayList();
            }
            specialPrizes.addAll(prizes);
            prizes = specialPrizes.subList(0, Math.min(specialPrizes.size(), page.getPageSize()));
        }
        return new StatusObjDto<List<ShowPrizeDto>>(true, prizes, StatusDto.SUCCESS, "");
    }

    public StatusObjDto<List<MyPrizeDto>> findCustomerPrizes(String activityCodes, String customerId, Page page) {
        //参数检查
        if (ObjectUtils.isEmptyOrNull(activityCodes)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCodes");
        }

        List<ActPrize> myPrizes = actPrizeBiz.findCustomerPrizes(Lists.newArrayList(activityCodes.split(",")), customerId);
        List<MyPrizeDto> prizes = Lists.newArrayList();
        for (ActPrize myPrize : myPrizes) {
            Integer type = myPrize.getType();
            MyPrizeDto prize = new MyPrizeDto();
            prize.setId(myPrize.getId());
            prize.setType(type);
            prize.setName(myPrize.getName());
            prize.setCode(myPrize.getRedeemCode());
            prize.setRecieveTime(myPrize.getUpdateTime());
            prize.setCardNo(myPrize.getCardNo());
            prize.setWorth(myPrize.getWorth());
            prize.setTime(myPrize.getTime());
            prize.setCardPassword(myPrize.getCardPassword());
            if (type.equals(ActPrize.TYPE_REDEEM)) { //有效期获取
                StatusObjDto<Redeem> redeemStatus = redeemBiz.findRedeemByCode(myPrize.getRedeemCode());
                if (!redeemStatus.getCode().equals(StatusDto.SUCCESS)) continue;
                Redeem redeem = redeemStatus.getObj();
                if (redeem.getValidityType().equals(1)) { //时间范围类型兑换码
                    prize.setValidityDateFrom(redeem.getValidityDateFrom());
                    prize.setValidityDateTo(DateUtils.getDayEnd(redeem.getValidityDateTo()));
                } else {
                    prize.setValidityDateFrom(redeem.getOutTime());
                    prize.setValidityDateTo(DateUtils.getDayEnd(DateUtils.addDay(redeem.getOutTime(), redeem.getValidityDay())));
                }
                Integer status = myPrize.getStatus();
                Date now = new Date();
                if (status.equals(ActPrize.STATUS_SEND) && now.getTime() > prize.getValidityDateTo().getTime()) {
                    status = MyPrizeDto.STATUS_EXPIRE;
                }
                prize.setDescription(" Level-2增强行情 " + redeem.getGoodsTime() + "个月");
                prize.setRecieveTime(redeem.getOutTime());
                prize.setStatus(status);
            } else if (type.equals(ActPrize.TYPE_COUPON)) { //有效期获取
                StatusObjDto<CouponInfo> couponStatus = couponBiz.couponInfo(null, myPrize.getRedeemCode());
                if (!couponStatus.getCode().equals(StatusDto.SUCCESS)) continue;
                CouponInfo coupon = couponStatus.getObj();
                if (coupon.getValidityType().equals(CouponInfo.VALIDITY_TYPE_DAYS)) {
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
            }
            prizes.add(prize);
        }

        return new StatusObjDto<List<MyPrizeDto>>(true, prizes, StatusDto.SUCCESS, "");
    }

    @Override
    public void notify(RedEnvelopeNotifyDto notifyDto) {
        try {
            ActPrize prize = prizeDao.get(notifyDto.getBusinessNo());
            if (prize != null) {
                prize.setStatus(ActPrize.STATUS_SEND);
                prize.setUpdateTime(new Date());
                prizeDao.update(prize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public StatusObjDto<List<PrizeCustomerDto>> customerPrizeList(String activityCode, Integer start, Integer end) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("acitivityCode");
        }
        List<PrizeCustomerDto> customerPrizeList = prizeDao.findCustomerPrizeList(activityCode, start, end);
        List<PrizeCustomerDto> result = customerPrizeList.stream().map(PrizeCustomerDto::replaceStar).collect(Collectors.toList());
        return new StatusObjDto<List<PrizeCustomerDto>>(true, result, 0, "");
    }

    /**
     * 查询类型不重复的奖品(各取一个)
     *
     * @param activityCode 活动编码
     * @param type         奖品类型
     * @return
     */
    public List<ActPrize> findDistinctPrizes(String activityCode, Integer type) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("acitivityCode");
        }
        return prizeDao.findDistinctPrizes(activityCode, type);
    }

    /**
     * 领取用户号占领的奖品到客户号下
     *
     * @param activityCodes
     * @param userId
     * @param openId
     * @param customerId
     * @param repeatable
     * @return
     */
    @Transactional
    public List<ActPrize> receiveOccupydPrize(String activityCodes, String userId, String openId, String customerId, String mobile, Integer repeatable) {
        List<String> codelist = Lists.newArrayList();
        if (ObjectUtils.isNotEmptyOrNull(activityCodes)) {
            codelist = Lists.newArrayList(activityCodes.trim().split(","));
        }
        ActPrize entity = new ActPrize();
        entity.setActivityCodes(codelist);
        entity.setUserId(userId);
        entity.setStatus(ActPrize.STATUS_OCCUPY);
        List<ActPrize> list = prizeDao.findList(entity);

        Iterator<ActPrize> iterator = list.iterator();
        while (iterator.hasNext()) {
            /**不在活动期**/
            if (!isValid(iterator.next().getActivityCode()).isOk()) {
                iterator.remove();
            }
        }

        Iterator<ActPrize> it = list.iterator();
        logger.info("prizesSize:{}", list.size());
        while (it.hasNext()) {
            ActPrize prize = it.next();
            if (isCoupon2(prize.getType())) {
                it.remove();
                continue;
            }

            prize.setStatus(ActPrize.STATUS_SEND);
            prize.setCustomerId(customerId);
            String redeemCode = prize.getRedeemCode();
            Map<String, Object> busparams = Maps.newHashMap();
            busparams.put("userId", userId);
            busparams.put("code", redeemCode);
            busparams.put("customerId", customerId);
            ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
            if (result.getCode() != 0) {
                throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
            }
            actPrizeBiz.updatePrize(userId, openId, customerId, prize.getId(), ActPrize.STATUS_SEND, mobile, null);
        }
        return list;
    }

    /**
     * 判断是否同类活动奖品
     *
     * @param list
     * @param prize
     * @return
     */
    private static boolean containsPrize(List<ActPrize> list, ActPrize prize) {
        if (list == null || list.size() == 0 || prize == null) {
            return false;
        }
        for (ActPrize each : list) {
            if (each.getActivityCode().equals(prize.getActivityCode()) && each.getType().equals(prize.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成奖品
     *
     * @param prizeMap
     * @return
     */
    @Transactional
    public StatusDto createPrize(List<Map<String, Object>> prizeMap) {
        List<ActPrize> prizes = Lists.newArrayList();
        int commitCount = 0;
        int i = 0;
        Date now = new Date();
        for (Map<String, Object> map : prizeMap) {
            ActPrize one = BeanUtils.mapToBean(map, ActPrize.class);
            if (one.getCreateTime() == null) {
                one.setCreateTime(now);
            }
            prizes.add(one);
            /**每200条插入记录**/
            if (i % 200 == 199) {
                prizeDao.insertBatch(prizes.subList(commitCount * 200, (commitCount + 1) * 200));
                commitCount++;
            }
            i++;
        }
        if (!prizes.isEmpty() && prizes.size() % 200 > 0) {
            prizeDao.insertBatch(prizes.subList(commitCount * 200, prizes.size()));
        }
        String msg = "成功生成" + prizes.size() + "条奖品记录";
        return new StatusDto(true, StatusDto.SUCCESS, msg);
    }

    /**
     * 购买投顾产品回馈优惠券接口
     *
     * @param plate
     * @param activityCode
     * @param productCode
     * @param customerId   TODO
     * @param outTradeNo   TODO
     * @return
     */
    @Transactional
    public StatusObjDto<List<MyPrizeDto>> couponFeedback(Integer plate, String activityCode, String productCode, String customerId, String outTradeNo) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(productCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("productCode");
        }
        StatusDto isValidAct = isValid(activityCode);
        if (!isValidAct.isOk()) {
            return new StatusObjDto<>(true, Lists.newArrayList(), StatusDto.SUCCESS, "");
        }
        List<ActPrize> prizes = findDistinctPrizes(activityCode, ActPrize.TYPE_COUPON);
        List<ActPrize> prizeList = Lists.newArrayList();
        for (ActPrize prize : prizes) {
            if (!prize.getType().equals(ActPrize.TYPE_COUPON)) continue;
            StatusObjDto<CouponInfo> applicableStatus = couponBiz.checkIsApplicable(null, prize.getRedeemCode(), plate, productCode, null);
            if (applicableStatus.isOk()) {
                prizeList.add(prize);
            }
        }
        List<MyPrizeDto> feedBacks = Lists.newArrayList();
        for (ActPrize actPrize : prizeList) {
            List<ActPrize> hasPrizes = findPrize(activityCode, customerId, null, null, actPrize.getCode(), null, outTradeNo);
            /**已经有优惠券，返回信息**/
            if (!hasPrizes.isEmpty()) {
                List<MyPrizeDto> dtoList = hasPrizes.stream().map(e -> getMyPrizeDto(e)).collect(Collectors.toList());
                feedBacks.addAll(dtoList);
            } else {
                /**没有优惠券，发放优惠券**/
                ActPrize one = getOneAvailablePrize(activityCode, actPrize.getCode());
                if (one != null) {
                    ActPrize prize = giveOutPrize(activityCode, outTradeNo, one.getId(), one.getCode(), null, null, customerId, null);
                    if (prize == null) {
                        throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
                    }
                    MyPrizeDto prizeDto = getMyPrizeDto(prize);
                    feedBacks.add(prizeDto);
                }
            }
        }
        return new StatusObjDto<>(true, feedBacks, StatusDto.SUCCESS, "");
    }

    public StatusObjDto<List<MyPrizeDto>> getPrizeByOrderId(String outTradeNo, String customerId, String userId, String mobile) {
        if (ObjectUtils.isEmptyOrNull(outTradeNo)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("outTradeNo");
        }

        StatusDto isValidAct = isValid(DOUBLE11_INVEST_ACTIVITY);
        /*双11活动期间买投顾产品送决策工具代金券*/
        if (isValidAct.isOk()) {
            return receiveOrderId(DOUBLE11_INVEST_ACTIVITY, DOUBLE11_INVEST_COUPONS, outTradeNo, customerId, userId, mobile);
        }

        ActPrize entity = new ActPrize();
        entity.setRemark(outTradeNo);
        entity.setCustomerId(customerId);
        List<ActPrize> list = prizeDao.findList(entity);
        List<MyPrizeDto> dtoList = Lists.newArrayList();

        /**若无奖品，查询该订单是否能回馈奖品**/
        if (list.isEmpty()) {
            Date now = new Date();
            String activityCode = AppConfigUtils.get(ACT_COUPON_FEEDBACK);
            Activity act = findActivity(activityCode);

            /**不在活动时间，直接返回**/
            if (ObjectUtils.isEmptyOrNull(activityCode) || act == null || now.after(act.getDateTo()) || now.before(act.getDateFrom())) {
                return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
            }

            /**查询订单信息**/
            Map<String, Object> orderParam = Maps.newHashMap();
            orderParam.put("customerId", customerId);
            orderParam.put("outTradeNo", outTradeNo);
            ResultDto orderDto = RemoteUtils.call("service.productcooperation.getorder", ApiServiceTypeEnum.COOPERATION, orderParam, true);
            logger.info("get order return : " + orderDto.getData());
            /**订单信息有误，直接返回**/
            if (orderDto == null || ResultDto.FAIL_COMMON.equals(orderDto.getCode()) || null == orderDto.getData()) {
                return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
            }
            Map<String, Object> orderMap = orderDto.getData();
            Integer orderTime = (Integer) orderMap.get("time");
            Integer orderTimeUnit = (Integer) orderMap.get("timeUnit");
            String productCode = (String) orderMap.get("productCode");
            Integer status = (Integer) orderMap.get("status");
            Date createTime = new Date(Long.valueOf(String.valueOf(orderMap.get("createTime"))));
            Integer totalFee = (Integer) orderMap.get("totalFee");
            /**实付金额为0，不获得奖品**/
            if (ObjectUtils.isEmptyOrNull(totalFee) || totalFee == 0) {
                return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
            }
            /**订单未支付**/
            if (!CodeConstant.CODE_YES.equals(status)) {
                return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
            }
            /**订单创建时间不在活动时间之内**/
            if (createTime == null || createTime.after(act.getDateTo()) || createTime.before(act.getDateFrom())) {
                return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
            }
            /**中台配置回馈活动设置，格式为：产品代码-规格:奖品编码字符串 **/
            String feedbackStr = AppConfigUtils.get(ACT_COUPON_FEEDBACK_STRING);
            @SuppressWarnings("unchecked")
            Map<String, Object> settingMap = JsonUtils.json2Map(feedbackStr);
            String key = productCode + "-" + orderTime + "-" + orderTimeUnit;
            Set<String> keySet = settingMap.keySet();
            if (keySet.contains(key)) {
                String prizeTypes = String.valueOf(settingMap.get(key));
                List<String> prizeTypeList = Lists.newArrayList(prizeTypes.split(","));
                for (String prizeType : prizeTypeList) {
                    //以前领取过7天体验券的不能再领取
                    if (FreeCourseBizImpl.ACTIVITY_CODE.equals(activityCode)
                            && FreeCourseBizImpl.PRIZE_TYPE_CODE_CYJJ_FREE_7DAY.equals(prizeType)) {
                        List<String> beforePrizeTypes = new ArrayList<>();
                        beforePrizeTypes.add("PRIZE.COUPON.CYJJ.FREE.7.RECEIVE");
                        boolean receiveBefore = isReceiveBefore(customerId, beforePrizeTypes);
                        if (receiveBefore) {
                            continue;
                        }
                    }
                    StatusObjDto<CouponRecieveStatusDto> result = couponRecieveBiz.receive(activityCode, prizeType, userId, null, customerId, null, outTradeNo);
                    if (result != null && StatusDto.SUCCESS.equals(result.getCode()) && result.getObj() != null) {
                        MyPrizeDto myprize = result.getObj().getPrize();
                        if (myprize != null) {
                            dtoList.add(myprize);
                        }
                    }
                }
            }
        } else {
            for (ActPrize prize : list) {
                MyPrizeDto prizeDto = getMyPrizeDto(prize);
                dtoList.add(prizeDto);
            }
        }
        return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
    }

    /**
     * 不管在哪个活动，查询以前有没有领取过相关的奖品类型
     *
     * @param customerId
     * @param beforePrizeTypes
     * @return
     */
    public boolean isReceiveBefore(String customerId, List<String> beforePrizeTypes) {
        boolean result = false;
        ActPrize prize = new ActPrize();
        prize.setCodes(beforePrizeTypes);
        prize.setCustomerId(customerId);
        List<ActPrize> actPrizes = prizeDao.queryUserPrizes(prize);
        if (!CollectionUtils.isEmpty(actPrizes)) {
            result = true;
        }
        return result;
    }

    public StatusObjDto<List<MyPrizeDto>> receiveOrderId(String activityCode, String prizeTypes, String outTradeNo,
                                                         String customerId, String userId, String mobile) {
        List<MyPrizeDto> prizeDtos = Lists.newArrayList();

        ActPrize entity = new ActPrize();
        entity.setRemark(outTradeNo);
        entity.setUserId(userId);
        List<ActPrize> list = prizeDao.findList(entity);

        if (list.isEmpty()) {
            Activity act = findActivity(activityCode);

            /**查询订单信息**/
            Map<String, Object> orderParam = Maps.newHashMap();
            orderParam.put("customerId", customerId);
            orderParam.put("outTradeNo", outTradeNo);
            ResultDto orderDto = RemoteUtils.call("service.productcooperation.getorder", ApiServiceTypeEnum.COOPERATION, orderParam, true);
            logger.info("get order return : " + orderDto.getData());

            Map<String, Object> orderMap = orderDto.getData();
            /**订单信息有误，直接返回**/
            if (ResultDto.FAIL_COMMON.equals(orderDto.getCode()) || null == orderMap) {
                return new StatusObjDto<>(true, prizeDtos, StatusDto.SUCCESS, "");
            }

            /*支付状态*/
            Integer payStatus = (Integer) orderMap.get("status");
            /**订单未支付**/
            if (!CodeConstant.CODE_YES.equals(payStatus)) {
                return new StatusObjDto<>(true, prizeDtos, StatusDto.SUCCESS, "");
            }

            /*订单时间*/
            Date orderTime = new Date(Long.parseLong(String.valueOf(orderMap.get("createTime"))));
            /**订单创建时间不在活动时间之内**/
            if (orderTime.after(act.getDateTo()) || orderTime.before(act.getDateFrom())) {
                return new StatusObjDto<>(true, prizeDtos, StatusDto.SUCCESS, "");
            }

            String[] split = StringUtils.split(prizeTypes, ",");
            for (String prizeType : split) {
                /*决策工具发手机号*/
                StatusObjDto<CouponRecieveStatusDto> result = couponRecieveBiz.receive(activityCode, prizeType, userId, null, customerId, null, outTradeNo, mobile);
                if (result != null && StatusDto.SUCCESS.equals(result.getCode()) && result.getObj() != null) {
                    MyPrizeDto myprize = result.getObj().getPrize();
                    if (myprize != null) {
                        myprize.setIsCurrentEnable(CodeConstant.CODE_YES);
                        prizeDtos.add(myprize);
                    }
                }
            }
        } else {
            for (ActPrize prize : list) {
                StatusObjDto<CouponInfo> coupon = couponBiz.couponInfo(null, prize.getRedeemCode());
                MyPrizeDto prizeDto = CouponCommonReceiveBizImpl.buildMyPrizeDto(prize, coupon.getObj());
                prizeDtos.add(prizeDto);
            }
        }
        return new StatusObjDto<>(true, prizeDtos, StatusDto.SUCCESS, "");
    }

    /**
     * 抽球模型，配置大转盘奖品
     *
     * @param turntableeParam
     */
    private void initPrizes(String activityCode, String prizeTypeCode) {

        /*奖品每日限量配置*/
        ActPrizeType actPrizeType = findPrizeType(prizeTypeCode);

        String redisKey = activityCode + ":" + prizeTypeCode;

        if (actPrizeType.getDailyLimit() != null && !ACTIVITY_2023DOUBLE11_XTJJDS.equals(activityCode)) {
            if (!ActivityRedis.ACT_PRIZES_TIMER.exists(prizeTypeCode)) {
                /*超过当天23:59:59, 计时器会失效，就删掉缓存*/
                /*活动平台修改了奖品类型每日限量配置, 会主动触发删除计时器，也需要删除奖品缓存，后续重新加载缓存*/
                ActivityRedis.ACT_ACTVITY_PRIZE.del(redisKey);
            }
        }

        boolean hasInit = ActivityRedis.ACT_ACTVITY_PRIZE.exists(redisKey);
        if (hasInit) return;

        if (actPrizeType.getDailyLimit() != null && !ACTIVITY_2023DOUBLE11_XTJJDS.equals(activityCode)) {
            /*计时器还未失效，但是每日限量奖品领完了*/
            if (ActivityRedis.ACT_PRIZES_TIMER.exists(prizeTypeCode)) {
                return;
                // throw ActivityBizException.ACT_EMPTY_PRIZE;
            }
        }

        logger.info("初始化大转盘奖品.............................");

        /**中奖奖品初始化**/
        List<Long> prizes = actPrizeBiz.findAvailablePrizes(activityCode, prizeTypeCode);
        if (prizes == null) {
            logger.info("活动奖品未配置.............................");
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动奖品未配置");
        }
        if (prizes.size() == 0) {
            logger.info("无可用的活动奖品.............................");
            return;
            // throw ActivityBizException.ACT_PRIZE_NOT_VALID;
        }

        if (actPrizeType.getDailyLimit() != null && !ACTIVITY_2023DOUBLE11_XTJJDS.equals(activityCode)) {
            /*每日限制只能领取这么多奖品*/
            Integer limit = actPrizeType.getDailyLimit();
            /* 查今日已发出的数量 */
            Integer statusSendCount = actPrizeBiz.findPrizeCount(activityCode, prizeTypeCode, null, null, null,
                    null, ActPrize.STATUS_SEND, DateUtil.beginOfDay(new Date()), DateUtil.endOfDay(new Date()));
            /* 每日限量要减掉当天已经发出的数量 */
            limit = limit >= statusSendCount ? limit - statusSendCount : 0;
            prizes = prizes.size() > limit ? prizes.subList(0, limit) : prizes;
        }

        /**奖品**/
        int total = 0;
        for (int i = 0; i < prizes.size(); i++) {
            ActivityRedis.ACT_ACTVITY_PRIZE.sadd(redisKey, prizes.get(i));
            total++;
        }

        if (actPrizeType.getDailyLimit() != null && !ACTIVITY_2023DOUBLE11_XTJJDS.equals(activityCode)) {
            /*计时器记录奖品加入缓存的时间*/
            long timeout = Duration.between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now(), LocalTime.MAX)).getSeconds();
            ActivityRedis.ACT_PRIZES_TIMER.set(prizeTypeCode, "timer", (int) timeout);
        }

        logger.info("初始化大转盘奖品完毕.............................,total" + total + ",prize:" + prizes.size());
    }

    public Map<String, Integer> dailyLimit() {
        String dailyLimitConfig = AppConfigUtils.get(ACT_PRIZES_DAILY_LIMIT);
        List<ActPrizesDailyLimitDto> dailyLimitDtoList = JSON.parseArray(dailyLimitConfig, ActPrizesDailyLimitDto.class);
        dailyLimitDtoList = Optional.ofNullable(dailyLimitDtoList).orElse(Lists.newArrayList());
        return dailyLimitDtoList.stream()
                /*key：奖品编码， value：每日限量*/
                .collect(Collectors.toMap(ActPrizesDailyLimitDto::getPrizeType, ActPrizesDailyLimitDto::getLimit));
    }

    public static boolean isCoupon(Integer type) {
        if (ActPrize.TYPE_REDEEM.equals(type)) {
            return true;
        }
        if (ActPrize.TYPE_CARD_PASSWORD.equals(type)) {
            return true;
        }
        if (ActPrize.TYPE_COUPON.equals(type)) {
            return true;
        }
        if (ActPrize.TYPE_REDENVELOPE.equals(type)) {
            return true;
        }
        if (ActPrize.TYPE_FIRST_FIN_CARD.equals(type)) {
            return true;
        }
        if (ActPrize.TYPE_AIQY.equals(type)) {
            return true;
        }
        if (ActPrize.TYPE_YOUKU.equals(type)) {
            return true;
        }
        if (type.equals(ActPrize.TYPE_NEW_REDENVELOPE)) {
            return true;
        }
        if (type.equals(ActPrize.TYPE_DJQ)) {
            return true;
        }
        if (type.equals(ActPrize.TYPE_ALV2)) {
            return true;
        }
        return false;
    }

    private boolean isCoupon2(Integer type) {
        //类型，1：折扣券，2：免单券，3：满减券,4:权益券，5：额度券，6-level2,7-京东卡,9-红包,10-融券费率
        if (type == 6 || type == 7 || type == 9) {
            return true;
        }
        return false;
    }

    public StatusDto delRedis(String redisKey) {
        if (ObjectUtils.isEmptyOrNull(redisKey)) {
            return new StatusDto(true, StatusDto.SUCCESS, "");
        }
        logger.info("delPrize,{}", redisKey);
        ActivityRedis.ACT_ACTVITY_PRIZE.del(redisKey);
        return new StatusDto(true, StatusDto.SUCCESS, "");
    }

    /**
     * 延期奖品及优惠券
     *
     * @param redeemCode
     * @param recommendCode
     * @return
     */
    public ActPrize delayPrize(String redeemCode, String recommendCode) {
        if (ObjectUtils.isEmptyOrNull(recommendCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("recommendCode");
        }
        if (ObjectUtils.isEmptyOrNull(redeemCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("redeemCode");
        }
        ActPrize prize = prizeDao.findByRedeem(redeemCode);
        if (prize == null) {
            return null;
        }
        Date now = new Date();
        String remark = "delay by " + recommendCode + ",from " + DateUtils.formate(prize.getUpdateTime(), "yyyy-MM-dd HH:mm:ss");
        prize.setUpdateTime(now);
        prize.setRemark(remark);
        StatusDto delayDto = couponBiz.delayCoupon(redeemCode, recommendCode, now.getTime());
        if (!delayDto.isOk()) {
            logger.error("优惠券延期异常,code,{},recommendCode,{},msg,{}", redeemCode, recommendCode, delayDto.getMsg());
            return null;
        }
        prizeDao.delayPrize(prize);
        return prize;
    }

    public StatusObjDto<List<MyPrizeDto>> getPrizeByOrderId(String activityCode, String outTradeNo,
                                                            String platform, AccTokenUser user, Customer customer) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }

        if (ObjectUtils.isEmptyOrNull(outTradeNo)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("outTradeNo");
        }

        if (ObjectUtils.isEmptyOrNull(platform)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("platform");
        }

        if (!"vas".equals(platform) && !"service".equals(platform)) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("平台不正确");
        }

        Activity activity = activityBaseBiz.findActivity(activityCode);
        StatusDto actValidResult = activityBaseBiz.isValid(activity);
        if (!actValidResult.isOk()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(actValidResult.getMsg());
        }

        // 查询已经领取到的奖品
        ActPrize qryActPrize = new ActPrize();
        qryActPrize.setActivityCode(activityCode);
        qryActPrize.setRemark(outTradeNo);
//        qryActPrize.setUserId(user.getUserId());
        List<ActPrize> receivedPrizeList = prizeDao.findList(qryActPrize);

        List<MyPrizeDto> dtoList = new ArrayList<>();

        if (receivedPrizeList.isEmpty()) {

            StatusObjDto<ActPrizeType> checkResult = null;
            if ("vas".equals(platform)) {
                checkResult = orderCheckBiz.checkVasOrder(user, activity, outTradeNo);
            } else if ("service".equals(platform)) {
                checkResult = orderCheckBiz.checkServiceOrder(user, customer, activity, outTradeNo);
            }

            if (!checkResult.isOk()) {
                return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, checkResult.getMsg());
            }

            // 领取优惠券
            ActPrizeType prizeType = checkResult.getObj();
            List<CouponReceiveStatusDto> couponReceiveStatusDtoList = couponRecieveBiz.receiveCoupon(
                    activity, Lists.newArrayList(prizeType), receivedPrizeList, user, customer, outTradeNo);

            if (CollectionUtil.isNotEmpty(couponReceiveStatusDtoList)) {
                CouponReceiveStatusDto couponReceiveStatusDto = couponReceiveStatusDtoList.get(0);
                PrizeDto prizeDto = couponReceiveStatusDto.getPrize();
                MyPrizeDto myPrizeDto = new MyPrizeDto();
                myPrizeDto.setCode(prizeDto.getRedeemCode());
                myPrizeDto.setName(prizeDto.getName());
                myPrizeDto.setId(prizeDto.getId());
                myPrizeDto.setDescription(prizeDto.getDescription());
                myPrizeDto.setRegulation(prizeDto.getRegulation());
                myPrizeDto.setValidityDateFrom(prizeDto.getValidityDateFrom());
                myPrizeDto.setValidityDateTo(prizeDto.getValidityDateTo());
                myPrizeDto.setCode(prizeDto.getCode());
                myPrizeDto.setIsCurrentEnable(prizeDto.getIsCurrentEnable());
                myPrizeDto.setStatus(prizeDto.getStatus());
                myPrizeDto.setCouponType(prizeDto.getCouponType());
                myPrizeDto.setType(prizeDto.getType());
                myPrizeDto.setOpenUrl(prizeDto.getOpenUrl());
                dtoList.add(myPrizeDto);
            }
        } else {
            for (ActPrize prize : receivedPrizeList) {
                MyPrizeDto myPrizeDto = new MyPrizeDto();
                myPrizeDto.setId(prize.getId());
                myPrizeDto.setCode(prize.getRedeemCode());
                myPrizeDto.setName(prize.getName());
                dtoList.add(myPrizeDto);
            }
        }
        return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
    }

    private ActPrizeType getPrizeTypeForDouble11(Integer specification, Integer timeType) {
        if (timeType.equals(1) && specification.equals(3)) {
            // 买A股L2三个月, 送金股扫描仪1个月免单券
            return actPrizeTypeBiz.getPrizeType("PRIZE.COUPON.JGSMY1.2023DOUBLE11");
        } else if (timeType.equals(1) && specification.equals(6)) {
            // 买A股L2六个月, 送金股扫描仪3个月免单券
            return actPrizeTypeBiz.getPrizeType("PRIZE.COUPON.JGSMY3.2023DOUBLE11");
        }
        return null;
    }
}
