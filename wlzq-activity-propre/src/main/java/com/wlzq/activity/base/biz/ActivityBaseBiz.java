package com.wlzq.activity.base.biz;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.dao.ActAppointmentDao;
import com.wlzq.activity.base.dao.ActPrizeTypeDao;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.dao.ParticipateDao;
import com.wlzq.activity.base.dto.ActivityDto;
import com.wlzq.activity.base.dto.AppointmentDto;
import com.wlzq.activity.base.dto.RecommendDto;
import com.wlzq.activity.base.model.*;
import com.wlzq.activity.base.redis.BaseRedis;
import com.wlzq.activity.redeem.dao.RedeemDao;
import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccAccountOpeninfo;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.*;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.FsdpBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.wlzq.activity.ActivityBizException.ACT_ACCOUNT_EXCEP;

/**
 * 活动基础业务类
 *
 * @author
 * @version 1.0
 */
@Service
public class ActivityBaseBiz {
    private Logger logger = LoggerFactory.getLogger(ActivityBaseBiz.class);
    private static final String LEVEL2_LOTTERY = "ACTIVITY.LOTTERY";
    private static final String LEVEL2_REDEEM = "ACTIVITY.REDEEM";


    @Autowired
    private ActivityDao activityDao;
    @Autowired
    private RedeemDao redeemDao;
    @Autowired
    private ParticipateDao participateDao;
    @Autowired
    private ActAppointmentDao actAppointmentDao;
    @Autowired
    private ActPrizeTypeDao actPrizeTypeDao;
    @Autowired
    private FsdpBiz fsdpBiz;


    public StatusDto isValid(String activityCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return new StatusDto(false, 201, "activityCode参数不能为空");
        }

        Activity act = findActivity(activityCode);

