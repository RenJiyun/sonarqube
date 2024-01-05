package com.wlzq.activity.redeem.biz.impl;

import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.impl.Level2BizImpl;
import com.wlzq.activity.redeem.biz.RedeemBiz;
import com.wlzq.activity.redeem.dao.RedeemDao;
import com.wlzq.activity.redeem.dao.RedeemReceiveRecordDao;
import com.wlzq.activity.redeem.dto.RedeemDto;
import com.wlzq.activity.redeem.dto.RedeemGoodsDto;
import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.activity.redeem.model.RedeemReceiveRecord;
import com.wlzq.activity.redeem.observers.RedeemObserver;
import com.wlzq.activity.redeem.observers.RedeemObservers;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.Staff;
import com.wlzq.common.utils.*;
import com.wlzq.common.utils.security.RSAUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.StaffBiz;
import com.wlzq.remote.service.common.base.PushBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 兑换码业务接口实现
 * @author louie
 *
 */
@Service
public class RedeemBizImpl extends ActivityBaseBiz implements RedeemBiz {
	private SimpleDateFormat timeFormatter = new SimpleDateFormat ("yyyy年MM月dd日");
	private static final String ACCOUNT_CERT_CODE="REDEEM.ACCOUNT.CERTIFACATION";
	@Autowired
	private	RedeemDao redeemDao;
	@Autowired
	private	RedeemReceiveRecordDao receiveRecordDao;
    @Autowired
    private StaffBiz staffBiz;
    @Autowired
    private PushBiz pushBiz;
    
	@Value("${lottery.ths.order.create}")
	private String createOrderUrl;
	@Value("${lottery.ths.order.notify}")
	private String notifyUrl;
	@Value("${wechat.sd.user.info}")
	private String wechatUserInfoUrl;
	@Value("${wechat.message.accountbind.template}")
	private String pushTemplate;
	@Value("${wechat.sd.weixin.pk}")
	private String wechatPk;
	
	private Logger logger = LoggerFactory.getLogger(RedeemBizImpl.class);
	 
	@Override
	public StatusDto sendCodeToWechat(String activityCode,String openId,String fundAccount,String nickname) {
		StatusDto validActivity = isValid(activityCode);
		if(!validActivity.isOk()) {
			return validActivity;
		}
		if(ObjectUtils.isEmptyOrNull(openId)) {
			return new StatusDto(false,1,"微信openid为空");
		}
		//查询该微信用户是否领取过
		RedeemReceiveRecord  ridParams = new RedeemReceiveRecord();
		ridParams.setTypeCode(ACCOUNT_CERT_CODE);
		ridParams.setReceiveId(openId);
		List<RedeemReceiveRecord> receives = receiveRecordDao.findList(ridParams);
		if(receives != null && receives.size() > 0) {
			return  new StatusDto(false,1,"该微信用户已参与过活动");
		}
		/*
		//查询该微信用户绑定的资金账号
		StatusObjDto<Map> fundResult = getFundAccount(openId);
		if(!fundResult.isOk()) {
			return fundResult;
		}
		String fundAccount = (String) fundResult.getObj().get("account");
		String nickname = (String) fundResult.getObj().get("nickname");
		*/
		
		//查询该资金账号是否领取过
		RedeemReceiveRecord  fundParams = new RedeemReceiveRecord();
		fundParams.setTypeCode(ACCOUNT_CERT_CODE);
		fundParams.setFundAccount(fundAccount);
		List<RedeemReceiveRecord> receivesf = receiveRecordDao.findList(ridParams);
		if(receivesf != null && receivesf.size() > 0) {
			return  new StatusDto(false,1,"该资金账号已参与过活动");
		}
		
		//查询未发送的兑换码
		Redeem redeem = redeemDao.findNotOccupy(ACCOUNT_CERT_CODE);
		//修改兑换码状态为占用状态
		redeem.setStatus(1);
		redeemDao.update(redeem);
		
		String deadlineDay = "";
		if(redeem.getValidityType().equals(2)) {
			Date deadline = DateUtils.addDay(new Date(), redeem.getValidityDay());
			deadlineDay = timeFormatter.format(deadline);
		}
		List<Object> pushParams = new ArrayList<Object>();
		pushParams.add(nickname);
		pushParams.add(fundAccount);
		pushParams.add("账户验证成功，恭喜您中奖！");
		String tip = "获得Level-2增强行情产品1个月使用权（价值8元）,\\n" + 
				  "兑换码为："+redeem.getCode()+"，兑换码有效期至"+deadlineDay+"，过期失效，快到“e万通”体验吧"+
				"\\n \\n 点我查看兑换步骤》》";
		pushParams.add(tip);
		StatusDto pushStatus = pushBiz.push(pushTemplate, openId, pushParams);
		
		//若推送失败修改兑换码状态
		if(!pushStatus.isOk()) {
			redeem.setStatus(0);
			redeemDao.update(redeem);
			return new StatusDto(false,pushStatus.getCode(),"");
		}
		
		redeem.setOutTime(new Date());
		redeem.setOpenId(openId);
		redeemDao.update(redeem);
		
		//添加领取记录
		RedeemReceiveRecord receiveRecord = new RedeemReceiveRecord();
		receiveRecord.setActivityCode(activityCode);
		receiveRecord.setFundAccount(fundAccount);
		receiveRecord.setReceiveId(openId);
		receiveRecord.setTypeCode(ACCOUNT_CERT_CODE);
		receiveRecord.setCreateTime(new Date());
		receiveRecordDao.insert(receiveRecord);
		
		return new StatusDto(true,0,"");
	}


