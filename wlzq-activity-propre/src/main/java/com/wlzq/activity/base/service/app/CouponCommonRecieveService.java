
package com.wlzq.activity.base.service.app;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.CouponCommonReceiveBiz;
import com.wlzq.activity.base.biz.NewCustomerGiftBagBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dto.AcReceivePriceVO;
import com.wlzq.activity.base.dto.CouponReceiveStatusDto;
import com.wlzq.activity.base.dto.CouponRecieveActivityDto;
import com.wlzq.activity.base.dto.CouponRecieveStatusDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.base.redis.BaseRedis;
import com.wlzq.activity.etf.biz.impl.EtfBizImpl;
import com.wlzq.activity.springfestival.biz.impl.SpringFestival2020BizImpl;
import com.wlzq.activity.task.biz.HengShengBiz;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.*;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.wlzq.activity.base.redis.CouponCommonRedis.COUPON_RECEIVE_LOCK;

/**
 * 券相关公共服务
 *
 * @author zhaozx
 */
@Service("activity.commoncouponrecieve")
@ApiServiceType({ApiServiceTypeEnum.APP,ApiServiceTypeEnum.COOPERATION})
@Slf4j
public class CouponCommonRecieveService extends BaseService {

	private static final String ACT_WOMEN_2O2O = "ACTIVITY.WOMENDAY.2020";
	private static final String ACT_NEWCUST_2O2O = "ACTIVITY.2020.NEWCUST";
	private static final String NEW_CUSTOMER_GIFTBAG = "ACTIVITY.NEW.CUSTOMER.GIFTBAG";
	/*金股雷达新增14天规格免单券活动*/
	private static final String ACTIVITY_14DAY_COUPON = "ACTIVITY.14DAY.COUPON";

    @Autowired
    private CouponCommonReceiveBiz couponRecieveBiz;
	@Autowired
	private NewCustomerGiftBagBiz newCustomerGiftBagBiz;
	@Autowired
	private ActivityBaseBiz activityBaseBiz;
	@Autowired
	private ActPrizeDao actPrizeDao;
	@Autowired
	private ActGoodsFlowDao actGoodsFlowDao;
	@Autowired
	private HengShengBiz hengShengBiz;

	/**
	 * 领取优惠券
	 *
	 * @param activityCode | 活动编码 |  | required
	 * @param prizeType    | 奖品类型编码 (可上传多个, 用逗号隔开) |  | required
	 * @return com.wlzq.activity.base.dto.CouponReceiveStatusDto
	 * @cate 秒杀星期一活动
	 */
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto receivecoupon(RequestParams params, AccTokenUser user, Customer customer) {
		String activityCode = (String) params.get("activityCode");
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}

		String prizeTypes = (String) params.get("prizeType");
		if (ObjectUtils.isEmptyOrNull(prizeTypes)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
		}

		String[] prizeTypeCodes = Arrays.stream(prizeTypes.split(","))
				.map(String::trim)
				.filter(StringUtils::isNotBlank).toArray(String[]::new);
		if (prizeTypeCodes.length == 0) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeType");
		}

		// 获取活动并检查活动是否有效
		Activity activity = activityBaseBiz.findActivity(activityCode);
		StatusDto actValidResult = activityBaseBiz.isValid(activity);
		if (!actValidResult.isOk()) {
			throw ActivityBizException.ACTIVITY_NOT_EXIST_MESSAGE.format(actValidResult.getMsg());
		}

		String productCode = prizeTypeCodes[0].split("\\.")[2];
		Long createTimeFrom = DateUtils.parseDate("2018-01-01", "yyyy-MM-dd").getTime();
		Long createTimeTo = System.currentTimeMillis();
		Map<String, Object> orderQryParams = RemoteUtils.newParamsBuilder()
				// 已支付订单
				.put("status", 1)
				.put("customerId", customer.getCustomerId())
				.put("productCode", productCode)
				.put("createTimeFrom", createTimeFrom)
				.put("createTimeTo", createTimeTo)
				.build();

		ResultDto orderDto = RemoteUtils.call("service.productcooperation.orderrecords", ApiServiceTypeEnum.COOPERATION, orderQryParams, true);
		if (orderDto == null || ResultDto.FAIL_COMMON.equals(orderDto.getCode()) || null == orderDto.getData()) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("请稍后再试");
		}

		List<Map<String, Object>> orderList = (List<Map<String, Object>>) orderDto.getData().get("info");
		if (CollectionUtil.isNotEmpty(orderList)) {
			boolean bought = orderList.stream().anyMatch(e -> {
				String timeUnit = e.get("timeUnit").toString();
				String time = e.get("time").toString();
				// 14天规格
                return "1".equals(timeUnit) && "14".equals(time);
            });
			if (bought) {
				throw ActivityBizException.ACT_ORDER_EXIST;
			}
		}

		// 获取已经领取到的奖品
		ActPrize queryPrize = new ActPrize()
				// 按活动组控制领券次数
				.setActivityGroupCode(activity.getGroupCode())
				.setCustomerId(customer.getCustomerId())
				.setPriceTypes(prizeTypeCodes);
		List<ActPrize> receivedPrizeList = actPrizeDao.getUserPrizeList(queryPrize);

		Long currentThreadId = Thread.currentThread().getId();
		boolean success = COUPON_RECEIVE_LOCK.setNXEX(customer.getCustomerId(), currentThreadId);
		log.info("领取券: lock {}", customer.getCustomerId());
		if (!success) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("领取中, 请稍后再试");
		}

		try {
			// todo: 完成双十一开发后, 需要对该逻辑做一个独立的分支, 以免影响秒杀星期一的活动
			List<CouponReceiveStatusDto> receiveStatusDtoList = couponRecieveBiz.receiveCoupon(
					activity, prizeTypeCodes, receivedPrizeList, user, customer);

			return listResult(receiveStatusDtoList);
		} finally {
			if (currentThreadId.equals(COUPON_RECEIVE_LOCK.get(customer.getCustomerId()))) {
				COUPON_RECEIVE_LOCK.del(customer.getCustomerId());
				log.info("领取券: unlock {}", customer.getCustomerId());
			}
		}
	}

    @Signature(true)