        return isValid(act);
    }

    /**
     * 校验活动是否有效
     */
    public StatusDto isValid(Activity act) {
        if (act == null) {
            return new StatusDto(false, 208, "活动不存在");
        }
        if (!isActivityTime(act)) {
            return new StatusDto(false, 219, "不在活动时间");
        }
        if (!act.getStatus().equals(Activity.STATUS_OPEN)) {
            return new StatusDto(false, 209, "系统维护中，稍后再试");
        }
        if (isBreakerTime(act) && act.getBreakerStatus().equals(Activity.BREAKER_STATUS_YES)) {
            return new StatusDto(false, 209, "系统维护中，稍后再试");
        }
        Long now = new Date().getTime();
        if (now < act.getDateFrom().getTime() || now > act.getDateTo().getTime()) {
            return new StatusDto(false, 210, "不在活动时间");
        }

        return new StatusDto(true, 0, "");
    }

    public StatusDto status(String activityCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return new StatusDto(false, 201, "activityCode参数不能为空");
        }
        Activity act = activityDao.findActivityByCode(activityCode);
        if (act == null) {
            return new StatusDto(false, 208, "活动不存在");
        }
        if (!isActivityTime(act)) {
            return new StatusDto(false, 219, "不在活动时间");
        }
        return new StatusDto(true, 0, "");
    }

    public StatusObjDto<ActivityDto> info(String activityCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return new StatusObjDto<ActivityDto>(false, 201, "activityCode参数不能为空");
        }
        Activity act = activityDao.findActivityByCode(activityCode);
        if (act == null) {
            return new StatusObjDto<ActivityDto>(false, 208, "活动不存在");
        }
        ActivityDto actDto = new ActivityDto();
        BeanUtils.copyProperties(act, actDto);
        if (!isActivityTime(act) ||
                (isBreakerTime(act) && act.getBreakerStatus().equals(Activity.BREAKER_STATUS_YES))) {
            actDto.setStatus(0);
        }

        return new StatusObjDto<ActivityDto>(true, actDto, 0, "");
    }

    public Activity findActivity(String activityCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return null;
        }

        Activity act = (Activity) BaseRedis.ACT_ACTIVITY_INFO.get(activityCode);
        if (act != null) return act;

        act = activityDao.findActivityByCode(activityCode);
        if (act != null) {
            BaseRedis.ACT_ACTIVITY_INFO.set(activityCode, act);
        }

        return act;
    }

    /**
     * @param prizeType 奖品编码
     * @return 奖品类型信息
     */
    public ActPrizeType findPrizeType(@NotNull String prizeType) {
        ActPrizeType info = (ActPrizeType) BaseRedis.ACT_PRIZETYPE_INFO.get(prizeType);

        if (info == null) {
            info = actPrizeTypeDao.findByCode(prizeType);

            if (info == null) {
                throw ActivityBizException.ACT_PRIZE_TYPE_NOT_EXIST;
            }
        }

        BaseRedis.ACT_PRIZETYPE_INFO.set(prizeType, info);

        return info;
    }

    public StatusObjDto<List<String>> findCustomers(String activityCode, String recommendMobile, Integer start, Integer end) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return new StatusObjDto<List<String>>(false, 201, "activityCode参数不能为空");
        }
        Activity act = activityDao.findActivityByCode(activityCode);
        if (act == null) {
            return new StatusObjDto<List<String>>(false, 208, "活动不存在");
        }

        if (ObjectUtils.isEmptyOrNull(recommendMobile)) {
            return new StatusObjDto<List<String>>(false, 201, "mobile参数不能为空");
        }
        if (!RegxUtils.isMobile(recommendMobile)) {
            return new StatusObjDto<List<String>>(false, 202, "请输入正确的手机号");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("recommendMobile", recommendMobile);
        params.put("activityCode", activityCode);
        params.put("start", start);
        params.put("end", end);
        //List<String> customers = level2PrizeDao.findCustomers(params);
        List<String> customers = Lists.newArrayList();
        return new StatusObjDto<List<String>>(true, customers, 0, "");
    }


    public StatusObjDto<RecommendDto> findRecommender(String activityCode, String mobile) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return new StatusObjDto<RecommendDto>(false, 201, "activityCode参数不能为空");
        }
        Activity act = activityDao.findActivityByCode(activityCode);
        if (act == null) {
            return new StatusObjDto<RecommendDto>(false, 208, "活动不存在");
        }

        if (ObjectUtils.isEmptyOrNull(mobile)) {
            return new StatusObjDto<RecommendDto>(false, 201, "mobile参数不能为空");
        }
        if (!RegxUtils.isMobile(mobile)) {
            return new StatusObjDto<RecommendDto>(false, 202, "请输入正确的手机号");
        }

        //		RecommendDto recommend = null;
        //		Map<String,Object> params = new HashMap<String,Object>();
        //		params.put("mobile", mobile);
        //		params.put("activityCode", activityCode);
        //
        //		if(activityCode.equals(LEVEL2_LOTTERY)) {
        //			Level2Prize prize = level2PrizeDao.findByMobile(params);
        //			if(prize == null) {
        //				return  new StatusObjDto<RecommendDto>(false, 1,"信息不存在");
        //			}
        //			recommend = getRecommenderFromLottery(prize);
        //		}else if(activityCode.equals(LEVEL2_REDEEM)) {
        //			Redeem redeem = redeemDao.findByMobile(params);
        //			if(redeem == null) {
        //				return  new StatusObjDto<RecommendDto>(false, 1,"信息不存在");
        //			}
        //			recommend = getRecommenderFromRedeem(redeem);
        //		}
        RecommendDto recommend = new RecommendDto();
        return new StatusObjDto<RecommendDto>(true, recommend, 0, "");
    }


    public boolean isActivityTime(String activityCode, Date date) {
        Activity activity = activityDao.findActivityByCode(activityCode);
        if (activity == null) {
            return false;
        }

        if (ObjectUtils.isEmptyOrNull(activity.getDateFrom()) ||
                ObjectUtils.isEmptyOrNull(activity.getDateTo())) {
            return true;
        }
        Date from = activity.getDateFrom();
        Date to = activity.getDateTo();
        long nowl = date.getTime();
        if ((from != null && nowl < from.getTime()) ||
                to != null && nowl > to.getTime()) {
            return false;
        }

        if (ObjectUtils.isEmptyOrNull(activity.getTimeFrom()) ||
                ObjectUtils.isEmptyOrNull(activity.getTimeTo())) {
            return true;
        }
        Integer timeFrom = getSecondsFromTimeStr(activity.getTimeFrom());
        Integer timeTo = getSecondsFromTimeStr(activity.getTimeTo());
        if (timeFrom == null || timeTo == null) {
            return true;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);//小时
        int minute = cal.get(Calendar.MINUTE);//分
        int second = cal.get(Calendar.SECOND);//秒
        Integer nows = hour * 3600 + minute * 60 + second;

        if (nows > timeFrom && nows < timeTo) {
            return true;
        }

        return false;
    }

    /**
     * 参与信息保存
     *
     * @param activityCode
     * @param userId
     * @param openId
     * @param method
     * @param ip
     * @param createTime
     */
    public void saveParticipate(String activityCode, String userId, String openId, String method
            , String ip, final Date createTime) {
        Participate participate = new Participate();
        participate.setActivityCode(activityCode);
        participate.setUserId(userId);
        participate.setOpenId(openId);
        participate.setMethod(method);
        participate.setIp(ip);
        participate.setCreateTime(createTime);
        participateDao.insert(participate);
    }

    /**
     * 熔断处理
     *
     * @param activityCode
     */
    public void breakerHandle(String activityCode) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) return;
        Activity act = activityDao.findActivityByCode(activityCode);
        if (act == null) return;
        if (!isBreakerTime(act)) {
            logger.info(activityCode + "不在熔断时间,设置是否熔断为否--------------------------------------");
            if (act.getBreakerStatus().equals(Activity.BREAKER_STATUS_YES)) {
                act.setBreakerStatus(Activity.BREAKER_STATUS_NO);
                activityDao.update(act);
            }
            return;
        }
        Participate participate = new Participate();
        participate.setActivityCode(act.getCode());
        String today = DateUtils.formate(new Date(), "yyyy-MM-dd");
        Date createTimeFrom = DateUtils.parseDate(today + " " + act.getBreakerTimeFrom(), "yyyy-MM-dd HH:mm:ss");
        participate.setCreateTimeFrom(createTimeFrom);
        Date createTimeTo = DateUtils.parseDate(today + " " + act.getBreakerTimeTo(), "yyyy-MM-dd HH:mm:ss");
        participate.setCreateTimeTo(createTimeTo);
        Long partCount = participateDao.findParticipateCount(participate);
        Long breakerNumber = act.getBreakerNumber();
        logger.info(activityCode + "开始熔断处理,熔断值:" + breakerNumber + ",参与人次:" + partCount);
        if (breakerNumber != null && breakerNumber <= partCount &&
                act.getBreakerStatus().equals(Activity.BREAKER_STATUS_NO)) {//修改状态为熔断
            act.setBreakerStatus(Activity.BREAKER_STATUS_YES);
            activityDao.update(act);
            logger.info(activityCode + "熔断成功--------------------------------------");
        }
    }

    private boolean isActivityTime(Activity activity) {
        if (ObjectUtils.isEmptyOrNull(activity.getDateFrom()) ||
                ObjectUtils.isEmptyOrNull(activity.getDateTo())) {
            return true;
        }
        Long nowl = new Date().getTime();
        Date from = activity.getDateFrom();
        Date to = activity.getDateTo();
        if ((from != null && nowl < from.getTime()) ||
                to != null && nowl > to.getTime()) {
            return false;
        }

        if (ObjectUtils.isEmptyOrNull(activity.getTimeFrom()) ||
                ObjectUtils.isEmptyOrNull(activity.getTimeTo())) {
            return true;
        }
        Integer timeFrom = getSecondsFromTimeStr(activity.getTimeFrom());
        Integer timeTo = getSecondsFromTimeStr(activity.getTimeTo());
        if (timeFrom == null || timeTo == null) {
            return true;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);//小时
        int minute = cal.get(Calendar.MINUTE);//分
        int second = cal.get(Calendar.SECOND);//秒
        Integer nows = hour * 3600 + minute * 60 + second;

        if (nows > timeFrom && nows < timeTo) {
            return true;
        }

        return false;
    }


    private boolean isBreakerTime(Activity activity) {
        if (ObjectUtils.isEmptyOrNull(activity.getDateFrom()) ||
                ObjectUtils.isEmptyOrNull(activity.getDateTo())) {
            return false;
        }
        Long nowl = new Date().getTime();
        Date from = activity.getDateFrom();
        Date to = activity.getDateTo();
        if ((from != null && nowl < from.getTime()) ||
                to != null && nowl > to.getTime()) {
            return false;
        }

        if (ObjectUtils.isEmptyOrNull(activity.getBreakerTimeFrom()) ||
                ObjectUtils.isEmptyOrNull(activity.getBreakerTimeTo())) {
            return false;
        }
        Integer timeFrom = getSecondsFromTimeStr(activity.getBreakerTimeFrom());
        Integer timeTo = getSecondsFromTimeStr(activity.getBreakerTimeTo());
        if (timeFrom == null || timeTo == null) {
            return false;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);//小时
        int minute = cal.get(Calendar.MINUTE);//分
        int second = cal.get(Calendar.SECOND);//秒
        Integer nows = hour * 3600 + minute * 60 + second;

        if (nows > timeFrom && nows < timeTo) {
            return true;
        }

        return false;
    }

    private Integer getSecondsFromTimeStr(String time) {
        try {
            if (ObjectUtils.isEmptyOrNull(time)) return null;
            String[] times = time.split(":");
            if (times.length != 3) return null;
            Integer hour = Integer.valueOf(times[0]);
            Integer minutes = Integer.valueOf(times[1]);
            Integer seconds = Integer.valueOf(times[2]);
            return hour * 3600 + minutes * 60 + seconds;
        } catch (Exception ex) {

        }
        return null;
    }

    private RecommendDto getRecommenderFromRedeem(Redeem redeem) {
        RecommendDto recommend = new RecommendDto();
        recommend.setOpenStatus(redeem.getNotityStatus());
        recommend.setGoodsName(redeem.getGoodsName());
        recommend.setGoodsTime(redeem.getGoodsTime());
        recommend.setTimeType(redeem.getTimeType());
        if (ObjectUtils.isEmptyOrNull(redeem.getRecommendMobile())) {
            recommend.setRecommendStatus(0);
        } else {
            recommend.setRecommendStatus(1);
            recommend.setRecommendMobile(redeem.getRecommendMobile());
            recommend.setRecommendName(redeem.getRecommendName());
            recommend.setRecommendOfficeName(redeem.getRecommendOfficeName());
        }

        return recommend;
    }

    public static StatusObjDto<Integer> isValidTime(ActActivityStepEnum stepEnum) {
        if (ObjectUtils.isEmptyOrNull(stepEnum)) {
            throw ActivityBizException.ACTIVITY_NOT_OPEN;
        }
        int[] openHour = stepEnum.getOPEN_HOUR();
        int[] closeHour = stepEnum.getCLOSE_HOUR();
        if (openHour == null || openHour.length == 0) {
            throw ActivityBizException.ACTIVITY_NOT_OPEN;
        }
        if (closeHour == null || closeHour.length == 0) {
            throw ActivityBizException.ACTIVITY_NOT_OPEN;
        }
        Integer index = ActActivityStepEnum.index(stepEnum);
        if (index != null) {
            return new StatusObjDto<Integer>(true, index, 0, "");
        } else {
            throw ActivityBizException.ACTIVITY_NOT_OPEN;
        }
    }

    /**
     * @param activityCode
     * @param openId
     * @param userId
     * @param customerId
     * @param mobile
     * @param appointmentCode TODO
     * @return
     */
    public StatusObjDto<AppointmentDto> appointment(String activityCode, String openId, String userId, String customerId, String mobile, String appointmentCode) {
        StatusDto valid = isValid(activityCode);
        if (!valid.isOk()) {
            return new StatusObjDto<>(false, valid.getCode(), valid.getMsg());
        }
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return new StatusObjDto<>(false, BizException.COMMON_PARAMS_NOT_NULL.getCode(), BizException.COMMON_PARAMS_NOT_NULL.format("activityCode").getMsg());
        }
        if (ObjectUtils.isEmptyOrNull(appointmentCode)) {
            return new StatusObjDto<>(false, BizException.COMMON_PARAMS_NOT_NULL.getCode(), BizException.COMMON_PARAMS_NOT_NULL.format("appointmentCode").getMsg());
        }
        ActAppointment actAppointment = new ActAppointment();
        actAppointment.setActivityCode(activityCode);
        actAppointment.setAppointmentCode(appointmentCode);
        actAppointment.setPhone(mobile);
        actAppointment.setCustomerId(customerId);
        ActAppointmentEnum appEnum = ActAppointmentEnum.getByAppointmentCode(appointmentCode);
        if (appEnum == null) {
            return new StatusObjDto<>(false, ActivityBizException.ACTIVITY_NOT_EXIST.getCode(), ActivityBizException.ACTIVITY_NOT_EXIST.getMsg());
        }
        List<ActAppointment> list = actAppointmentDao.findList(actAppointment);

        if (list.isEmpty()) {
            actAppointment.setAppointmentName(appEnum.getAppointmentName());
            actAppointment.setReachType(appEnum.getReachType());
            actAppointment.setOpenId(openId);
            actAppointment.setUserId(userId);
            actAppointment.setCreateDate(new Date());
            actAppointment.setAppointmentTime(new Date());
            actAppointment.setPhone(mobile);
            actAppointmentDao.insert(actAppointment);
        }

        AppointmentDto dto = new AppointmentDto();
        dto.setActivityCode(activityCode);
        dto.setAppointmentCode(appointmentCode);
        dto.setStatus(CodeConstant.CODE_YES);
        String mobileWithStar = StarReplaceUtils.replaceStarAction(mobile);
        dto.setMobile(mobileWithStar);
        return new StatusObjDto<>(true, dto, StatusDto.SUCCESS, "");
    }

    public StatusObjDto<List<AppointmentDto>> appointmentStatus(String activityCode, String customerId,
                                                                String appointmentCodes, String mobile) {
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            return new StatusObjDto<>(false, BizException.COMMON_PARAMS_NOT_NULL.getCode(), BizException.COMMON_PARAMS_NOT_NULL.format("activityCode").getMsg());
        }
        if (ObjectUtils.isEmptyOrNull(appointmentCodes)) {
            return new StatusObjDto<>(false, BizException.COMMON_PARAMS_NOT_NULL.getCode(), BizException.COMMON_PARAMS_NOT_NULL.format("appointmentCodes").getMsg());
        }
        List<String> appointmentCodeList = Lists.newArrayList(appointmentCodes.split(","));
        ActAppointment actAppointment = new ActAppointment();
        actAppointment.setActivityCode(activityCode);
        actAppointment.setAppointmentCodeList(appointmentCodeList);
        actAppointment.setCustomerId(customerId);
        List<ActAppointment> list = Lists.newArrayList();
        /**客户号为空时，默认无预约**/
        if (ObjectUtils.isNotEmptyOrNull(customerId)) {
            list = actAppointmentDao.findList(actAppointment);
        }
        if (list.isEmpty()) {
            actAppointment.setPhone(mobile);
            list = actAppointmentDao.findList(actAppointment);
        }
        List<AppointmentDto> dtoList = Lists.newArrayList();
        for (String code : appointmentCodeList) {
            AppointmentDto dto = new AppointmentDto();
            dto.setActivityCode(activityCode);
            dto.setAppointmentCode(code);
            ActAppointment appointment = list.stream().filter(e -> code.equals(e.getAppointmentCode())).findAny().orElse(null);
            if (appointment != null) {
                dto.setStatus(CodeConstant.CODE_YES);
                String mobileWithStar = StarReplaceUtils.replaceStarAction(appointment.getMobile());
                dto.setMobile(mobileWithStar);
            } else {
                dto.setStatus(CodeConstant.CODE_NO);
            }
            dtoList.add(dto);
        }
        return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
    }


    public StatusObjDto<AppointmentDto> userAppointment(String activityCode, String appointmentCode, String appointmentName,
                                                        AccTokenUser user, Customer customer) {

        // 查询是否已预约
        ActAppointment qryAppointment = new ActAppointment();
        qryAppointment.setActivityCode(activityCode);
        qryAppointment.setAppointmentCode(appointmentCode);
        qryAppointment.setUserId(user.getUserId());

        // todo
        // 这里不需要获取数据, 只要简单的 count 即可
        List<ActAppointment> existAppointments = actAppointmentDao.findList(qryAppointment);

        // 若无预约, 则新增预约
        if (existAppointments.isEmpty()) {
            ActAppointment appointment = new ActAppointment();
            appointment.setActivityCode(activityCode);
            appointment.setAppointmentCode(appointmentCode);
            appointment.setAppointmentName(appointmentName);
            appointment.setUserId(user.getUserId());
            appointment.setOpenId(user.getOpenid());
            appointment.setCustomerId(customer == null ? null : customer.getCustomerId());
            appointment.setMobile(user.getMobile());
            appointment.setPhone(user.getMobile());
            Date now = new Date();
            appointment.setCreateDate(now);
            appointment.setAppointmentTime(now);
            actAppointmentDao.insert(appointment);
        }

        AppointmentDto dto = new AppointmentDto();
        dto.setActivityCode(activityCode);
        dto.setAppointmentCode(appointmentCode);
        dto.setStatus(CodeConstant.CODE_YES);
        return new StatusObjDto<>(true, dto, StatusDto.SUCCESS, "");
    }


    public StatusObjDto<List<AppointmentDto>> userAppointmentStatus(String activityCode,
                                                                    List<String> appointmentCodeList,
                                                                    AccTokenUser user) {
        ActAppointment qryAppointment = new ActAppointment();
        qryAppointment.setActivityCode(activityCode);
        qryAppointment.setAppointmentCodeList(appointmentCodeList);
        qryAppointment.setUserId(user.getUserId());

        List<ActAppointment> actAppointmentList = actAppointmentDao.findList(qryAppointment);
        List<AppointmentDto> dtoList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(actAppointmentList)) {
            for (ActAppointment actAppointment : actAppointmentList) {
                AppointmentDto dto = new AppointmentDto();
                dto.setActivityCode(actAppointment.getActivityCode());
                dto.setAppointmentCode(actAppointment.getAppointmentCode());
                dto.setStatus(actAppointment.getStatus());
                dto.setMobile(actAppointment.getPhone());
                dtoList.add(dto);
            }
        }
        return new StatusObjDto<>(true, dtoList, StatusDto.SUCCESS, "");
    }


    public void backList(String mobile) {
        //黑名单过滤
        Map<String, Object> bizMap = new HashMap<>();
        bizMap.put("mobile", mobile);
        bizMap.put("source", 10001);
        ResultDto resultDto = RemoteUtils.call("account.blacklistcooperation.getByMobileAndSource", ApiServiceTypeEnum.COOPERATION, bizMap, true);
        if (ResultDto.SUCCESS.equals(resultDto.getCode())) {
            String count = resultDto.getMsg();
            if (StringUtils.isNotBlank(count)) {
                int i = Integer.parseInt(count);
                if (i > 0) {
                    throw ACT_ACCOUNT_EXCEP;
                }
            }
        }
    }


    /**
     * 根据手机号获取对应的客户号
     */
    public String getCustomerByMobile(String mobile) {
        logger.info("fsdp call to get customer property");
        StatusObjDto<List<AccAccountOpeninfo>> khkhxx = fsdpBiz.khkhxx(null, null, mobile);
        logger.info("return:" + JsonUtils.object2JSON(khkhxx));
        if (khkhxx.isOk() && khkhxx.getObj().size() > 0) {
            return khkhxx.getObj().get(0).getCustomerId();
        }

        return null;
    }
}
