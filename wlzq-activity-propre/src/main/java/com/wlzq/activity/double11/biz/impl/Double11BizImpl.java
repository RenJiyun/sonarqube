package com.wlzq.activity.double11.biz.impl;

import cn.hutool.core.lang.Tuple;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActShareBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActShare;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.base.utils.HttpClientPoolUtils;
import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.activity.double11.dao.ActCustomerUnionIdDao;
import com.wlzq.activity.double11.dao.Double11Dao;
import com.wlzq.activity.double11.dto.*;
import com.wlzq.activity.double11.model.ActCustomerUnionId;
import com.wlzq.activity.double11.redis.Double11Redis;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.label.ILabelApi;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 双11活动接口实现
 *
 * @author louie
 */
@Service
@Slf4j
public class Double11BizImpl extends ActivityBaseBiz implements Double11Biz {
    /**
     * 活动编码
     */
    private static final String ACTIVITY_CODE = "ACTIVITY.DOUBLE11_2019";
    /**
     * Level2奖品编码
     */
    private static final String LEVEL2_PRIZE_CODE = "PRIZE.DOUBLE11_2019.LEVEL2_1";
    /***/
    private static final String ACTIVITY_2022_DOUBLE11 = "ACTIVITY.2022DOUBLE11.INVEST.COUPON";

    /** 企微添加渠道编码, 与财人汇约定 */
    private static final String QIWEI_SOURCENAME = "act_annual_bill_2023";

    @Autowired
    private ActShareBiz actShareBiz;
    @Autowired
    private ActPrizeBiz prizeBiz;
    @Autowired
    private Double11Dao double11Dao;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActCustomerUnionIdDao actCustomerUnionIdDao;
    @Autowired
    private ILabelApi iLabelApi;
    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;