//	@CustomerMustLogin(true)
//    @MustLogin(true)
	public ResultDto recieve(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String prizeType = (String) params.get("prizeType");
		String userId = user == null?null:user.getUserId();
		String openId = user == null?null:user.getOpenid();
		String recommendCode = (String) params.get("recommendCode");
		Integer needUserId = params.getInt("needUserId");
		String customerId = customer==null?"":customer.getCustomerId();
		String mobile = "";
		Integer isMobileDimension = params.getInt("isMobileDimension");
		if (user != null) {
			mobile = user.getMobile();
		}

		if ("ACTIVITY.2023DOUBLE11.GGT".equals(activityCode)) {
			boolean haveGgPerm = hengShengBiz.checkGgPerm(user, customer);
			if (!haveGgPerm) {
				throw ActivityBizException.NOT_HAVE_GG_PERM;
			}
		}

		AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO().setUserId(userId)
				.setCustomerId(customerId).setActivityCode(activityCode)
				.setPrizeType(prizeType).setOpenId(openId)
				.setRecommendCode(recommendCode).setMobile(mobile).setNeedUserId(needUserId);
		
		if (CodeConstant.CODE_YES.equals(isMobileDimension)) {
			acReceivePriceVO.setCustomerDimension(false);
			acReceivePriceVO.setMobileDimension(true);
		}

		// 单独某一个活动的逻辑
		if (SpringFestival2020BizImpl.ACTIVITY_CODE_RECEVIE.equals(activityCode)) {
			Date openDate = customer.getOpenDate();
			SpringFestival2020BizImpl.openDateCheck(openDate);
		}

		// 单独某一个活动的检测 : 2O2O 新客检查
		finNewCustCheck(activityCode,user,customer);
		// 单独某一个活动的检测 : ACT_WOMEN_2O2O
		activityAvailableCheck(activityCode,user,customer);

		if (EtfBizImpl.ACTIVITY_CODE.equals(activityCode) && !EtfBizImpl.isPay(customer.getCustomerId())) {
			throw ActivityBizException.ACT_ETF_ONLY_PAID;
		}
		/*金股雷达活动领券状态查询和领券接口，增加下活动参与资格校验*/
		if (ACTIVITY_14DAY_COUPON.equals(activityCode)){
			checkDTCP028(customerId);
		}

		if (StringUtils.isBlank(customerId)) {
			boolean noNeedCustomerLoginPrizeType = couponRecieveBiz.isNoNeedCustomerLoginPrizeType(prizeType);
			if (!noNeedCustomerLoginPrizeType ) {
				return new ResultDto(ResultDto.FAIL_COMMON, "奖品编码有误");
			}else{
				acReceivePriceVO.setNoNeedCustomerLogin(CodeConstant.CODE_YES);
				//如果是客户号可以不登录的情况，手机号就不能为空
				if (StringUtils.isBlank(mobile)) {
					return new ResultDto(ResultDto.FAIL_COMMON, "手机号不能为空");
				}
			}
		}

		List<CouponRecieveStatusDto> recieveStatusDtos = couponRecieveBiz.receivePriceCommon(acReceivePriceVO);
		ResultDto back = null;
		if (recieveStatusDtos!=null){
			if (recieveStatusDtos.size()==1) {
				back = new ResultDto(0, BeanUtils.beanToMap(recieveStatusDtos.get(0)), "");
			}else{
				back = listResult(recieveStatusDtos);
			}
		}
		return back;
	}


	/**
	 * 领取新客大礼包
	 * (里面的奖品分别在两个不同的活动里面，所以做个新的接口)
	 */
	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto receivenewcustomergiftbag(RequestParams params, AccTokenUser user,Customer customer) {
		String recommendCode = (String) params.get("recommendCode");
		String mobile = user != null ? user.getMobile() : customer.getMobile();

		AcReceivePriceVO acReceivePriceVO1 = new AcReceivePriceVO()
				.setCustomerId(customer.getCustomerId()).setActivityCode(NEW_CUSTOMER_GIFTBAG)
				.setPrizeType("COUPON.INVEST.NEW.CUSTOMER.GIFTBAG2.202110,COUPON.INVEST.NEW.CUSTOMER.GIFTBAG3.202110,COUPON.INVEST.NEW.CUSTOMER.GIFTBAG4.202110")
				.setRecommendCode(recommendCode).setMobile(mobile);
		List<CouponRecieveStatusDto> list1 = couponRecieveBiz.receiveNewCustomerGiftBag(acReceivePriceVO1, customer);

		return listResult(list1);
	}

	/**
	 * 领取大礼包
	 */
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto receivegiftbag(RequestParams params, AccTokenUser user,Customer customer) {
		String recommendCode = (String) params.get("recommendCode");
		String activityCode = (String) params.get("activityCode");
		String mobile = user != null ? user.getMobile() : customer.getMobile();
		String userId = user != null ? user.getUserId() : "";

		AcReceivePriceVO acReceivePriceVO1 = new AcReceivePriceVO()
				.setCustomerId(customer.getCustomerId()).setActivityCode(activityCode)
				.setRecommendCode(recommendCode).setMobile(mobile).setUserId(userId);
		StatusObjDto<List<CouponRecieveStatusDto>> result = couponRecieveBiz.receiveGiftBag(acReceivePriceVO1, customer);

		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		Map<String, Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		data.put("total", result.getObj().size());
		return new ResultDto(0, data, "");
	}


	/**
	 * 查询大礼包奖品领取情况
	 */
	@Signature(true)
	@MustLogin(true)
	@CustomerMustLogin(true)
	public ResultDto prizeinfo(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		AcReceivePriceVO priceVO = new AcReceivePriceVO().setActivityCode(activityCode)
				.setUserId(user.getUserId()).setMobile(user.getMobile()).setCustomerId(customer.getCustomerId());
		StatusObjDto<List<CouponRecieveStatusDto>> result = newCustomerGiftBagBiz.queryPrizeInfo(priceVO,customer);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		Map<String, Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		data.put("total", result.getObj().size());
		return new ResultDto(0, data, "");
	}



	/**
	 * 查询用户/客户在指定活动中的奖品领取情况以及活动奖品总体情况
	 *
	 * @param activityCode | 活动编码 |  | required
	 * @param prizeTypes    | 奖品类型编码 (可上传多个, 用逗号隔开) |  | required
	 * @param dimension   | 查询维度: 0-用户, 1-客户 (默认优先使用客户维度查询) |  | non-required
	 * @param startDate | 查询开始时间: 格式为yyyyMMdd |  | non-required
	 * @param endDate   | 查询结束时间: 格式为yyyyMMdd |  | non-required
	 * @return com.wlzq.activity.base.dto.CouponReceiveStatusDto
	 * @cate 2023年双十一
	 */
	@Signature(true)
	public ResultDto userreceivecouponstatus(RequestParams params, AccTokenUser user, Customer customer) {

		String activityCode = (String) params.get("activityCode");
		if (ObjectUtils.isEmptyOrNull(activityCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activityCode");
		}

		String prizeTypes = (String) params.get("prizeTypes");
		if (ObjectUtils.isEmptyOrNull(prizeTypes)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeTypes");
		}

		String[] prizeTypeCodes = Arrays.stream(prizeTypes.split(","))
											.map(String::trim)
											.filter(e -> !StringUtils.isEmpty(e))
											.toArray(String[]::new);

		if (prizeTypeCodes.length == 0) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("prizeTypes");
		}

		// 因为该接口还会返回活动中券的信息, 例如剩余数量, 已领取的数量等, 因此需要校验活动是否有效
		Activity activity = activityBaseBiz.findActivity(activityCode);
		if (activity == null) {
			throw ActivityBizException.ACTIVITY_NOT_EXIST;
		}

		Integer dimension = params.getInt("dimension");

		String startDate = (String) params.get("startDate");
		String endDate = (String) params.get("endDate");

		StatusObjDto<List<CouponReceiveStatusDto>> result =
				couponRecieveBiz.userReceiveCouponStatus(activity, prizeTypeCodes, user, customer,
						dimension, startDate, endDate);

		if(!result.isOk()) {
			return new ResultDto(result.getCode() ,result.getMsg());
		}

		Map<String,Object> data = Maps.newHashMap();
		data.put("total",result.getObj().size());
		data.put("info", result.getObj());
		return new ResultDto(0, data,"");
	}


    @Signature(true)
	//@MustLogin(true)
	public ResultDto status(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String prizeTypes = (String) params.get("prizeTypes");
		String customerId = customer == null?"0":customer.getCustomerId();
		String userId = user == null?null:user.getUserId();
		String recommendCode = (String) params.get("recommendCode");
		String openId = user == null ? null : user.getThirdUid();
		String mobile = user == null ? null : user.getMobile();
		Integer needUserId = params.getInt("needUserId");
		Integer isMobileDimension = params.getInt("isMobileDimension");

		/*金股雷达活动领券状态查询和领券接口，增加下活动参与资格校验*/
		if (ACTIVITY_14DAY_COUPON.equals(activityCode)){
			checkDTCP028(customerId);
		}
		AcReceivePriceVO acReceivePriceVO = new AcReceivePriceVO().setUserId(userId)
				.setCustomerId(customerId).setActivityCode(activityCode)
				.setPrizeType(prizeTypes).setOpenId(openId)
				.setRecommendCode(recommendCode).setMobile(mobile).setNeedUserId(needUserId);
		if (CodeConstant.CODE_YES.equals(isMobileDimension)) {
			acReceivePriceVO.setCustomerDimension(false);
			acReceivePriceVO.setMobileDimension(true);
		}
		// 单独某一个活动的检测 : 2O2O 新客检查
		Boolean flag = finNewCustCheckFlag(activityCode,user,customer);
		if (!flag) {
	    	StatusObjDto<List<CouponRecieveStatusDto>> couponDto = couponRecieveBiz.getNewCustomerCouponStatus(acReceivePriceVO);
	    	if(!couponDto.isOk()) {
	    		return new ResultDto(couponDto.getCode() ,couponDto.getMsg());
	    	}
	    	Map<String,Object> couponData = Maps.newHashMap();
	    	couponData.put("total",couponDto.getObj().size());
	    	couponData.put("info", couponDto.getObj());
			return new ResultDto(0,couponData,"");
		}
		
    	StatusObjDto<List<CouponRecieveStatusDto>> result = couponRecieveBiz.status(acReceivePriceVO);

    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("total",result.getObj().size());
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}

	private void checkDTCP028(String customerId) {
		Map<String, Object> busparams = Maps.newHashMap();

		busparams.put("customerId", customerId);
		/*产品编码*/
		busparams.put("productCode", "DTCP028");
		/*购买数量*/
		busparams.put("quantity", 14);
		/*购买期限单位，1：天，2：周，3：月，4：季，5：年*/
		busparams.put("timeUnit", 1);

		ResultDto result = RemoteUtils.call("service.productcooperation.countadviserorder", ApiServiceTypeEnum.COOPERATION, busparams, true);

		if (!result.getCode().equals(ResultDto.SUCCESS)) {
			throw new ActivityBizException(result.getCode(), result.getMsg());
		}

		Integer count = (Integer) result.getData().get("count");
		if (count > 0) {
			throw ActivityBizException.ACT_PRIZE_USED_DTCP028_14DAYS;
		}
	}


	@Signature(true)
	@CustomerMustLogin(true)
	public ResultDto sectionrecieve(RequestParams params, AccTokenUser user,Customer customer) {
		String activityCode = (String) params.get("activityCode");
		String prizeTypes = (String) params.get("prizeTypes");
		String customerId = customer.getCustomerId();
		String userId = user == null ? null : user.getUserId();
		String openId = user == null ? null : user.getThirdUid();
    	StatusObjDto<Integer> result = couponRecieveBiz.sectionReceive(activityCode, prizeTypes, userId, openId, customerId);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	Integer status = result.getObj();
    	data.put("status", status);
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}

    @Signature(true)
	public ResultDto findactivities(RequestParams params, AccTokenUser user,Customer customer) {
		Integer plate =  params.getInt("plate");
		String productCode = params.getString("productCode");
    	StatusObjDto<List<CouponRecieveActivityDto>> result = couponRecieveBiz.findActivities(plate, productCode);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	Map<String,Object> data = Maps.newHashMap();
    	data.put("total", result.getObj().size());
    	data.put("info", result.getObj());
    	ResultDto back = new ResultDto(0,data,"");
    	return back;
	}

    private static void activityAvailableCheck(String acitivty, AccTokenUser user,Customer customer) {
		if (ACT_WOMEN_2O2O.equals(acitivty)) {
			Integer gender = customer.getGender();
			if (!CodeConstant.CODE_YES.equals(gender)) {
				throw ActivityBizException.ACT_ONLY_WOMEN;
			}
		}
    }

    private static void finNewCustCheck(String activity, AccTokenUser user, Customer customer) {
    	if (ObjectUtils.isEmptyOrNull(activity)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("activity");
		}
    	if (!ACT_NEWCUST_2O2O.equals(activity)) {
    		return;
    	}
		String customerId = customer == null ? null : customer.getCustomerId();
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			return;
		}
		// 缓存中有客户号
		if (BaseRedis.ACT_FINORDER_BUYSUCCESS_CUSID.exists(customerId)) {
			throw ActivityBizException.ACT_ONLY_NEWCUST;
		}
		Map<String, Object> busparams = Maps.newHashMap();
		busparams.put("customerId", customerId);
		ResultDto finOrderDto = RemoteUtils.call("fin.ordercooperation.succount", ApiServiceTypeEnum.COOPERATION, busparams, true);
		if (ResultDto.SUCCESS.equals(finOrderDto.getCode()) && finOrderDto.getData() != null) {
			Integer count = (Integer)finOrderDto.getData().get("count");
			if (count != null && count.compareTo(0) == 1) {
				throw ActivityBizException.ACT_ONLY_NEWCUST;
			}
		}
		if (finOrderDto == null || ResultDto.FAIL_COMMON.equals(finOrderDto.getCode())) {
			throw ActivityBizException.ACT_ONLY_NEWCUST;
		}
    }

	/**
	 *
	 * @param activity
	 * @param user
	 * @param customer
	 * @return CodeConstant.CODE_YES -- 检查通过， CodeConstant.CODE_NO - 检查不通过，直接返回
	 */
	private static Boolean finNewCustCheckFlag(String activity, AccTokenUser user, Customer customer) {
		if (ObjectUtils.isEmptyOrNull(activity)) {
			return false;
		}
		if (!ACT_NEWCUST_2O2O.equals(activity)) {
			return true;
		}
		String customerId = customer == null ? null : customer.getCustomerId();
		if (ObjectUtils.isEmptyOrNull(customerId)) {
			return false;
		}
		// 缓存中有客户号
		if (BaseRedis.ACT_FINORDER_BUYSUCCESS_CUSID.exists(customerId)) {
			return false;
		}
		Map<String, Object> busparams = Maps.newHashMap();
		busparams.put("customerId", customerId);
		ResultDto finOrderDto = RemoteUtils.call("fin.ordercooperation.succount", ApiServiceTypeEnum.COOPERATION, busparams, true);
		if (ResultDto.SUCCESS.equals(finOrderDto.getCode()) && finOrderDto.getData() != null) {
			Integer count = (Integer)finOrderDto.getData().get("count");
			if (count != null && count.compareTo(0) == 1) {
				return false;
			}
		}
		if (finOrderDto == null || ResultDto.FAIL_COMMON.equals(finOrderDto.getCode())) {
			return false;
		}
		return true;
	}

    @Signature(true)
	public ResultDto recievebyuserid(RequestParams params) {
		String userId = params.getString("userId");
		String activityCode = params.getString("activityCode");
		String prizeType = params.getString("prizeType");
		StatusObjDto<CouponRecieveStatusDto> result = couponRecieveBiz.receiveByUserId(activityCode, prizeType, userId, null, null,null, null);
    	if(!result.isOk()) {
    		return new ResultDto(result.getCode() ,result.getMsg());
    	}
    	ResultDto back = new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
    	return back;
	}

	public static <T> ResultDto listResult(List<T> list ) {
		Map<String,Object> data = new HashMap<>();
		if (list!=null && list.size()>0) {
			JSONArray jsonObject = (JSONArray) JSON.toJSON(list);
			data.put("list",jsonObject);
		}
		return new ResultDto(ResultDto.SUCCESS,data,"");
	}
}
