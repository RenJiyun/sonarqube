package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.task.dto.ActTaskReqDto;
import com.wlzq.activity.task.dto.ActTaskStatusResDto;
import com.wlzq.activity.task.dto.PushTimeDto;
import com.wlzq.activity.task.dto.TaskPushDto;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luohc
 * @date 2023/4/14 14:14
 */
@Service
public class FreeCourseBizImpl {

    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private ActPrizeDao prizeDao;

    public static final String ACTIVITY_CODE = "ACTIVITY.FREECOURSE.DIGGING.CYCLE.STOCKS";
    public static final String ACTIVITY_CODE_GET_REDPACK = "ACTIVITY.FREECOURSE.DIGGING.CYCLE.REDPACK";

    public static final String PRIZE_TYPE_CODE_RED_PACKAGE = "PRIZE.REDB.FREECOURSE.29.9";
    public static final String PRIZE_TYPE_CODE_400_VOUCH = "COUPON.VAS.CYJJ.VOUCHER400";
    public static final String PRIZE_TYPE_CODE_CYJJ_FREE_7DAY = "COUPON.VAS.CYJJ.FREE7.DAY";

    public static final String BUY_COURSE_DAY = "2023-05-21";
    public static final String PRODUCT_CODE = "KCCP006";


    /**
     * 查询任务进度
     * @param req
     * @return
     */
    public StatusObjDto<Object> queryTaskInfo(ActTaskReqDto req) {
        ActTaskStatusResDto statusDto = new ActTaskStatusResDto();
        statusDto.setCounter(0);
        statusDto.setStatus(CodeConstant.CODE_NO);
        statusDto.setRedPackStatus(ActTaskStatusResDto.STATUS_NOT_FINISH_TASK);

        //活动验证
        Activity act = activityBaseBiz.findActivity(req.getActivityCode());
        StatusDto valid = activityBaseBiz.isValid(act);
        if(!valid.isOk()) {
            Activity act1 = activityBaseBiz.findActivity(ACTIVITY_CODE_GET_REDPACK);
            StatusDto valid1 = activityBaseBiz.isValid(act1);
            if (!valid1.isOk()) {
                //如果主活动，和领取红包活动都过期了，才直接返回
                return new StatusObjDto<>(false, statusDto, valid.getCode(), valid.getMsg());
            }
        }


        //查询是否2023-05-21前购买课程
        String remark = act.getRemark();
        Date payTimeEndDate;
        String productCode;
        if (StringUtils.isBlank(remark)) {
            payTimeEndDate = DateUtil.parseDate(BUY_COURSE_DAY);
            productCode = PRODUCT_CODE;
        }else{
            payTimeEndDate = DateUtil.parseDate(remark.split("\\|")[0]);
            productCode = remark.split("\\|")[1];
        }
        boolean buyFlag = isBuyCourseInTime(req.getUserId(),req.getCustomerId(), productCode,payTimeEndDate,statusDto);
        if (CodeConstant.CODE_YES.equals(statusDto.getStatus())) {
            //已买课程
            //查询红包领取状态
            boolean match = hasReceiveRedPackage(req,statusDto);
            if (match) {
                statusDto.setCounter(ActTaskStatusResDto.FINISH_COURSE_COUNTER);
                statusDto.setStatus(CodeConstant.CODE_YES);
                statusDto.setRedPackStatus(ActTaskStatusResDto.STATUS_RECEIVED);
                return new StatusObjDto<>(true, statusDto, StatusDto.SUCCESS, "");
            }
        }else{
            //从来没有购买过
            return new StatusObjDto<>(true, statusDto, StatusDto.SUCCESS, "");
        }


        //查询课程学习进度
        Integer count = countFinishedLearn(req.getUserId(),req.getCustomerId(),  act,productCode);
        statusDto.setCounter(count);
        if (count >= ActTaskStatusResDto.FINISH_COURSE_COUNTER) {
            statusDto.setRedPackStatus(ActTaskStatusResDto.STATUS_WAITING_RECEIVE);
        }
        return new StatusObjDto<>(true, statusDto, StatusDto.SUCCESS, "");
    }


    public Integer countFinishedLearn(String userId, String customerId, Activity act,String productCode) {
        if (StringUtils.isBlank(customerId)) {
            return 0;
        }
        Map<String, Object> pMap1 = new HashMap<>();
        pMap1.put("userId", userId);
        pMap1.put("customerId", customerId);
        pMap1.put("productCode", productCode);
        pMap1.put("startTime",DateUtil.format(act.getDateFrom(), "yyyy-MM-dd HH:mm:ss"));
        pMap1.put("endTime",DateUtil.format(act.getDateTo(), "yyyy-MM-dd HH:mm:ss"));
        ResultDto resultDto1 = RemoteUtils.call("service.coursecooperation.countfinishcourseprogress", ApiServiceTypeEnum.COOPERATION, pMap1, true);
        Integer count = 0;
        if (ResultDto.SUCCESS.equals(resultDto1.getCode())) {
            count = (Integer) resultDto1.getData().get("count");
        }
        return count;
    }