	@Override
	public StatusObjDto<RedeemGoodsDto> recieve(String activityCode, String code, String mobile, String userId, String recommendMobile) {
		/*
		StatusDto validActivity = isValid(activityCode);
		if(!validActivity.isOk()) {
			return new StatusObjDto<RedeemGoodsDto>(false,validActivity.getCode(),validActivity.getMsg());
		}
		*/
		StatusDto checkParams = checkParams(code, mobile);
		if(!checkParams.isOk()) {
			return new StatusObjDto<RedeemGoodsDto>(false,checkParams.getCode(),checkParams.getMsg());
		}
		if(ObjectUtils.isNotEmptyOrNull(recommendMobile) && !RegxUtils.isMobile(recommendMobile)){
			return new StatusObjDto<RedeemGoodsDto>(false,206, "请输入正确的推荐人手机号"); 
		}
		Staff staff = null;
		if(ObjectUtils.isNotEmptyOrNull(recommendMobile)) {
			StatusObjDto<Staff> staffStatus = staffBiz.infoByMobile(recommendMobile);
			if(!staffStatus.isOk()) {
				return new StatusObjDto<RedeemGoodsDto>(false, 207,"获取推荐人信息失败"); 
			}
			staff = staffStatus.getObj();
		}


		Redeem redeem = redeemDao.findByCode(code);
		RedeemGoodsDto goodsInfo = new RedeemGoodsDto();
		if(redeem == null) {
			// return new StatusObjDto<>(false, 301, "兑换码不正确"); old
			//查不到就去base兑换
			Map<String,Object> map = new HashMap<>();
			map.put("code",code);
			map.put("recommendMobile",recommendMobile);
			ResultDto result = RemoteUtils.call("base.couponcooperation.receivelevel2", ApiServiceTypeEnum.COOPERATION, map, true);
			if(!result.getCode().equals(ResultDto.SUCCESS)) {
				return new StatusObjDto<>(false,result.getCode(),result.getMsg());
			}
			
			if (result.getData() != null) {
				goodsInfo = BeanUtils.mapToBean(result.getData(), RedeemGoodsDto.class);
			}
		}else{
			//活动平台查询到兑换码的情况
			StatusObjDto<RedeemGoodsDto> statusObjDto = doReceive(code, mobile, userId, recommendMobile, staff, redeem, goodsInfo);
			if (statusObjDto != null) {
				return statusObjDto;
			}
		}


		//通知领取结果
		Thread notifyT = new Thread(new Runnable() {
			@Override
			public void run() {
				List<RedeemObserver> observers = RedeemObservers.getObservers();
				for(RedeemObserver observer:observers) {
					observer.notify(userId,code);
				}
			}
		});
		notifyT.start();
		
		return  new StatusObjDto<>(true, goodsInfo,0,"");
	}

