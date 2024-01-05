
package com.wlzq.activity.base.service.app;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.*;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.dto.*;
import com.wlzq.activity.base.model.*;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.Page;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动相关公共服务
 *
 * @author wlzq
 */
@Service("activity.common")
public class CommonService extends BaseService {

    private Logger logger = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActSigninBiz actSigninBiz;
    @Autowired
    private ActMessageBiz actMessageBiz;
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private ActShareBiz actShareBiz;
    @Autowired
    private ActLotteryBiz actLotteryBiz;
    @Autowired
    private ActivityDao activityDao;


    /**
     * 默认返回的消息条数
     */
    private static Integer MSG_LENGTH = 5;

    @Signature(true)
    public ResultDto status(RequestParams params) {
        String activityCode = (String) params.get("activityCode");
        StatusDto result = activityBaseBiz.status(activityCode);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }

        ResultDto back = new ResultDto(0, null, "");
        return back;
    }

    @Signature(true)
    public ResultDto activityinfo(RequestParams params) {
        String activityCode = (String) params.get("activityCode");
        StatusObjDto<ActivityDto> result = activityBaseBiz.info(activityCode);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        ActivityDto act = result.getObj();
        ResultDto back = new ResultDto(0, BeanUtils.beanToMap(act), "");
        return back;
    }


    /**
     * 获取活动列表
     *
     * @param groupCode | 活动组编码 |  | required
     * @return com.wlzq.activity.base.dto.ActivityDto
     * @cate 秒杀星期一活动
     */
    @Signature(true)
    public ResultDto activitylist(RequestParams params) {
        String groupCode = (String) params.get("groupCode");
        if (ObjectUtils.isEmptyOrNull(groupCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("groupCode");
        }

        ActListDto actListDto = new ActListDto();
        List<Activity> actList = activityDao.findListByGroupCode(groupCode);
        if (actList == null || actList.isEmpty()) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST;
        } else {
            List<ActivityDto> dtoList = actList.stream().map(act -> {
                ActivityDto actDto = new ActivityDto();
                org.springframework.beans.BeanUtils.copyProperties(act, actDto);

                // 设置活动状态
                StatusDto actValidResult = activityBaseBiz.isValid(act);
                if (!actValidResult.isOk()) {
                    actDto.setStatus(0);
                }
                return actDto;
            }).collect(Collectors.toList());
            actListDto.setCount(dtoList.size());
            actListDto.setActList(dtoList);
        }
        return new ResultDto(0, BeanUtils.beanToMap(actListDto), "");
    }

    @Signature(true)
    public ResultDto findCustomers(RequestParams params) {
        String activityCode = (String) params.get("activityCode");
        String reMobile = (String) params.get("recommendMobile");
        int[] page = buildPage(params);
        StatusObjDto<List<String>> result = activityBaseBiz.findCustomers(activityCode, reMobile, page[0], page[1]);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        List<String> customers = result.getObj();
        Map<String, Object> customersResult = new HashMap<String, Object>();
        customersResult.put("total", customers.size());
        customersResult.put("info", customers);
        ResultDto back = new ResultDto(0, customersResult, "");
        return back;
    }

    @Signature(true)
    public ResultDto findRecommender(RequestParams params) {
        String activityCode = (String) params.get("activityCode");
        String reMobile = (String) params.get("mobile");
        StatusObjDto<RecommendDto> result = activityBaseBiz.findRecommender(activityCode, reMobile);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        RecommendDto recommend = result.getObj();
        ResultDto back = new ResultDto(0, BeanUtils.beanToMap(recommend), "");
        return back;
    }

    @Signature(true)
    public ResultDto signin(RequestParams params, AccTokenUser user) {
        long startTime = System.currentTimeMillis();
        String activityCode = params.getString("activityCode");
        StatusObjDto<ActSignin> result = actSigninBiz.signIn(user, activityCode);
        if (!result.isOk()) {
            logger.info("活动接口<|>活动签到<|>结果:失败<|>活动编码:" + activityCode + "<|>userId:" + user == null ? "" : user.getUserId() + "<|>耗时:" + (System.currentTimeMillis() - startTime));
            return new ResultDto(result.getCode(), result.getMsg());
        }
        ActSignin actSignin = result.getObj();
        ResultDto back = new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(actSignin), "");
        logger.info("活动接口<|>活动签到<|>结果:成功<|>活动编码:" + activityCode + "<|>userId:" + user.getUserId() + "<|>耗时:" + (System.currentTimeMillis() - startTime));
        return back;
    }

    @Signature(true)
    public ResultDto leavingmessage(RequestParams params, AccTokenUser user) {
        long startTime = System.currentTimeMillis();
        String content = params.getString("message");
        String activityCode = params.getString("activityCode");
        StatusObjDto<ActMessage> result = actMessageBiz.leavingMessage(user, activityCode, content);
        if (!result.isOk()) {
            logger.info("活动接口<|>活动留言<|>结果:失败<|>活动编码:" + activityCode + "<|>userId:" + user == null ? "" : user.getUserId() + "<|>耗时:" + (System.currentTimeMillis() - startTime));
            return new ResultDto(result.getCode(), result.getMsg());
        }
        ResultDto back = new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(result.getObj()), CodeConstant.SUCCESS_MSG);
        logger.info("活动接口<|>活动留言<|>结果:成功<|>活动编码:" + activityCode + "<|>userId:" + user.getUserId() + "<|>耗时:" + (System.currentTimeMillis() - startTime));
        return back;
    }

    @Signature(true)
    public ResultDto messagelist(RequestParams params) {
        long startTime = System.currentTimeMillis();
        Integer maxOrder1 = params.getInt("maxOrder1");
        Integer maxOrder2 = params.getInt("maxOrder2");
        Integer length = params.getInt("length");
        String activityCode = params.getString("activityCode");
        if (length == null) {
            length = MSG_LENGTH;
        }
        StatusObjDto<List<ActMessage>> result = actMessageBiz.getMsgList(maxOrder1, length, activityCode);
        if (!result.isOk()) {
            logger.info("活动接口<|>获取活动留言列表<|>结果:失败<|>活动编码:" + activityCode + "<|>耗时:" + (System.currentTimeMillis() - startTime));
            return new ResultDto(result.getCode(), result.getMsg());
        }
        if (result.getObj() != null && result.getObj().size() == 0) { // 没有数据的情况下再去找之前的数据
            if (maxOrder1 == maxOrder2) {
                maxOrder2 = 0;
            }
            result = actMessageBiz.getMsgList(maxOrder2, length, activityCode);
        }
        int size = result.getObj() == null ? 0 : result.getObj().size();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("total", size);
        map.put("info", result.getObj());

        ResultDto back = new ResultDto(CodeConstant.SUCCESS, BeanUtils.beanToMap(map), CodeConstant.SUCCESS_MSG);
        logger.info("活动接口<|>获取活动留言列表<|>结果:成功<|>活动编码:" + activityCode + "<|>数量:" + size + "<|>耗时:" + (System.currentTimeMillis() - startTime));
        return back;
    }

    @Signature(true)
    @CustomerMustLogin(true)
    public ResultDto giveoutprize(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        String prizeTypeCode = params.getString("prizeTypeCode");
        ActPrize prize = actPrizeBiz.getOneAvailablePrize(activityCode, prizeTypeCode);
        if (prize == null) {
            return new ResultDto(ActivityBizException.ACT_EMPTY_PRIZE.getCode(), ActivityBizException.ACT_EMPTY_PRIZE.getMsg());
        }
        //更新奖品状态
        String userId = user == null ? null : user.getUserId();
        String openId = user == null ? null : user.getOpenid();
        String customerId = customer.getCustomerId();
        actPrizeBiz.updatePrize(userId, openId, customerId, prize.getId(), ActPrize.STATUS_SEND, null, null);
        //更新优惠券状态
        String redeemCode = prize.getRedeemCode();
        Map<String, Object> busparams = Maps.newHashMap();
        busparams.put("userId", userId);
        busparams.put("customerId", customerId);
        busparams.put("code", redeemCode);
        ResultDto result = RemoteUtils.call("base.couponcooperation.receivecoupon", ApiServiceTypeEnum.COOPERATION, busparams, true);
        //调用失败
        if (result.getCode() != 0) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        return new ResultDto(0, BeanUtils.beanToMap(prize), "");
    }

    @Signature(true)
    //@MustLogin(true)
    public ResultDto findprizes(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCodes = (String) params.get("activityCodes");
        String userId = user == null ? null : user.getUserId();
        String openId = user == null ? null : user.getOpenid();
        String customerId = customer == null ? null : customer.getCustomerId();
        Page page = buildPageNew(params);
        StatusObjDto<List<MyPrizeDto>> result = actPrizeBiz.findPrizes(activityCodes, customerId, userId, openId, page);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }

        Map<String, Object> customersResult = new HashMap<String, Object>();
        customersResult.put("total", result.getObj().size());
        customersResult.put("info", result.getObj());
        ResultDto back = new ResultDto(0, customersResult, "");
        return back;
    }

    @Signature(true)
    public ResultDto customerprizes(RequestParams params, AccTokenUser user) {
        int[] page = buildPage(params);
        String activityCode = params.getString("activityCodes");

        StatusObjDto<List<PrizeCustomerDto>> result = actPrizeBiz.customerPrizeList(activityCode, page[0], page[1]);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        Map<String, Object> customersResult = new HashMap<String, Object>();
        customersResult.put("total", result.getObj().size());
        customersResult.put("info", result.getObj());
        return new ResultDto(0, customersResult, "");
    }

    @Signature(true)
    @CustomerMustLogin(true)
    public ResultDto findcustomerprizes(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCodes = (String) params.get("activityCodes");
        String customerId = customer == null ? null : customer.getCustomerId();
        Page page = buildPageNew(params);
        StatusObjDto<List<MyPrizeDto>> result = actPrizeBiz.findCustomerPrizes(activityCodes, customerId, page);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }

        Map<String, Object> customersResult = new HashMap<String, Object>();
        customersResult.put("total", result.getObj().size());
        customersResult.put("info", result.getObj());
        ResultDto back = new ResultDto(0, customersResult, "");
        return back;
    }

    @Signature(true)
    @MustLogin(true)
    public ResultDto saveshare(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        String userId = user.getUserId();
        String openId = user.getOpenid();
        String customerId = customer == null ? null : customer.getCustomerId();
        int count = actShareBiz.saveShare(ActShare.TYPE_WECHAT, activityCode, userId, openId, customerId);
        int returnCode = count > 0 ? 0 : 1;
        String msg = returnCode == 0 ? "" : "分享失败";
        ResultDto result = new ResultDto();
        result.setCode(returnCode);
        result.setMsg(msg);
        return result;
    }

    /**
     * 领取用户号占用的奖品
     *
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto receiveoccupydprize(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCodes = params.getString("activityCodes");
        String userId = user.getUserId();
        String customerId = customer.getCustomerId();
        String mobile = user.getMobile();
        String openId = user.getOpenid();

        Integer repeatable = params.getInt("repeatable") == null ? new Integer(0) : params.getInt("repeatable");

        List<ActPrize> list = actPrizeBiz.receiveOccupydPrize(activityCodes, userId, openId, customerId, mobile, repeatable);
        Map<String, Object> data = Maps.newHashMap();
        data.put("total", list.size());
        data.put("info", list);
        return new ResultDto(0, data, "");
    }

    /**
     * 活动已领取的奖品列表
     *
     * @param activityCode | 活动编码 |  | required
     * @return com.wlzq.activity.base.dto.ShowPrizeDto
     * @cate 12连刮视频月卡活动
     */
    @Signature(true)
    public ResultDto allrecieved(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        Page page = buildPageNew(params);
        StatusObjDto<List<ShowPrizeDto>> result = actPrizeBiz.prizes(activityCode, page);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        Map<String, Object> data = Maps.newHashMap();
        data.put("total", result.getObj().size());
        data.put("info", result.getObj());
        return new ResultDto(0, data, "");
    }

    /**
     * 预约提醒
     *
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto appointment(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        String appointmentCode = params.getString("appointmentCode");
        String customerId = ObjectUtils.isEmptyOrNull(customer) ? null : customer.getCustomerId();
        String mobile = params.getString("mobile");
        mobile = ObjectUtils.isEmptyOrNull(mobile) ? ObjectUtils.isEmptyOrNull(customer) ? user.getMobile() : customer.getMobile() : mobile;
        String userId = ObjectUtils.isEmptyOrNull(user) ? null : user.getUserId();
        String openId = ObjectUtils.isEmptyOrNull(user) ? null : user.getOpenid();
        StatusObjDto<AppointmentDto> result = activityBaseBiz.appointment(activityCode, openId, userId, customerId, mobile, appointmentCode);
        return new ResultDto(result.getCode(), BeanUtils.beanToMap(result.getObj()), result.getMsg());
    }

    /**
     * 预约状态查询
     *
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
    public ResultDto appointmentstatus(RequestParams params, AccTokenUser user, Customer customer) {
        /**客户未登录**/
        String activityCode = params.getString("activityCode");
        String appointmentCodes = params.getString("appointmentCodes");
        String customerId = customer == null ? "" : customer.getCustomerId();
        String mobile = customer == null ? "" : customer.getMobile();
        mobile = ObjectUtils.isEmptyOrNull(mobile) ? ObjectUtils.isEmptyOrNull(customer) ? user.getMobile() : customer.getMobile() : mobile;
        StatusObjDto<List<AppointmentDto>> result = activityBaseBiz.appointmentStatus(activityCode, customerId, appointmentCodes, mobile);
        Map<String, Object> data = Maps.newHashMap();
        data.put("total", result.getObj().size());
        data.put("info", result.getObj());
        return new ResultDto(0, data, "");
    }


    /**
     * 用户预约活动提醒
     *
     * @param activityCode    | 活动编码 |  | required
     * @param appointmentCode | 预约编码 (格式为: 优惠券编码) |  | required
     * @param appointmentName | 预约名称, 如: 818财富节秒杀抢券预约 |  | non-required
     * @return com.wlzq.activity.base.dto.AppointmentDto
     * @cate 秒杀星期一活动
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto userappointment(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }

        String appointmentCode = params.getString("appointmentCode");
        if (ObjectUtils.isEmptyOrNull(appointmentCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("appointmentCode");
        }

        String appointmentName = params.getString("appointmentName");


        Activity activity = activityBaseBiz.findActivity(activityCode);
        if (activity == null) {
            throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(activityCode);
        }

        if (!Activity.STATUS_OPEN.equals(activity.getStatus())) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动未生效");
        }

        Date now = new Date();
        if (activity.getDateFrom().before(now) && activity.getDateTo().after(now)) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动已开始");
        }

        if (activity.getDateTo().before(now)) {
            throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动已结束");
        }

        StatusObjDto<AppointmentDto> result = activityBaseBiz.userAppointment(
                activityCode, appointmentCode, appointmentName, user, customer);

        return new ResultDto(result.getCode(), BeanUtils.beanToMap(result.getObj()), result.getMsg());
    }


    /**
     * 用户预约活动提醒状态查询
     *
     * @param activityCode     | 活动编码 |  | required
     * @param appointmentCodes | 预约编码 (可上传多个, 用逗号隔开, 其中每个编码格式为: 优惠券编码) |  | required
     * @param mobile           | 手机号 |  | non-required
     * @return com.wlzq.activity.base.dto.AppointmentDto
     * @cate 秒杀星期一活动
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto userappointmentstatus(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        if (ObjectUtils.isEmptyOrNull(activityCode)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
        }
        String appointmentCodes = params.getString("appointmentCodes");
        if (ObjectUtils.isEmptyOrNull(appointmentCodes)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("appointmentCodes");
        }

        List<String> appointmentCodeList = Arrays.stream(appointmentCodes.split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());
        if (appointmentCodeList.isEmpty()) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("appointmentCodes");
        }

        StatusObjDto<List<AppointmentDto>> result = activityBaseBiz.userAppointmentStatus(activityCode, appointmentCodeList, user);
        Map<String, Object> data = Maps.newHashMap();
        data.put("total", result.getObj().size());
        data.put("info", result.getObj());
        return new ResultDto(0, data, "");
    }


    /**
     * 订单领取奖品
     *
     * @param activityCode | 活动编码 |  | required
     * @param outTradeNo   | 订单号 |  | required
     * @param platform     | 平台 (vas-决策商城, service-投顾社区) |  | non-required
     * @return com.wlzq.activity.base.dto.MyPrizeDto
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto getprizebyorderid(RequestParams params, AccTokenUser user, Customer customer) {
        String activityCode = params.getString("activityCode");
        String outTradeNo = params.getString("outTradeNo");
        String platform = params.getString("platform");
        String customerId = customer != null ? customer.getCustomerId() : null;
        String userId = user.getUserId();
        String mobile = user.getMobile();

        StatusObjDto<List<MyPrizeDto>> result = null;
        if (ObjectUtils.isEmptyOrNull(platform)) {
            // 兼容旧有功能
            result = actPrizeBiz.getPrizeByOrderId(outTradeNo, customerId, userId, mobile);
        } else {
            result = actPrizeBiz.getPrizeByOrderId(activityCode, outTradeNo, platform, user, customer);
        }

        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        Map<String, Object> data = Maps.newHashMap();
        data.put("total", result.getObj().size());
        data.put("info", result.getObj());
        ResultDto back = new ResultDto(0, data, "");
        return back;
    }

}