    @Override
    public StatusDto l2receive(String userId, String openId, String mobile, String customerId, String fundAccount) {
        if (ObjectUtils.isEmptyOrNull(userId)) {
            throw BizException.NOT_LOGIN_ERROR;
        }
        StatusDto isValidAct = isValid(ACTIVITY_CODE);
        if (!isValidAct.isOk()) {
            return new StatusObjDto<Integer>(false, isValidAct.getCode(), isValidAct.getMsg());
        }

        actShareBiz.saveShare(ActShare.TYPE_WECHAT, ACTIVITY_CODE, userId, openId, customerId);
        actShareBiz.shareCount(ACTIVITY_CODE, userId, null, null);
        List<ActPrize> prizes = prizeBiz.findUserPrizes(ACTIVITY_CODE, userId, null, null);
        if (prizes.size() > 0) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("已获取过Level2奖品");
        }
        ActPrize prize = prizeBiz.giveOutPrize(ACTIVITY_CODE, "", null, LEVEL2_PRIZE_CODE, userId, openId, customerId, mobile);
        if (prize == null) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
        }
        return new StatusDto(true, StatusDto.SUCCESS, "");
    }

    @Override
    public StatusObjDto<Map<String, Object>> achievement(Long achievementDate, Integer achievementType) {
        String yyyyMMdd = DateUtils.parseTimetamp(achievementDate, "yyyyMMdd");

        Double11Redis double11Redis = double11RedisOf(achievementType);
        List achievementDto = JsonUtils.jsonToList((String) double11Redis.get(yyyyMMdd));

        Map<String, Object> data = Maps.newHashMap();

        Date updateTime = null;
        if (achievementDto == null) {
            achievementDto = new ArrayList<>();
        } else {
            Double11Redis updateTimeRedis = updateTimeRedisOf(achievementType);
            updateTime = (Date) updateTimeRedis.get(updateTimeKeyOf(updateTimeRedis));
        }

        if (updateTime == null) {
            updateTime = new Date();
        }

        Activity activity = activityBaseBiz.findActivity(ACTIVITY_2022_DOUBLE11);

        data.put("info", achievementDto);
        data.put("updateTime", updateTime.getTime());
        data.put("dateFrom", activity.getDateFrom().getTime());
        data.put("dateTo", activity.getDateTo().getTime());

        return new StatusObjDto<>(true, data, 0, "");
    }

    private Double11Redis double11RedisOf(Integer achievementType) {
        HashMap<Integer, Double11Redis> cache = new HashMap<>();
        cache.put(1, Double11Redis.PERSONAL_SPRINT_AWARD_LIST);
        cache.put(2, Double11Redis.PERSONAL_OUTBREAK_AWARD_LIST);
        cache.put(3, Double11Redis.THS_ADVISER_TEAM_SPRINT_AWARD_LIST);
        cache.put(4, Double11Redis.THS_ADVISER_TEAM_OUTBREAK_AWARD_LIST);
        cache.put(5, Double11Redis.DECISION_SALE_PROMOTION_AWARD_LIST);
        cache.put(6, Double11Redis.DECISION_SALE_COMMUNICATION_AWARD_LIST);

        return cache.get(achievementType);
    }

    private Double11Redis updateTimeRedisOf(Integer achievementType) {
        HashMap<Integer, Double11Redis> cache = new HashMap<>();
        cache.put(1, Double11Redis.PERSONAL_AWARD_UPDATE_TIME);
        cache.put(2, Double11Redis.PERSONAL_AWARD_UPDATE_TIME);
        cache.put(3, Double11Redis.THS_ADVISER_TEAM_AWARD_UPDATE_TIME);
        cache.put(4, Double11Redis.THS_ADVISER_TEAM_AWARD_UPDATE_TIME);
        cache.put(5, Double11Redis.DECISION_SALE_AWARD_UPDATE_TIME);
        cache.put(6, Double11Redis.DECISION_SALE_AWARD_UPDATE_TIME);

        return cache.get(achievementType);
    }

    private String updateTimeKeyOf(Double11Redis updateTimeRedis) {
        HashMap<Double11Redis, String> cache = new HashMap<>();

        Activity activity = activityBaseBiz.findActivity(ACTIVITY_2022_DOUBLE11);
        Integer actBeginDate = Integer.valueOf(DateUtils.formate(activity.getDateFrom(), "yyyyMMdd"));

        cache.put(Double11Redis.PERSONAL_AWARD_UPDATE_TIME, String.valueOf(actBeginDate));
        cache.put(Double11Redis.THS_ADVISER_TEAM_AWARD_UPDATE_TIME, String.valueOf(actBeginDate));
        cache.put(Double11Redis.DECISION_SALE_AWARD_UPDATE_TIME, DecisionSaleCommunicationAward.DECISION_SALE_AWARD_UPDATE_TIME);

        return cache.get(updateTimeRedis);
    }

    @Override
    public StatusDto updatePersonalAwardRanking() {
        Date today = new Date();
        /**推荐人更新时间*/
        Date updateEndDate = DateUtils.parseDate("2022-11-20 23:59:59", "yyyy-MM-dd HH:mm:ss");
        /**获取主会场活动*/
        Activity activity = activityBaseBiz.findActivity(ACTIVITY_2022_DOUBLE11);
        /**活动开始时间*/
        Integer actBeginDate = Integer.valueOf(DateUtils.formate(activity.getDateFrom(), "yyyyMMdd"));
        /**活动结束时间*/
        Integer actEndDate = Integer.valueOf(DateUtils.formate(activity.getDateTo(), "yyyyMMdd"));
        Integer now = Integer.valueOf(DateUtils.formate(today, "yyyyMMdd"));
        /**活动未开始或已过推荐人信息更新时间,马上返回*/
        if (today.after(updateEndDate) || now < actBeginDate) {
            return new StatusDto(true, 0);
        }
        if (now >= actBeginDate && now <= actEndDate) {
            actEndDate = now;
        }
        Boolean flag = true;
        while (flag) {
            Date beginDate = DateUtils.parseDate(actBeginDate + " 08:30:00", "yyyyMMdd HH:mm:ss");
            Date endDate = DateUtils.parseDate(actBeginDate + " 23:59:59", "yyyyMMdd HH:mm:ss");
            List<PersonalSprintAward> personalSprintAwardList = double11Dao.findPersonalSprintAwardList(beginDate, endDate);
            /**设置冲刺奖到缓存*/
            if (!CollectionUtils.isEmpty(personalSprintAwardList)) {
                Double11Redis.PERSONAL_SPRINT_AWARD_LIST.set(String.valueOf(actBeginDate), JsonUtils.object2JSON(personalSprintAwardList));
            }
            beginDate = DateUtils.parseDate(actBeginDate + " 00:00:00", "yyyyMMdd HH:mm:ss");
            List<PersonalOutbreakAward> personalOutbreakAwardList = double11Dao.findPersonalOutbreakAwardList(beginDate, endDate);
            /**设置爆发奖到缓存*/
            if (!CollectionUtils.isEmpty(personalOutbreakAwardList)) {
                Double11Redis.PERSONAL_OUTBREAK_AWARD_LIST.set(String.valueOf(actBeginDate), JsonUtils.object2JSON(personalOutbreakAwardList));
            }
            /**设置e万通各人奖励更新时间*/
            Double11Redis.PERSONAL_AWARD_UPDATE_TIME.set(String.valueOf(actBeginDate), new Date());
            log.info("e万通各人奖励:{}日的排行榜更新完毕", String.valueOf(actBeginDate));
            if (actBeginDate < actEndDate) {
                /**加一天*/
                actBeginDate = Integer.valueOf(DateUtils.formate(DateUtils.addDay(beginDate, 1), "yyyyMMdd"));
            } else {
                flag = false;
            }
        }
        return new StatusDto(true, 0);
    }

    @Override
    public StatusDto updateThsAdviserTeamAwardRanking() {
        Date today = new Date();
        /**获取主会场活动*/
        Activity activity = activityBaseBiz.findActivity(ACTIVITY_2022_DOUBLE11);
        /**活动开始时间*/
        Integer actBeginDate = Integer.valueOf(DateUtils.formate(activity.getDateFrom(), "yyyyMMdd"));
        /**活动结束时间*/
        Integer actEndDate = Integer.valueOf(DateUtils.formate(activity.getDateTo(), "yyyyMMdd"));
        Integer now = Integer.valueOf(DateUtils.formate(today, "yyyyMMdd"));
        /**活动未开始马上返回*/
        if (now < actBeginDate) {
            return new StatusDto(true, 0);
        }
        if (now >= actBeginDate && now <= actEndDate) {
            actEndDate = now;
        }
        Boolean flag = true;
        while (flag) {
            Date beginDate = DateUtils.parseDate(actBeginDate + " 08:30:00", "yyyyMMdd HH:mm:ss");
            Date endDate = DateUtils.parseDate(actBeginDate + " 23:59:59", "yyyyMMdd HH:mm:ss");
            List<ThsAdviserTeamSprintAward> thsAdviserTeamSprintAwardList = double11Dao.findThsAdviserTeamSprintAwardList(beginDate, endDate);
            /**设置冲刺奖到缓存*/
            if (!CollectionUtils.isEmpty(thsAdviserTeamSprintAwardList)) {
                Double11Redis.THS_ADVISER_TEAM_SPRINT_AWARD_LIST.set(String.valueOf(actBeginDate), JsonUtils.object2JSON(thsAdviserTeamSprintAwardList));
            }
            beginDate = DateUtils.parseDate(actBeginDate + " 00:00:00", "yyyyMMdd HH:mm:ss");
            List<ThsAdviserTeamOutbreakAward> thsAdviserTeamOutbreakAwardList = double11Dao.findThsAdviserTeamOutbreakAwardList(beginDate, endDate);
            /**设置爆发奖到缓存*/
            if (!CollectionUtils.isEmpty(thsAdviserTeamOutbreakAwardList)) {
                Double11Redis.THS_ADVISER_TEAM_OUTBREAK_AWARD_LIST.set(String.valueOf(actBeginDate), JsonUtils.object2JSON(thsAdviserTeamOutbreakAwardList));
            }
            /**设置同花顺投顾团队奖励更新时间*/
            Double11Redis.THS_ADVISER_TEAM_AWARD_UPDATE_TIME.set(String.valueOf(actBeginDate), new Date());
            log.info("同花顺投顾团队奖励:{}日的排行榜更新完毕", String.valueOf(actBeginDate));
            if (actBeginDate < actEndDate) {
                /**加一天*/
                actBeginDate = Integer.valueOf(DateUtils.formate(DateUtils.addDay(beginDate, 1), "yyyyMMdd"));
            } else {
                flag = false;
            }
        }
        return new StatusDto(true, 0);
    }

    @Override
    public StatusDto updateDecisionSaleAwardRanking() {
        Date today = new Date();
        /**推荐人更新时间*/
        Date updateEndDate = DateUtils.parseDate("2022-11-20 23:59:59", "yyyy-MM-dd HH:mm:ss");
        /**获取主会场活动*/
        Activity activity = activityBaseBiz.findActivity(ACTIVITY_2022_DOUBLE11);
        /**活动开始时间*/
        Integer actBeginDate = Integer.valueOf(DateUtils.formate(activity.getDateFrom(), "yyyyMMdd"));
        /**活动结束时间*/
        Integer actEndDate = Integer.valueOf(DateUtils.formate(activity.getDateTo(), "yyyyMMdd"));
        Integer now = Integer.valueOf(DateUtils.formate(today, "yyyyMMdd"));
        /**活动未开始或已过推荐人信息更新时间,马上返回*/
        if (today.after(updateEndDate) || now < actBeginDate) {
            return new StatusDto(true, 0);
        }
        Date beginDate = DateUtils.parseDate(actBeginDate + " 00:00:00", "yyyyMMdd HH:mm:ss");
        Date endDate = DateUtils.parseDate(actEndDate + " 23:59:59", "yyyyMMdd HH:mm:ss");
        List<DecisionSaleCommunicationAward> decisionSaleCommunicationAwardList = double11Dao.findDecisionSaleCommunicationAwardList(beginDate, endDate);
        /**设置决策工具销售传播奖到缓存*/
        if (!CollectionUtils.isEmpty(decisionSaleCommunicationAwardList)) {
            Double11Redis.DECISION_SALE_COMMUNICATION_AWARD_LIST.set(String.valueOf(actEndDate), JsonUtils.object2JSON(decisionSaleCommunicationAwardList));
        }
        /**设置决策工具销售奖励更新时间*/
        Double11Redis.DECISION_SALE_AWARD_UPDATE_TIME.set(DecisionSaleCommunicationAward.DECISION_SALE_AWARD_UPDATE_TIME, new Date());
        log.info("决策工具销售奖励{}的排行榜更新完毕", String.valueOf(actEndDate));
        return new StatusDto(true, 0);
    }

    @Override
    @Transactional
    public StatusDto saveUnionId(String unionId, String openId, String customerId, String mobile) {
        StatusDto statusDto = new StatusDto(true, 0);
        ActCustomerUnionId actCustomerUnionId = new ActCustomerUnionId()
                .setUnionId(unionId)
                .setCustomerId(customerId)
                .setOpenId(openId)
                .setMobile(mobile)
                .setCreateTime(new Date());
        actCustomerUnionIdDao.insert(actCustomerUnionId);
        return statusDto;
    }

    private String getCrhApiUrl() {
        return AppConfigUtils.get("2023double11.crh.api.url", "http://w2l0z0q2q1y2w1x2.wlzq.cn:8090/api");
    }

    @Override
    public QrcodeDto getQrcode(String customerId) {
        String appid = AppConfigUtils.get("2023double11.crh.api.appid", "wlzq");
        String proof = AppConfigUtils.get("2023double11.crh.api.proof", "B70A326CC2CE504047EE4963AA31C26F");
        String url = getCrhApiUrl() + "/cust/client/getQrcodeByKhh";
        Map<String, Object> headers = new HashMap<>();
        headers.put("appid", appid);
        headers.put("proof", proof);

        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("client_id", customerId);
        reqParams.put("sourceName", QIWEI_SOURCENAME);
        String result;
        try {
            result = HttpClientPoolUtils.httpGetRequest(url, headers, reqParams);
            log.info("财人汇接口获取二维码: client_id = {}, result = {}", customerId, result);
        } catch (Throwable t) {
            log.error("财人汇接口获取二维码异常: client_id = {}", customerId);
            return null;
        }
        Gson gson = new Gson();
        Map<String, Object> resultMap = gson.fromJson(result, Map.class);
        String code = resultMap.get("code").toString();
        if ("0000".equals(code)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            String qrCode = dataMap.get("qrCode").toString();
            String mobile = dataMap.get("phoneNumber") == null ? "": dataMap.get("phoneNumber").toString();
            String qwUserid = dataMap.get("qwUserid") == null ? "" : dataMap.get("qwUserid").toString();
            return new QrcodeDto().setQrCode(qrCode).setMobile(mobile).setQwUserid(qwUserid);
        } else {
            log.error("财人汇接口获取二维码失败: client_id = {}, code = {}, msg = {}", customerId, code, resultMap.get("msg"));
        }
        return null;
    }

    @Override
    public Tuple checkQiWeiAddResult(String customerId, String unionId) {
        String appid = AppConfigUtils.get("2023double11.crh.api.appid", "wlzq");
        String proof = AppConfigUtils.get("2023double11.crh.api.proof", "B70A326CC2CE504047EE4963AA31C26F");
        String url = getCrhApiUrl() + "/cust/client/qwAddResult";
        Map<String, Object> headers = new HashMap<>();
        headers.put("appid", appid);
        headers.put("proof", proof);

        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("client_id", customerId);
        reqParams.put("unionid", unionId);
        String result;
        try {
            result = HttpClientPoolUtils.httpGetRequest(url, headers, reqParams);
            log.info("财人汇接口查询添加结果: client_id = {}, unionid = {}, result = {}", customerId, unionId, result);
        } catch (Throwable t) {
            log.error("财人汇接口查询添加结果异常: client_id = {}, unionid = {}", customerId, unionId);
            return new Tuple(false, "");
        }
        Gson gson = new Gson();
        Map<String, Object> resultMap = gson.fromJson(result, Map.class);
        String code = resultMap.get("code").toString();
        if ("0000".equals(code)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            BigDecimal addResult = new BigDecimal(dataMap.get("addResult").toString());
            if (addResult.compareTo(BigDecimal.ONE) == 0) {
                return new Tuple(true, dataMap.get("sourceName"));
            } else {
                return new Tuple(false, "");
            }
        } else {
            log.error("财人汇接口查询添加结果失败: client_id = {}, unionid = {}, code = {}, msg = {}",
                    customerId, unionId, code, resultMap.get("msg"));
        }
        return new Tuple(false, "");
    }


    @Override
    public boolean checkNonAccount(Customer customer) {
        String label = "x2010016";
        StatusObjDto<Map<String, Object>> statusObjDto = null;
        try {
            // 第一个入参为2, 表示查询的客群类型为客户
            statusObjDto = iLabelApi.queryHistoryLabelList(2, customer.getCustomerId(), "everyDay", "");
        } catch (Throwable t) {
            log.error("有效户标签查询异常: customerId = {}, msg = {}", customer.getCustomerId(), t.getMessage());
            return false;
        }

        if (statusObjDto != null && statusObjDto.isOk()) {
            Map<String, Object> data = statusObjDto.getObj();
            // 含有指定标签即为有效户
            if (data != null && ObjectUtils.isNotEmptyOrNull(data.get(label))) {
                return false;
            }
        } else {
            // 若查询失败, 则默认该客户为有效户
            log.warn("有效户标签查询失败: customerId = {}, msg = {}", customer.getCustomerId(),
                    statusObjDto == null ? "null" : statusObjDto.getMsg());
            return false;
        }
        return true;
    }

    @Override
    public void supplementRecommend(AccTokenUser user, String outTradeNo, String recommendMobile) {
        String mobile = user.getMobile();
        ActGoodsFlow qryActGoodsFlow = new ActGoodsFlow()
                .setActivityCode("ACTIVITY.2023DOUBLE11.ZJF")
                .setBizCode(outTradeNo)
                .setMobile(mobile);
        List<ActGoodsFlow> actGoodsFlowList = actGoodsFlowDao.list(qryActGoodsFlow);
        if (CollectionUtils.isEmpty(actGoodsFlowList)) {
            return;
        }

        ActGoodsFlow actGoodsFlow = actGoodsFlowList.get(0);
        // 更新推荐人信息
        String specialMobile = "18602705253";
        if (specialMobile.equals(recommendMobile)) {
            recommendMobile = "";
        }
        actGoodsFlow.setRecommendMobile(recommendMobile);
        actGoodsFlow.setUsedGoodsQuantity(0.0);
        actGoodsFlowDao.update(actGoodsFlow);
    }
}