	private StatusObjDto<RedeemGoodsDto> doReceive(String code, String mobile, String userId, String recommendMobile, Staff staff, Redeem redeem, RedeemGoodsDto goodsInfo) {
		if(redeem.getStatus().equals(2)) {
			return new StatusObjDto<>(false, 302, "兑换码已使用");
		}
		//验证有效期
		Integer type = redeem.getValidityType();
		long now = new Date().getTime();
		if(type.equals(1)) {//验证时间范围
			if(now < redeem.getValidityDateFrom().getTime() ||
					now > redeem.getValidityDateTo().getTime()) {
				return new StatusObjDto<>(false,302,"不在有效期");
			}
		}else if(type.equals(2)) { //验证有效天数
			Date deadlineDay = DateUtils.addDay(redeem.getOutTime(), redeem.getValidityDay());
			if(ObjectUtils.isEmptyOrNull(deadlineDay)) {
				throw BizException.COMMON_CUSTOMIZE_ERROR.format("兑换码未发出");
			}
			Date dayEndTime = DateUtils.getDayEnd(deadlineDay);
			long deadline = dayEndTime.getTime();
			if(now > deadline) {
				return new StatusObjDto<>(false, 302, "不在有效期");
			}
		}
		boolean openStatus = notityTongHS(redeem, mobile, recommendMobile);

		Integer status = openStatus?1:105;
		if(!status.equals(1)) {
			goodsInfo.setStatus(status);
			goodsInfo.setGoodsName(redeem.getGoodsName());
			goodsInfo.setGoodsTime(redeem.getGoodsTime());
			goodsInfo.setTimeType(redeem.getTimeType());
			return new StatusObjDto<>(false, goodsInfo, 105, "权限开通失败");
		}
		Integer notiStatus = status.equals(1)?1:0;
		redeem.setNotityStatus(notiStatus);
		redeem.setStatus(2); //已开通
		redeem.setTakeTime(new Date());
		redeem.setMobile(mobile);
		redeem.setUserId(userId);
		if(staff != null) {
			redeem.setRecommendMobile(recommendMobile);
			redeem.setRecommendName(staff.getName());
			redeem.setRecommendOfficeId(staff.getOfficeId());
			redeem.setRecommendOfficeName(staff.getOfficeName());
		}
		redeemDao.update(redeem);

		goodsInfo.setStatus(status);
		goodsInfo.setGoodsName(redeem.getGoodsName());
		goodsInfo.setGoodsTime(redeem.getGoodsTime());
		goodsInfo.setTimeType(redeem.getTimeType());

		/**同步更新优惠券信息**/
		remoteUpdateCoupon(code);
		return null;
	}

	@Override
	public StatusObjDto<RedeemDto> findRedeemByOpenId(String openId) {
		Redeem redeem = new Redeem();
		//redeem.setStatus(1);
		redeem.setOpenId(openId);
		List<Redeem> redeems = redeemDao.findList(redeem);
		if(redeems != null && redeems.size() > 0) {
			RedeemDto redeemDto = new RedeemDto(redeems.get(0).getStatus(),redeems.get(0).getCode());
			return new StatusObjDto<RedeemDto>(true,redeemDto,0,"");
		}
		return new StatusObjDto<RedeemDto>(false,null,0,"");
	}
	

