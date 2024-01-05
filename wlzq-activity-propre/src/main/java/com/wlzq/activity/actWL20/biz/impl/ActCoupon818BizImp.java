package com.wlzq.activity.actWL20.biz.impl;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.actWL20.biz.ActCoupon818Biz;
import com.wlzq.activity.actWL20.biz.FundinGoBiz;
import com.wlzq.activity.actWL20.dao.ActCoupon818Dao;
import com.wlzq.activity.actWL20.dto.ActSubscribeDto;
import com.wlzq.activity.actWL20.model.ActFundinGo;
import com.wlzq.activity.actWL20.model.ActSubscribe;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.base.redis.BaseRedis;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author renjiyun
 */
@Service
public class ActCoupon818BizImp extends ActivityBaseBiz implements ActCoupon818Biz {

    private Logger logger = LoggerFactory.getLogger(ActCoupon818BizImp.class);

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private ActCoupon818Dao actCoupon818Dao;

    @Autowired
    private CouponCommonReceiveBiz couponRecieveBiz;

    @Autowired
    private FundinGoBiz fundinGoBiz;

    @Override
    public StatusObjDto<CouponRecieveStatusDto> recieve(String activityCode, String prizeType, String userId, String openId, Customer customer, String recommendCode) {

        //检查用户合法性
        StatusDto isValid = isValid(customer, activityCode);
        if (!isValid.isOk()) {
            throw ActivityBizException.ACT_CUS_NOT_VALID;
        }

        StatusObjDto<CouponRecieveStatusDto> result = couponRecieveBiz.receive(activityCode, prizeType, userId, openId, customer.getCustomerId(), recommendCode, null);
        if (!result.isOk()) {
            return new StatusObjDto<CouponRecieveStatusDto>(false, result.getCode(), result.getMsg());
        }
        return result;
    }

    @Override
    public int addFundinGo(Customer customer) {
        if (ObjectUtils.isEmptyOrNull(customer)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("customer");
        }
        ActFundinGo fundinGo = actCoupon818Dao.findFundinGo(customer.getCustomerId());

        if (!ObjectUtils.isEmptyOrNull(fundinGo)) {
            throw ActivityBizException.ACT_RJMONEY_ADD_OK;
        }

        return actCoupon818Dao.addFundinGo(customer);
    }

    public StatusDto isValid(Customer customer, String activityCode) {
        //判断是否2019年1月1日之后开户
        if (!isValidOpenDate(customer)) {
            throw ActivityBizException.ACT_CUS_NOT_VALID;
        }

        Activity act = findActivity(activityCode);

        //首次入金是否>=1000元
        ResultDto resultDto = fundinGoBiz.historyRJ(customer.getCustomerId());
        if (!ResultDto.SUCCESS.equals(resultDto.getCode()) || resultDto.getData() == null || resultDto.getData().isEmpty() || resultDto.getData().get("O_RESULT") == null) {
            throw ActivityBizException.ACT_CUS_NOT_VALID;
        }

        List<Map<String, Object>> result = (List<Map<String, Object>>) resultDto.getData().get("O_RESULT");

        if (result == null || result.size() == 0) {
            throw ActivityBizException.ACT_CUS_NOT_FOUND;
        }

        for (Map<String, Object> map : result) {
            //首次入金日期<20210809则不符合条件
            if (map.containsKey("FIRST_FUNDIN_DT")) {
                String firstFundinDt = (String) map.get("FIRST_FUNDIN_DT") + " 00:00:00";
                Date firstDate = DateUtils.parseDate(firstFundinDt, "yyyyMMdd HH:mm:ss");
                Date checkDate = DateUtils.parseDate(AppConfigUtils.get("ACT818_FIRST_FUNDIN_TIME", "2021-08-09") + " 00:00:00", "yyyy-MM-dd HH:mm:ss");

                if (firstDate.getTime() < checkDate.getTime()) {
                    throw ActivityBizException.ACT_CUS_NOT_VALID;
                }

            }

            //累计入金金额<1000则不符合条件
            if (map.containsKey("TOTAL_FUNDIN_AMT")) {
                double totalFundinAmt = Double.valueOf(map.get("TOTAL_FUNDIN_AMT").toString());
                if (totalFundinAmt - 1000 < 0) {
                    throw ActivityBizException.ACT_RJMONEY_NOT_VALID;
                }
            }

            //最近入金日期必须再活动日期内
            if (map.containsKey("LAST_FUNDIN_DT")) {
                String lastFundinDt = (String) map.get("LAST_FUNDIN_DT") + " 00:00:00";
                Date lastDate = DateUtils.parseDate(lastFundinDt, "yyyyMMdd HH:mm:ss");

                if (lastDate.getTime() < act.getDateFrom().getTime() || lastDate.getTime() > act.getDateTo().getTime()) {
                    throw ActivityBizException.ACT_CUS_NOT_VALID;
                }

            }
        }

        return new StatusDto(true, 0, "");
    }

    private boolean isValidOpenDate(Customer customer) {
        if (ObjectUtils.isEmptyOrNull(customer.getOpenDate())) {
            return false;
        }

        Date openDate = customer.getOpenDate();
        Date validDate = DateUtils.parseDate(AppConfigUtils.get("ACT818_OPEN_DATE", "2019-01-01") + " 00:00:00", "yyyy-MM-dd HH:mm:ss");

        if (openDate.getTime() < validDate.getTime()) {
            return false;
        }

        return true;
    }

    public Activity findActivity(String activityCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return null;
        }

        Activity act = (Activity) BaseRedis.ACT_ACTIVITY_INFO.get(activityCode);
        if (act != null) {
            return act;
        }

        act = activityDao.findActivityByCode(activityCode);
        if (act != null) {
            BaseRedis.ACT_ACTIVITY_INFO.set(activityCode, act);
        }

        return act;
    }

    @Override
    public int subscribe(String activityCode, String mobile, String customerId) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }

        ActSubscribe actSubscribe = actCoupon818Dao.findSubscribe(activityCode, mobile);
        if (!ObjectUtils.isEmptyOrNull(actSubscribe)) {
            throw ActivityBizException.ACT_SUBSCRIBE_OK;
        }

        return actCoupon818Dao.addSubscribe(activityCode, mobile, customerId);
    }

    @Override
    public StatusObjDto<ActSubscribeDto> subscribeCheck(String activityCode, String mobile, String customerId) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }
        ActSubscribe actSubscribe = actCoupon818Dao.findSubscribe(activityCode, mobile);
        ActSubscribeDto dto = new ActSubscribeDto();
        dto.setStatus(ObjectUtils.isEmptyOrNull(actSubscribe) ? 0 : 1);

        return new StatusObjDto<ActSubscribeDto>(true, dto, StatusDto.SUCCESS, ObjectUtils.isEmptyOrNull(actSubscribe) ? "未订阅" : "已订阅");
    }

}