    /**
     * 是否在5-21号前购买课程
     */
    public boolean isBuyCourseInTime(String userId, String customerId, String productCode, Date payTimeEndDate, ActTaskStatusResDto statusDto) {
        if (StringUtils.isBlank(customerId)) {
            return false;
        }
        Map<String, Object> pMap = new HashMap<>();
        pMap.put("userId", userId);
        pMap.put("customerId", customerId);
        pMap.put("productCode",productCode);
        ResultDto resultDto = RemoteUtils.call("service.coursecooperation.paidorders", ApiServiceTypeEnum.COOPERATION, pMap, true);
        boolean buyFlag = false;
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("total");
            if (count >= 1) {
                List<Map<String,Object>> info = (List<Map<String, Object>>) resultDto.getData().get("info");
                if (!CollectionUtils.isEmpty(info)) {
                    Date createTime = new Date((Long) info.get(0).get("createTime"));
                    if (statusDto != null) {
                        statusDto.setTime(createTime);
                        statusDto.setStatus(1);
                    }
                    if (createTime.getTime() <= DateUtil.endOfDay(payTimeEndDate).getTime()) {
                         buyFlag = true;
                    }
                }
            }
        }
        return buyFlag;
    }


    public boolean hasReceiveRedPackage(ActTaskReqDto req, ActTaskStatusResDto statusDto) {
        String customerId = req.getCustomerId();
        if (StringUtils.isBlank(customerId)) {
            return false;
        }
        List<String> activityCodes = new ArrayList<>();
        activityCodes.add(ACTIVITY_CODE);
        activityCodes.add(ACTIVITY_CODE_GET_REDPACK);
        ActPrize prize = new ActPrize();
        prize.setActivityCodes(activityCodes);
        prize.setCustomerId(customerId);
        List<ActPrize> prizes = prizeDao.findUserPrizes(prize);

        boolean match = false;
        List<String> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(prizes)) {
            for (ActPrize actPrize : prizes) {
                if (Objects.equals(actPrize.getCode(),PRIZE_TYPE_CODE_RED_PACKAGE)) {
                    match = true;
                }else if (Objects.equals(actPrize.getCode(),PRIZE_TYPE_CODE_CYJJ_FREE_7DAY)) {
                    list.add(PRIZE_TYPE_CODE_CYJJ_FREE_7DAY);
                }else if (Objects.equals(actPrize.getCode(),PRIZE_TYPE_CODE_400_VOUCH)) {
                    list.add(PRIZE_TYPE_CODE_400_VOUCH);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                statusDto.setReceiveCouponCodes(list);
            }
        }
        return match;
    }


    public StatusDto checkBeforeReceive(String userId, String customerId, Activity act){
        String remark = act.getRemark();
        Date payTimeEndDate;
        String productCode;
        if (StringUtils.isBlank(remark)) {
            payTimeEndDate = DateUtil.parseDate(BUY_COURSE_DAY);
            productCode = PRODUCT_CODE;
        }else{
            payTimeEndDate = DateUtil.parseDate(remark.split("\\|")[0]);
            productCode = remark.split("\\|")[1];
        }
        boolean buyCourse = isBuyCourseInTime(userId, customerId, productCode,payTimeEndDate, null);
        if (!buyCourse) {
            return new StatusDto(false,"购买课程任务未完成");
        }

        Integer counter = countFinishedLearn(userId, customerId,act, productCode);
        if (counter<ActTaskStatusResDto.FINISH_COURSE_COUNTER) {
            return new StatusDto(false,"学习课程任务未完成");
        }
        return new StatusDto(true);
    }


    public StatusObjDto<List<TaskPushDto>> getWaitingReceiveRedPack(PushTimeDto timeDto) {
        Activity act = activityBaseBiz.findActivity(ACTIVITY_CODE);
        StatusDto valid = activityBaseBiz.isValid(act);
        List<TaskPushDto> taskPushDtos = new ArrayList<>();
        if(!valid.isOk()) {
            return new StatusObjDto<>(true, taskPushDtos, StatusDto.SUCCESS, "活动已过期");
        }

        Date payTimeEndDate = DateUtil.parseDate(StringUtils.isBlank(timeDto.getPayTimeEnd())? BUY_COURSE_DAY: timeDto.getPayTimeEnd());
        String productCode = StringUtils.isBlank(timeDto.getProductCode())? PRODUCT_CODE: timeDto.getProductCode();

        //查询购买的订单列表
        Map<String, Object> pMap = new HashMap<>();
        pMap.put("productCode", productCode);
        pMap.put("payTimeEnd",DateUtil.endOfDay(payTimeEndDate).getTime());
        ResultDto resultDto = RemoteUtils.call("service.coursecooperation.paidorders", ApiServiceTypeEnum.COOPERATION, pMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            Integer count = (Integer) resultDto.getData().get("total");
            if (count >= 1) {
                List<Map<String,Object>> info = (List<Map<String, Object>>) resultDto.getData().get("info");
                if (CollectionUtils.isEmpty(info)) {
                    return new StatusObjDto<>(true, taskPushDtos, StatusDto.SUCCESS, "购买的订单数据为空");
                }
                for (Map<String, Object> map : info) {
                    String customerId = (String)map.get("customerId");
                    String mobile = (String) map.get("mobile");

                    ActPrize prize = new ActPrize();
                    prize.setActivityCode(ACTIVITY_CODE_GET_REDPACK);
                    prize.setCode(PRIZE_TYPE_CODE_RED_PACKAGE);
                    prize.setCustomerId(customerId);
                    List<ActPrize> prizes = prizeDao.findUserPrizes(prize);
                    if (!CollectionUtils.isEmpty(prizes)) {
                        //已领取红包，不推送
                        continue;
                    }


                    Integer counter = countFinishedLearn(null, customerId,act, productCode);
                    if (counter<ActTaskStatusResDto.FINISH_COURSE_COUNTER) {
                        //还没学习完，不推送
                        continue;
                    }

                    TaskPushDto pushDto = new TaskPushDto();
                    taskPushDtos.add(pushDto);
                    pushDto.setMobile(mobile);
                    pushDto.setCustomerId(customerId);
                    pushDto.setSendToKey(PRIZE_TYPE_CODE_RED_PACKAGE+customerId);
                }
            }
        }
        return new StatusObjDto<>(true, taskPushDtos, StatusDto.SUCCESS, "");
    }


}