	@Override
	public StatusObjDto<Redeem> findAvailable(String typeCode) {
		//查询未发送的兑换码
		Redeem redeem = redeemDao.findNotOccupy(typeCode);
		if(redeem == null) {
			return  new StatusObjDto<Redeem>(false,1,"无可用的兑换码");
		}
		//修改兑换码状态为占用状态
		redeem.setStatus(1);
		//设置兑换码发出时间
		redeem.setOutTime(new Date());
		redeemDao.update(redeem);
		
		return new StatusObjDto<Redeem>(true,redeem,0,"");
	}

	@Override
	public StatusObjDto<Redeem> findRedeemByCode(String code) {
		Redeem redeem = redeemDao.findByCode(code);
		if(redeem == null) {

			return new StatusObjDto<Redeem>(false,StatusDto.FAIL_COMMON,"兑换码不存在");
		}
		return  new StatusObjDto<Redeem>(true,redeem,0,"");
	}
	
	private StatusDto checkParams(String code, String mobile) {
		if(ObjectUtils.isEmptyOrNull(code)) {
			return new StatusDto(false,203,"code参数不能为空"); 
		}
		if(ObjectUtils.isEmptyOrNull(code)) {
			return new StatusDto(false,204,"code参数不能为空"); 
		}
		
		if(!RegxUtils.isMobile(mobile)){
			return new StatusDto(false,204, "请输入正确的手机号"); 
		}
		
		return new StatusDto(true,0, ""); 
	}

	private StatusObjDto<Map> getFundAccount(String openid) {
		try {
			String userInfoUrl = wechatUserInfoUrl.replace("{weixinpk}", wechatPk).replace("{openid}", openid);
			String infoResult = HttpClientUtils.doGet(userInfoUrl, null);
			Map mapResult = JsonUtils.json2Map(infoResult);
			String errNo = (String)mapResult.get("error_no");
			if(errNo != null && errNo.equals("0")) {
				List<Map> results = (List<Map>) mapResult.get("results");
				if(results != null && results.size() > 0) {
					String fundAccount = (String) results.get(0).get("zj_account");
					String nickname = (String) results.get(0).get("nickname");
					Map<String,String> data = new HashMap<String,String>();
					data.put("account", fundAccount);
					data.put("nickname", nickname);
					return  new StatusObjDto<Map>(true,data,0,"");
				}
			}else {
				String errorInfo = (String) mapResult.get("error_info");
				return  new StatusObjDto<Map>(false,1,errorInfo);
			}
		}catch(Exception ex) {
			return  new StatusObjDto<Map>(false,1,ex.getMessage());
		}
		
		return  new StatusObjDto<Map>(false,1,"从SD获取微信用户信息异常");
	}
	
