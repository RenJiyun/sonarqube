package com.wlzq.activity.task.biz;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.ActivityBiz;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.activity.task.dto.ActTaskReqDto;
import com.wlzq.activity.task.dto.ActTaskResDto;
import com.wlzq.activity.task.dto.ActTaskStatusDto;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.dao.ActTaskDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.*;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.AccountUserBiz;
import com.wlzq.remote.service.common.account.FortuneBiz;
import com.wlzq.remote.service.common.account.PointBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动task服务类
 *
 * @author wlzq
 */
@Service
@Slf4j
public class TaskBiz extends ActivityBaseBiz {

    @Value("${fin.url.pre}")
    private String BEHAVIOR_FIN_URL_PRE;
    @Value("${invest.url.pre}")
    private String BEHAVIOR_INVEST_URL_PRE;
    @Autowired
    private FortuneBiz fortuneBiz;
    @Autowired
    private AccountUserBiz accountUserBiz;
    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;
    @Autowired
    private ActTaskDao actTaskDao;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActivityBiz activityBiz;
    @Autowired
    private PointBiz pointBiz;
    @Autowired
    private TaskPoolBiz taskPoolBiz;

    private static final String GOODS_CODE_ACT_POINT = "ACT.POINT";
    /* 文章留言 */
    private static final String TASK_ACT_VIEW_ARTICLES_COMMENT = "TASK.ACT.VIEW.ARTICLES.COMMENT";
    /* 购买投顾产品 */
    private static final String TASK_ACT_VIEW_INVEST_BUY = "TASK.ACT.VIEW.INVEST.BUY";
    /* 关注投顾 */
    private static final String TASK_ACT_VIEW_INVEST_CARE = "TASK.ACT.VIEW.INVEST.CARE";

    private static final String TASK_GGQZD = "TASK.GGQZD";

    public ActTaskResDto queryTaskStatus(ActTaskReqDto req) {
        ActTaskResDto resDto = null;
        String userId = req.getUserId();
        if (StringUtils.isNotBlank(req.getAccessToken())) {
            //有传accessToken，优先用 key + accessToken
            resDto = queryTaskStatusByToken(req, req.getKey(), req.getAccessToken());
        } else if (StringUtils.isNotBlank(userId)) {
            //没有accessToken，用userId
            AccUser accUser = accountUserBiz.findByUserId(userId);
            if (accUser == null || StringUtils.isBlank(accUser.getMobile())) {
                resDto = new ActTaskResDto();
            } else {
                resDto = queryTaskStatus(req, req.getCustomerId(), userId, accUser.getMobile());
            }
        } else {
            //没有accessToken， 也没userId, 只有customerId, 目前是不支持的。userId，accessToken 不能同时为空。
            throw BizException.COMMON_PARAMS_NOT_NULL.format("userId，accessToken");
        }
        return resDto;
    }

    private ActTaskResDto queryTaskStatusByToken(ActTaskReqDto req, String key, String accessToken) {
        //授权财富账号信息
        FortuneInfo fortuneInfo = fortuneBiz.fortuneInfoByAuth(key, accessToken);
        if (fortuneInfo == null) {
            throw BizException.CUSTOMER_NOT_LOGIN_ERROR;
        }
        User user = fortuneInfo.getUser();
        WCustomer customer = fortuneInfo.getCustomer();

        String customerId = customer != null ? customer.getCustomerId() : "";
        String userId = user.getUserId();
        String mobile = user.getMobile();
        return queryTaskStatus(req, customerId, userId, mobile);
    }