	/**
	 * 同花顺创建订单与修改权限
	 * @param redeem
	 */
	private boolean notityTongHS(final Redeem redeem,String mobile,String recommendMobile) {
		String url = "";
		try {
			//创建订单
			Map params = new HashMap<String,Object>();
			params.put("goodsId", redeem.getGoodsId());
			Integer mobileNeedEnc = AppConfigUtils.getInt(Level2BizImpl.CONFIG_MOBILE_NEED_ENC,0);
			if(CodeConstant.CODE_YES.equals(mobileNeedEnc)) {
				String rsaKey =  AppConfigUtils.get(Level2BizImpl.CONFIG_RSA_KEY);
				RSAPublicKey pubKey = RSAUtils.getPublicKey(rsaKey);
				String mobileJson = "{\"mobile_phone\":\""+mobile+"\"}";
				mobile = RSAUtils.encryptByPublicKeyToBase64(mobileJson, pubKey);
			}
			params.put("customerId", mobile);
			if(ObjectUtils.isNotEmptyOrNull(recommendMobile)) {
				params.put("phone", recommendMobile);
			}
			url = createOrderUrl+",params:"+JsonUtils.mapToJson(params);
			String createResultS = HttpClientUtils.doPost(createOrderUrl, params);
			Map orderMap = JsonUtils.json2Map(createResultS);
			Integer code = (Integer) orderMap.get("code");
			if(code == null || !code.equals(0)) {
				updateNotifyResult(redeem,0,url+",result:"+createResultS);
				return false;
			}
			String order = (String) orderMap.get("data");
			redeem.setOrderNo(order);
			updateNotifyResult(redeem,1,url+",result:"+createResultS);

			//修改权限
			params.clear();
			params.put("orderNo", redeem.getOrderNo());	
			params.put("reason", "兑换码活动");
			params.put("channelCode", 8);
			params.put("payStatus", 1); 
			params.put("payMode", 6);
			params.put("customerProductType", 1);
			params.put("practicalPrice", "0"); //实收金额设置为0
			if(ObjectUtils.isNotEmptyOrNull(recommendMobile)) {
				params.put("phone", recommendMobile);
			}
			url = notifyUrl+",params:"+JsonUtils.mapToJson(params);
			String notifyResultS = HttpClientUtils.doPost(notifyUrl, params);
			Map notifyMap = JsonUtils.json2Map(notifyResultS);
			code = (Integer) notifyMap.get("code");
			if(code == null || !code.equals(0)) {
				updateNotifyResult(redeem,code,url+",result:"+notifyResultS);
				return false;
			}
			updateNotifyResult(redeem,1,url+",result:"+notifyResultS);
			return true;
		} catch (Exception e) {
			updateNotifyResult(redeem,0,url+",result:"+e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	private void updateNotifyResult(Redeem redeem,Integer status,String message) {
		//redeem.setNotityStatus(status);
		redeem.setNotityMessage(message);
		redeemDao.update(redeem);
	}
	
	/**
	 * 远程同步优惠券
	 * @param code
	 */
	private void remoteUpdateCoupon(String code) {
		if (ObjectUtils.isEmptyOrNull(code)) {
			return;
		}
		Redeem redeem = redeemDao.findByCode(code);
		if (ObjectUtils.isEmptyOrNull(redeem)) {
			return;
		}
		String userId = redeem.getUserId();
		String orderNo = redeem.getOrderNo();
		Long outTime = null;
		if (ObjectUtils.isNotEmptyOrNull(redeem.getOutTime())) {
			outTime = redeem.getOutTime().getTime();
		}
		Long takeTime = null;
		if (ObjectUtils.isNotEmptyOrNull(redeem.getTakeTime())) {
			takeTime = redeem.getTakeTime().getTime();
		}
		Integer status = redeem.getStatus();
		String notityMessage = redeem.getNotityMessage();
		Map<String, Object> busparams = BeanUtils.beanToMap(redeem);
		busparams.put("code", code);
		busparams.put("userId", userId);
		busparams.put("orderNo", orderNo);
		busparams.put("outTime", outTime);
		busparams.put("takeTime", takeTime);
		busparams.put("status", status);
		busparams.put("notityMessage", notityMessage);
		ResultDto resultDto = RemoteUtils.call("base.couponcooperation.updredeem", ApiServiceTypeEnum.COOPERATION, busparams, true);
		logger.info("updredeem: " + resultDto.getCode() + ", " + resultDto.getMsg());
	}

//	@Override
//	@Transactional
//	public StatusDto out(String code) {
//		if(ObjectUtils.isEmptyOrNull(code)) {
//			return  new StatusDto(false,StatusDto.FAIL_COMMON,"code参数为空");
//		}
//		Redeem redeem = redeemDao.findByCode(code);
//		if(redeem == null) {
//			return  new StatusDto(false,StatusDto.FAIL_COMMON,"兑换码不存在");
//		}
//		//修改兑换码状态为占用状态
//		redeem.setStatus(1);
//		redeem.setOutTime(new Date());
//		redeemDao.update(redeem);
//		
//		return new StatusDto(true,StatusDto.SUCCESS);
//	}

}