    /**
     * 查询任务状态：
     * 1：浏览主推基金页
     * 去 behavior查
     * 2：浏览投顾社区文章
     * 去 behavior查
     * 3：首次开通条件单 - 用手机号查
     * 去account查：
     * 4: 是否为新用户 - 用手机号查
     * 经过base，然后去二方服务查
     */
    private ActTaskResDto queryTaskStatus(ActTaskReqDto req, String customerId, String userId, String mobile) {
        String tasks = req.getTask();
        if (StringUtils.isBlank(tasks)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("任务参数异常");
        }

        List<ActTaskStatusDto> taskStatusDtos = new ArrayList<>();
        ActTaskResDto res = new ActTaskResDto().setBeginDate(req.getBeginDate()).setEndDate(req.getEndDate()).setTaskStatusDtos(taskStatusDtos);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beginDateTime = LocalDateTimeUtil.of(req.getBeginDate());
        LocalDateTime endDateTime = LocalDateTimeUtil.of(req.getEndDate());
        String beginDateTimeStr = LocalDateTimeUtil.format(beginDateTime, DatePattern.NORM_DATETIME_PATTERN);
        String endDateTimeStr = LocalDateTimeUtil.format(endDateTime, DatePattern.NORM_DATETIME_PATTERN);

        List<Integer> taskList = Arrays.stream(tasks.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        for (Integer task : taskList) {
            try {
                ActTaskStatusDto taskStatusDto = new ActTaskStatusDto().setTask(task).setStatus(0);
                taskStatusDtos.add(taskStatusDto);
                if (task == 1 || task == 2) {
                    taskBehavior(userId, task, taskStatusDto, beginDateTimeStr, endDateTimeStr);
                } else if (task == 3) {
                    task3OpenTjd(mobile, taskStatusDto, beginDateTimeStr, endDateTimeStr);
                } else if (task == 4) {
                    task4IsNewCustomer(mobile, taskStatusDto, now, beginDateTime, endDateTime);
                } else if (task == 5) {
                    task58CountAdviserOrder(taskStatusDto, customerId, userId, beginDateTimeStr, endDateTimeStr, 2, task);
                } else if (task == 6) {
                    task6CountAdviserFollow(taskStatusDto, customerId, userId, beginDateTimeStr, endDateTimeStr);
                } else if (task == 7) {
                    task7CountAdviserCollect(taskStatusDto, customerId, userId, beginDateTimeStr, endDateTimeStr);
                } else if (task == 8) {
                    task58CountAdviserOrder(taskStatusDto, customerId, userId, beginDateTimeStr, endDateTimeStr, 3, task);
                } else if (task == 9) {
                    task9CountAdviserLike(taskStatusDto, customerId, userId, beginDateTimeStr, endDateTimeStr);
                } else if (task == 10) {
                    task10CountArticleComment(taskStatusDto, customerId, userId, beginDateTimeStr, endDateTimeStr);
                } else if (task == 11) {
                    task11CountCourseProgress(taskStatusDto, customerId, userId, beginDateTimeStr, endDateTimeStr);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }
        }
        return res;
    }


    private void task58CountAdviserOrder(ActTaskStatusDto taskStatusDto, String customerId, String userId, String beginTime, String endTime, Integer adviserProductType, Integer task) {
        Map<String, Object> pMap = new HashMap<>();
        if (StringUtils.isBlank(customerId)) {
            pMap.put("userId", userId);
        } else {
            pMap.put("customerId", customerId);
        }

        pMap.put("minTotalFee", 0);
        //        if (StringUtils.equals(SpringUtil.getActiveProfile(),"test")) {
        //            pMap.put("minTotalFee",1);
        //        }else{
        //            pMap.put("minTotalFee",10000);//查询大于100元的订阅订单。
        //        }
        pMap.put("startTime", beginTime);
        pMap.put("endTime", endTime);
        pMap.put("adviserProductType", adviserProductType);//InvestAdviserProduct.TYPE_STRATEGE
        ResultDto resultDto = RemoteUtils.call("service.productcooperation.countadviserorder", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Map<String, Object> data = resultDto.getData();
            Integer count = (Integer) data.get("count");
            taskStatusDto.setTimes(count);
            if (count >= 1) {
                taskStatusDto.setStatus(1);
            }
            if (task == 5) {
                Integer amount = (Integer) data.get("amount");
                taskStatusDto.setAmount(amount);
            }
        }
    }

    private void task6CountAdviserFollow(ActTaskStatusDto res, String customerId, String userId, String beginTime, String endTime) {
        Map<String, Object> pMap = new HashMap<>();

        if (StringUtils.isBlank(customerId)) {
            pMap.put("userId", userId);
        } else {
            pMap.put("customerId", customerId);
        }

        pMap.put("startTime", beginTime);
        pMap.put("endTime", endTime);
        ResultDto resultDto = RemoteUtils.call("service.advisercooperation.countadviserfollow", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("count");
            res.setTimes(count);
            if (count >= 1) {
                res.setStatus(1);
            }
        }
    }

    private void task7CountAdviserCollect(ActTaskStatusDto res, String customerId, String userId, String beginTime, String endTime) {
        Map<String, Object> pMap = new HashMap<>();

        if (StringUtils.isBlank(customerId)) {
            pMap.put("userId", userId);
        } else {
            pMap.put("customerId", customerId);
        }

        pMap.put("startTime", beginTime);
        pMap.put("endTime", endTime);
        ResultDto resultDto = RemoteUtils.call("service.articlecooperation.countadvisercollect", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("count");
            res.setTimes(count);
            if (count >= 1) {
                res.setStatus(1);
            }
        }
    }

    private void task9CountAdviserLike(ActTaskStatusDto res, String customerId, String userId, String beginTime, String endTime) {
        Map<String, Object> pMap = new HashMap<>();

        if (StringUtils.isBlank(customerId)) {
            pMap.put("userId", userId);
        } else {
            pMap.put("customerId", customerId);
        }

        pMap.put("startTime", beginTime);
        pMap.put("endTime", endTime);
        ResultDto resultDto = RemoteUtils.call("service.articlecooperation.countadviserlike", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("count");
            res.setTimes(count);
            if (count >= 1) {
                res.setStatus(1);
            }
        }
    }

    private void task10CountArticleComment(ActTaskStatusDto res, String customerId, String userId, String beginTime, String endTime) {
        Map<String, Object> pMap = new HashMap<>();

        if (StringUtils.isBlank(customerId)) {
            pMap.put("userId", userId);
        } else {
            pMap.put("customerId", customerId);
        }

        pMap.put("startTime", beginTime);
        pMap.put("endTime", endTime);
        ResultDto resultDto = RemoteUtils.call("service.articlecooperation.countarticlecomment", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("count");
            res.setTimes(count);
            if (count >= 1) {
                res.setStatus(1);
            }
        }
    }

    private void task11CountCourseProgress(ActTaskStatusDto res, String customerId, String userId, String beginTime, String endTime) {
        Map<String, Object> pMap = new HashMap<>();

        if (StringUtils.isBlank(customerId)) {
            pMap.put("userId", userId);
        } else {
            pMap.put("customerId", customerId);
        }

        pMap.put("startTime", beginTime);
        pMap.put("endTime", endTime);
        ResultDto resultDto = RemoteUtils.call("service.coursecooperation.countcourseprogress", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("count");
            res.setTimes(count);
            if (count >= 1) {
                res.setStatus(1);
            }
        }
    }

    private void taskBehavior(String userId, Integer task, ActTaskStatusDto res, String beginDateStr, String endDateStr) {
        Map<String, Object> pMap = new HashMap<>();
        //查询行为
        String pageCode = "";
        if (task == 1) {
            pageCode = BEHAVIOR_FIN_URL_PRE;
        } else if (task == 2) {
            pageCode = BEHAVIOR_INVEST_URL_PRE;
        }
        pMap.put("pageCode", pageCode);
        pMap.put("userId", userId);
        pMap.put("beginDateStr", beginDateStr);
        pMap.put("endDateStr", endDateStr);

        ResultDto resultDto = RemoteUtils.call("behavior.buryingpoint.querybehaviorevent", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer status = (Integer) resultDto.getData().get(pageCode);
            res.setStatus(status);
            if (ObjectUtils.equals(status, 1)) {
                res.setTimes(1);
            }
        }
    }

    /**
     * 查开通条件单开通
     *
     * @param mobile
     * @param res
     * @param beginDateStr
     * @param endDateStr
     */
    public void task3OpenTjd(String mobile, ActTaskStatusDto res, String beginDateStr, String endDateStr) {
        Map<String, Object> pMap = new HashMap<>();
        pMap.put("mobile", mobile);
        pMap.put("beginDateStr", beginDateStr);
        pMap.put("endDateStr", endDateStr);
        ResultDto resultDto = RemoteUtils.call("account.tjd.querytjdopen", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer times = (Integer) resultDto.getData().get("times");
            if (times != null && times >= 1) {
                res.setStatus(1);
                res.setTimes(times);
            }
        }
    }

    /**
     * 查询新用户，一个月内注册的为新用户。如果查不到用户信息，也算新用户
     *
     * @param mobile
     * @param res
     * @param now
     * @param beginData
     * @param endData
     */
    private void task4IsNewCustomer(String mobile, ActTaskStatusDto res, LocalDateTime now, LocalDateTime beginData, LocalDateTime endData) {
        LocalDateTime registerTime = queryRegisterTime(mobile, now);
        if (registerTime != null) {
            if ((registerTime.isAfter(beginData) || registerTime.isEqual(beginData))
                    && (registerTime.isBefore(endData) || registerTime.isEqual(endData))) {
                res.setStatus(1);
                res.setTimes(1);
            }
        }
    }


    /**
     * 通过手机号，获取客户首次注册时间。三种情况：
     * <p/> 接口调用异常，返回null；
     * <p/> 接口调用成功，查询不到注册时间，返回入参默认值 defaultValue；
     * <p/> 接口调用成功，查询到注册时间，返回注册时间。
     *
     * @param mobile
     * @param defaultValue , 查询不到的情况，是否默认是今天注册
     */
    public LocalDateTime queryRegisterTime(String mobile, LocalDateTime defaultValue) {
        String p = "{\"V_SJHM\":\"" + mobile + "\"}";
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceId", "ext.sjzx.ewt.khzcsj");
        params.put("isNeedLogin", 1);
        params.put("params", p);

        //当天注册是没有返回的
        ResultDto result = RemoteUtils.call("base.fsdpcoopration.callservice", ApiServiceTypeEnum.COOPERATION, params, true);

        LocalDateTime re = null;
        if (result != null && ObjectUtils.equals(result.getCode(), 0)) {
            Map<String, Object> oData = result.getData();
            Integer code = (Integer) oData.get("code");
            if (ObjectUtils.equals(code, 1)) {
                ArrayList<Map<String, Object>> oResultList = (ArrayList<Map<String, Object>>) oData.get("O_RESULT");
                if (!CollectionUtils.isEmpty(oResultList)) {
                    //如果返回多个注册时间，取最新的注册时间
                    LocalDateTime tmpDate;
                    for (int i = 0; i < oResultList.size(); i++) {
                        Map<String, Object> item = oResultList.get(i);
                        String registerDate = (String) item.get("ZCSJ");
                        if (StringUtils.isNotBlank(registerDate)) {
                            tmpDate = LocalDateTimeUtil.parse(registerDate, DatePattern.PURE_DATE_PATTERN);
                            if (re == null || tmpDate.isAfter(re)) {
                                re = tmpDate;
                            }
                        }
                    }
                } else {
                    //如果查询不到用户注册时间，就返回入参默认值 defaultValue
                    re = defaultValue;
                }
            }
        }
        return re;
    }


    public StatusObjDto<ActTaskStatusDto> queryUserTaskStatus(ActTaskReqDto req) {
        ActTaskStatusDto statusDto = new ActTaskStatusDto();

        Map<String, ActTask> taskMap = null;
        if (Double11Biz.ACTIVITY_2023DOUBLE11_ZJF.equals(req.getActivityCode())) {
            taskMap = taskPoolBiz.getActTaskMap();
            ActTask actTask = taskMap.get(req.getTaskCode());
            if (actTask == null) {
                throw BizException.COMMON_PARAMS_NOT_NULL.format("任务不存在");
            }
            if (actTask.getAllTotalNum() != null && actTask.getAllTotalNum() > 0) {
                ActGoodsFlow qryActGoodsFlow = new ActGoodsFlow()
                        .setMobile(req.getMobile())
                        .setActivityCode(req.getActivityCode())
                        .setTaskCode(req.getTaskCode());
                List<ActGoodsFlow> actGoodsFlows = actGoodsFlowDao.list(qryActGoodsFlow);
                if (actGoodsFlows.size() >= actTask.getAllTotalNum()) {
                    throw ActivityBizException.ACT_TASK_HAS_DONE;
                }
            }
        }

        Date now = new Date();
        Date beginOfDay = DateUtil.beginOfDay(now);
        Date endOfDay = DateUtil.endOfDay(now);

        //今天是否已经完成任务
        ActGoodsFlow goodsFlow = new ActGoodsFlow();
        goodsFlow.setMobile(req.getMobile());
        goodsFlow.setActivityCode(req.getActivityCode());
        goodsFlow.setTaskCode(req.getTaskCode());

        goodsFlow.setCreateTimeStart(beginOfDay);
        goodsFlow.setCreateTimeEnd(endOfDay);
        List<ActGoodsFlow> list = actGoodsFlowDao.list(goodsFlow);
        if (!CollectionUtils.isEmpty(list)) {
            if (Double11Biz.ACTIVITY_2023DOUBLE11_ZJF.equals(req.getActivityCode())) {
                if (taskMap == null) {
                    taskMap = taskPoolBiz.getActTaskMap();
                }
                ActTask actTask = taskMap.get(req.getTaskCode());
                if (actTask == null) {
                    throw BizException.COMMON_PARAMS_NOT_NULL.format("任务不存在");
                }
                if (actTask.getTaskType() == 1 && actTask.getTotalNum() != null
                        && actTask.getTotalNum() > 0 && list.size() >= actTask.getTotalNum()) {
                    throw ActivityBizException.ACT_TASK_TODAY_HAS_DONE;
                }
            } else {
                statusDto.setStatus(CodeConstant.CODE_YES).setBizCode(list.get(0).getBizCode()).setTime(list.get(0).getCreateTime());
                throw ActivityBizException.ACT_TASK_TODAY_HAS_DONE;
            }

        }

        // 此业务编码是否已经参与任务
        if (com.wlzq.common.utils.ObjectUtils.isNotEmptyOrNull(req.getBizCode())) {
            ActGoodsFlow goodsFlow1 = new ActGoodsFlow();
            goodsFlow1.setMobile(req.getMobile());
            goodsFlow1.setActivityCode(req.getActivityCode());
            goodsFlow1.setTaskCode(req.getTaskCode());
            goodsFlow1.setBizCode(req.getBizCode());
            ActGoodsFlow actGoodsFlow1 = actGoodsFlowDao.get(goodsFlow1);
            if (actGoodsFlow1 != null) {
                statusDto.setStatus(CodeConstant.CODE_NO).setBizCode(req.getBizCode()).setTime(actGoodsFlow1.getCreateTime());
                throw ActivityBizException.ACT_TASK_BIZ_CODE_HAS_USED;
            }
        }
        statusDto.setStatus(CodeConstant.CODE_NO);
        return new StatusObjDto<>(true, statusDto, StatusDto.SUCCESS, "");
    }


    /**
     * 完成任务（已手机号为做任务的主体）
     *
     * @param req
     * @return
     */
    @Transactional
    public StatusObjDto<ActTaskStatusDto> doTask(ActTaskReqDto req) {
        String activityCode = req.getActivityCode();
        String taskCode = req.getTaskCode();
        String mobile = req.getMobile();
        String customerId = req.getCustomerId();
        ActTaskStatusDto statusDto = new ActTaskStatusDto();

        Date now = new Date();
        Date beginOfDay = DateUtil.beginOfDay(now);
        Date endOfDay = DateUtil.endOfDay(now);

        //活动验证
        StatusDto valid = activityBaseBiz.isValid(activityCode);
        if (!valid.isOk()) {
            return new StatusObjDto<>(false, statusDto, valid.getCode(), valid.getMsg());
        }

        //查询任务是否存在可用
        ActTask actTask = new ActTask();
        actTask.setActivityCode(activityCode);
        actTask.setCode(taskCode);
        actTask.setStatus(CodeConstant.CODE_YES);
        actTask = actTaskDao.get(actTask);
        if (actTask == null) {
            return new StatusObjDto<>(false, statusDto, StatusDto.FAIL_COMMON, "活动任务不存在");
        }

        String curCustomerId = null;

        // 特殊规则限制: 例如一个客户号最多只能用两个手机号参与活动
        if (activityCode.equals(ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY)
                || activityCode.equals(ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY_PZB)
                || activityCode.equals(ActivityBiz.ACTIVITY_GGQZD)) {
            // 获取当前参与任务的客户号, 并且验证有没有完成任务的权限
            ITaskBaseBiz taskBaseBiz = activityBiz.getTaskBizImplByActivityCode(activityCode);
            curCustomerId = taskBaseBiz.getCustomerIdAndCheckDoTaskAuth(req, activityCode, taskCode, mobile, actTask);
        }

        //判断当前登录手机号，是否已完成任务的次数限制；
        List<ActGoodsFlow> actGoodsFlows = Lists.newArrayList();
        if (Objects.equals(actTask.getTaskType(), ActTask.DAILY_TASK)) {
            ActGoodsFlow goodsFlow = new ActGoodsFlow();
            goodsFlow.setActivityCode(activityCode);
            goodsFlow.setTaskCode(taskCode);
            goodsFlow.setMobile(mobile);
            goodsFlow.setCreateTimeStart(beginOfDay);
            goodsFlow.setCreateTimeEnd(endOfDay);
            // 获取今天的任务完成流水
            actGoodsFlows = actGoodsFlowDao.list(goodsFlow);
            if (CollectionUtil.isNotEmpty(actGoodsFlows) && actGoodsFlows.size() >= actTask.getTotalNum()) {
                throw ActivityBizException.ACT_TASK_TODAY_HAS_DONE;
            }
        } else if (Objects.equals(actTask.getTaskType(), ActTask.ONCE_TASK)) {
            ActGoodsFlow goodsFlow = new ActGoodsFlow();
            goodsFlow.setActivityCode(activityCode);
            goodsFlow.setTaskCode(taskCode);
            goodsFlow.setMobile(mobile);
            actGoodsFlows = actGoodsFlowDao.list(goodsFlow);
            if (!CollectionUtils.isEmpty(actGoodsFlows) && actGoodsFlows.size() >= actTask.getTotalNum()) {
                throw ActivityBizException.ACT_TASK_HAS_DONE;
            }
        }

        // 判断当前登录手机号 + bizCode，指定的 bizCode 是否已用于完成历史任务；
        if (StringUtils.isNotBlank(req.getBizCode())) {
            ActGoodsFlow goodsFlow1 = new ActGoodsFlow();
            goodsFlow1.setActivityCode(activityCode);
            goodsFlow1.setTaskCode(taskCode);
            goodsFlow1.setMobile(mobile);
            goodsFlow1.setBizCode(req.getBizCode());
            List<ActGoodsFlow> actGoodsFlows1 = actGoodsFlowDao.list(goodsFlow1);
            if (!CollectionUtils.isEmpty(actGoodsFlows1)) {
                throw ActivityBizException.ACT_TASK_BIZ_CODE_HAS_USED;
            }
        }

        /* 检查任务是否完成, 特定于某些任务 */
        checkTaskIsDone(actTask, req.getUserId(), actGoodsFlows);

        // 获取任务奖品数量, 例如12连刮视频月卡活动需要每累计5次, 多赠送一次抽奖机会
        Double goodsQuantity = calculateGoodsQuantity(actTask, mobile);

        ActGoodsFlow newGoodsFlow = new ActGoodsFlow();
        newGoodsFlow.setMobile(mobile)
                .setCustomerId(curCustomerId != null ? curCustomerId : customerId)
                .setUserId(req.getUserId())
                .setActivityCode(activityCode).setTaskCode(taskCode)
                .setBizCode(req.getBizCode()).setCreateTime(now).setUpdateTime(now)
                .setFlag(ActGoodsFlow.FLOW_FLAG_GET).setGoodsCode(actTask.getGoodsCode())
                .setGoodsQuantity(goodsQuantity);
        actGoodsFlowDao.insert(newGoodsFlow);
        statusDto.setStatus(CodeConstant.CODE_YES);
        statusDto.setTimes(goodsQuantity.intValue());

        // 如果是奖励积分的任务，需要给用户增加积分
        if (actTask.getGoodsCode().equals(GOODS_CODE_ACT_POINT)) {
            pointBiz.addPoint(req.getUserId(), actTask.getGoodsQuantity().longValue(), PointRecord.SOURCE_TASK, PointRecord.FLOW_ADD,
                    StringUtils.isEmpty(req.getDescription()) ? actTask.getName() : req.getDescription(), activityCode);
        }
        return new StatusObjDto<>(true, statusDto, StatusDto.SUCCESS, "");
    }

    private Double calculateGoodsQuantity(ActTask actTask, String mobile) {
        if (TASK_GGQZD.equals(actTask.getCode())) {
            ActGoodsFlow actGoodsFlowQry = new ActGoodsFlow();
            actGoodsFlowQry.setActivityCode(actTask.getActivityCode());
            actGoodsFlowQry.setTaskCode(actTask.getCode());
            actGoodsFlowQry.setMobile(mobile);
            List<ActGoodsFlow> actGoodsFlowList = actGoodsFlowDao.list(actGoodsFlowQry);
            if (CollectionUtil.isNotEmpty(actGoodsFlowList)) {
                int taskNum = actGoodsFlowList.size();
                if (taskNum % 5 == 4) {
                    return actTask.getGoodsQuantity() + 1;
                }
            }
        }
        return actTask.getGoodsQuantity();
    }

    /**
     * 检查任务是否完成
     */
    private void checkTaskIsDone(ActTask actTask, String userId, List<ActGoodsFlow> actGoodsFlows) {
        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("userId", userId);
        if (actTask.getTaskType().equals(ActTask.DAILY_TASK)) {
            busparams.put("startTime", LocalDateTimeUtil.format(LocalDateTime.MIN, DatePattern.NORM_DATETIME_PATTERN));
            busparams.put("endTime", LocalDateTimeUtil.format(LocalDateTime.MAX, DatePattern.NORM_DATETIME_PATTERN));
        } else {
            Activity activity = findActivity(actTask.getActivityCode());
            busparams.put("startTime", DateUtil.format(activity.getDateFrom(), DatePattern.NORM_DATETIME_PATTERN));
            busparams.put("endTime", DateUtil.format(activity.getDateTo(), DatePattern.NORM_DATETIME_PATTERN));
        }

        ResultDto resultDto;
        switch (actTask.getCode()) {
            // case TASK_ACT_VIEW_ARTICLES_COMMENT:
            //     resultDto = RemoteUtils.call("service.articlecooperation.countarticlecomment", ApiServiceTypeEnum.COOPERATION, busparams, true);
            //     break;
            case TASK_ACT_VIEW_INVEST_BUY:
                busparams.put("adviserProductType", 2);
                resultDto = RemoteUtils.call("service.productcooperation.countadviserorder", ApiServiceTypeEnum.COOPERATION, busparams, true);
                break;
            // case TASK_ACT_VIEW_INVEST_CARE:
            //     resultDto = RemoteUtils.call("service.advisercooperation.countadviserfollow", ApiServiceTypeEnum.COOPERATION, busparams, true);
            //     break;
            default:
                return;
        }

        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("count");
            if (count <= actGoodsFlows.size()) {
                throw ActivityBizException.ACT_TASK_HAS_NOT_DONE;
            }
        }
    }

    /**
     * 任务列表查询
     */
    public StatusObjDto<List<ActTask>> taskList(ActTaskReqDto req) {
        // 查任务列表
        ActTask entity = new ActTask();
        entity.setActivityCode(req.getActivityCode());
        List<ActTask> list = actTaskDao.findList(entity);
        if (list.isEmpty()) {
            throw ActivityBizException.ACT_TASK_NOT_SETTING;
        }

        // 对每个任务设置完成状态
        if (req.getUserId() != null) {
            for (ActTask actTask : list) {
                ActGoodsFlow goodsFlow = new ActGoodsFlow();
                goodsFlow.setActivityCode(req.getActivityCode());
                goodsFlow.setTaskCode(actTask.getCode());
                goodsFlow.setMobile(req.getMobile());
                if (Objects.equals(actTask.getTaskType(), ActTask.DAILY_TASK)) {
                    goodsFlow.setCreateTimeStart(DateUtil.beginOfDay(new Date()));
                    goodsFlow.setCreateTimeEnd(DateUtil.endOfDay(new Date()));
                }
                List<ActGoodsFlow> goodsFlowList = actGoodsFlowDao.list(goodsFlow);
                actTask.setCompleteNum(goodsFlowList.size());
            }
        } else {
            /* 未登录的状态设置完成数为0 */
            list.forEach(item -> item.setCompleteNum(0));
        }

        return new StatusObjDto<>(true, list, StatusDto.SUCCESS, "");
    }

}
