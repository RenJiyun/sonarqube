package com.wlzq.activity.couponreceive.biz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.couponreceive.biz.CouponRecieveBiz;
import com.wlzq.activity.couponreceive.config.CouponRecieveActivity;
import com.wlzq.activity.couponreceive.config.RecieveCoupon;
import com.wlzq.activity.couponreceive.config.RecieveInfo;
import com.wlzq.activity.couponreceive.dao.CouponRecieveDao;
import com.wlzq.activity.couponreceive.dto.CouponDto;
import com.wlzq.activity.couponreceive.dto.CouponsDto;
import com.wlzq.activity.couponreceive.model.CouponRecieve;
import com.wlzq.activity.couponreceive.redis.CouponReceiveRedis;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.CouponBiz;

/**
 * 优惠券领取活动接口实现
 * @author louie
 *
 */
@Service
public class CouponRecieveBizImpl implements CouponRecieveBiz {
	@Autowired
	private CouponBiz couponBiz;
	@Autowired
	private CouponRecieveActivity activity;
	@Autowired
	private CouponRecieveDao  recieveDao;
	
	private String CLZH_DISCOUNT8 = "COUPON.ACT.RECIEVE.CLZH.DISCOUNT8";
	private String SYSYH_DISCOUNT6 = "COUPON.ACT.RECIEVE.SYSYH.DISCOUNT6";
	private String YFDP_DISCOUNT5 = "COUPON.ACT.RECIEVE.YFDP.DISCOUNT5";
	private String QLCJ_DISCOUNT5 = "COUPON.ACT.RECIEVE.QLCJ.DISCOUNT5";
	
	private HashMap<Integer, ArrayList<String>> couponrecieves = new HashMap<Integer, ArrayList<String>>(){{  
	      put(1,Lists.newArrayList(new String[] {CLZH_DISCOUNT8}));       
	      put(2,Lists.newArrayList(new String[] {SYSYH_DISCOUNT6}));       
	      put(3,Lists.newArrayList(new String[] {YFDP_DISCOUNT5,QLCJ_DISCOUNT5}));      
	}}; 

	private HashMap<String, String> produceCodes = new HashMap<String, String>(){{  
	      put(CLZH_DISCOUNT8,"DTCP005");       
	      put(SYSYH_DISCOUNT6,"DTCP006");       
	      put(YFDP_DISCOUNT5,"DTCP003");       
	      put(QLCJ_DISCOUNT5,"DTCP004");      
	}}; 
	
	private Date id1TimeStart =  DateUtils.parseDate("2019-05-28 9:00:00", "yyyy-MM-dd HH:mm:ss");
	private Date id1TimeEnd =  DateUtils.parseDate("2019-05-30 24:00:00", "yyyy-MM-dd HH:mm:ss");
	private Date id2TimeStart =  DateUtils.parseDate("2019-05-31 9:00:00", "yyyy-MM-dd HH:mm:ss");
	private Date id2TimeEnd =  DateUtils.parseDate("2019-06-3 24:00:00", "yyyy-MM-dd HH:mm:ss");
	private Date id3TimeStart =  DateUtils.parseDate("2019-06-4 9:00:00", "yyyy-MM-dd HH:mm:ss");
	private Date id3TimeEnd =  DateUtils.parseDate("2019-06-6 24:00:00", "yyyy-MM-dd HH:mm:ss");
	
	
	@Override
	public StatusObjDto<Map<Integer,CouponsDto>> coupons(String customerId) {
		Date now = new Date();
		Map<Integer,CouponsDto> coupons = Maps.newHashMap();
		for(Integer key:couponrecieves.keySet()) {
			List<String> templates = couponrecieves.get(key);
			List<CouponDto>  cs = Lists.newArrayList();
			CouponsDto couponsDto = new CouponsDto();
			for(String te:templates) {
				CouponDto couponDto = getCouponInfo(te);
				if(couponDto == null) continue;
				String productCode = produceCodes.get(te);
				couponDto.setProductCode(productCode);
				//是否可领取设置
				if(now.getTime() >= id1TimeStart.getTime() && now.getTime() < id1TimeEnd.getTime() && 
						te.equals(CLZH_DISCOUNT8)) {
					couponsDto.setCanRecieve(1);
				}else if(now.getTime() >= id2TimeStart.getTime() && now.getTime() < id2TimeEnd.getTime() && 
						te.equals(SYSYH_DISCOUNT6)) {
					couponsDto.setCanRecieve(1);
				}else if(now.getTime() >= id3TimeStart.getTime() && now.getTime() < id3TimeEnd.getTime() && 
						(te.equals(YFDP_DISCOUNT5) || te.equalsIgnoreCase(QLCJ_DISCOUNT5))) {
					couponsDto.setCanRecieve(1);
				}else {
					couponsDto.setCanRecieve(0);
				}
				
				//是否已领取设置
				if(ObjectUtils.isNotEmptyOrNull(customerId) && ((te.equals(CLZH_DISCOUNT8) && now.getTime() > id1TimeEnd.getTime()) ||
						(te.equals(SYSYH_DISCOUNT6) && now.getTime() > id2TimeEnd.getTime()) ||
						((te.equals(YFDP_DISCOUNT5) || te.equals(QLCJ_DISCOUNT5)) && now.getTime() > id3TimeEnd.getTime()))) {
					Integer recieveCount = (Integer) CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.get(customerId+CLZH_DISCOUNT8);
					if(recieveCount == null) {
						CouponRecieve param = new CouponRecieve();
						param.setCouponTemplate(te);
						param.setCustomerId(customerId);
						recieveCount = recieveDao.findCount(param);
						CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.set(customerId+te,recieveCount);
					}
					if(recieveCount > 0) {
						couponDto.setStatus(CouponsDto.STATUS_HAS_RECIEVED);
					}
				}
				
				cs.add(couponDto);
			}
			couponsDto.setCoupons(cs);
			coupons.put(key,couponsDto);
		}
		
		/*
		if(now.getTime() > id1TimeEnd.getTime() && ObjectUtils.isNotEmptyOrNull(customerId)) {
			Integer recieveCount = (Integer) CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.get(customerId+CLZH_DISCOUNT8);
			if(recieveCount == null && ObjectUtils.isNotEmptyOrNull(customerId)) {
				CouponRecieve param = new CouponRecieve();
				param.setCouponTemplate(CLZH_DISCOUNT8);
				param.setCustomerId(customerId);
				recieveCount = recieveDao.findCount(param);
				CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.set(customerId+CLZH_DISCOUNT8,recieveCount);
			}
			if(recieveCount > 0) {
				coupons.get(1).getCoupons().get(0).setStatus(CouponsDto.STATUS_HAS_RECIEVED);
			}
		}
		*/

		List<RecieveInfo> infos = activity.getRecieveInfos();
		RecieveInfo enableInfo = null;
		//查询进行中的可领取的优惠券
		for(int i = 0;i < infos.size();i++) {
			RecieveInfo info = infos.get(i);
			if(now.getTime() >= info.getTimeStart().getTime() && now.getTime() < info.getTimeEnd().getTime()) {
				enableInfo = info;
				break;
			}
		}
		if(enableInfo == null) {
			return new StatusObjDto<Map<Integer,CouponsDto>>(true,coupons,StatusDto.SUCCESS,"");
		}
		
		Integer id = null;
		if(enableInfo.getTimeStart().getTime() >= id1TimeStart.getTime() && enableInfo.getTimeStart().getTime() < id1TimeEnd.getTime()) {
			id = 1;
		}else if(enableInfo.getTimeStart().getTime() >= id2TimeStart.getTime() && enableInfo.getTimeStart().getTime() < id2TimeEnd.getTime()) {
			id = 2;
		}else if(enableInfo.getTimeStart().getTime() >= id3TimeStart.getTime() && enableInfo.getTimeStart().getTime() < id3TimeEnd.getTime()) {
			id = 3;
		}
		CouponsDto couponsDto = new CouponsDto();
		List<CouponDto> couponsCanR = Lists.newArrayList();
		//查询是否已领取完
		for(RecieveCoupon coupon:enableInfo.getCoupons()) {
			CouponDto couponDto = getCouponInfo(coupon.getCouponTemplateCode());
			if(couponDto == null) continue;
			Integer hasRecieveCount = (Integer) CouponReceiveRedis.RECIEVE_COUNT.get(coupon.getId());
			if(hasRecieveCount == null) {
				CouponRecieve param = new CouponRecieve();
				param.setCouponTemplate(coupon.getCouponTemplateCode());
				param.setCreateTimeFrom(enableInfo.getTimeStart());
				param.setCreateTimeTo(enableInfo.getTimeEnd());
				hasRecieveCount = recieveDao.findCount(param);
				CouponReceiveRedis.RECIEVE_COUNT.set(coupon.getId(), hasRecieveCount);
			}
			
			Integer recieveCount = ObjectUtils.isNotEmptyOrNull(customerId)?(Integer) CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.get(customerId+coupon.getCouponTemplateCode()):null;
			if(recieveCount == null && ObjectUtils.isNotEmptyOrNull(customerId)) {
				CouponRecieve param = new CouponRecieve();
				param.setCouponTemplate(coupon.getCouponTemplateCode());
				param.setCustomerId(customerId);
				recieveCount = recieveDao.findCount(param);
				CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.set(customerId+coupon.getCouponTemplateCode(),recieveCount);
			}
			
			Integer cStatus = 1;
			if(hasRecieveCount >= coupon.getLimit()) {
				cStatus = CouponsDto.STATUS_RECIEVE_COMPLETE;
			}else {
				cStatus = CouponsDto.STATUS_CAN_RECIEVE;
			}
			cStatus = recieveCount != null && recieveCount > 0?CouponsDto.STATUS_HAS_RECIEVED:cStatus;
			couponDto.setStatus(cStatus);
			couponDto.setId(coupon.getId());
			String cid = coupon.getId();
			String productCode = cid.equals("1")?"DTCP005":cid.equals("2")?"DTCP006":cid.equals("3")?"DTCP003":"DTCP004";
			couponDto.setProductCode(productCode);
			couponsCanR.add(couponDto);
		}
		couponsDto.setCoupons(couponsCanR);
		couponsDto.setCanRecieve(1);
		coupons.put(id,couponsDto);
		
		return new StatusObjDto<Map<Integer,CouponsDto>>(true,coupons,StatusDto.SUCCESS,"");
	}

	@Override
	public StatusObjDto<Integer> recieve(String couponId,String customerId,String userId) {
		if(ObjectUtils.isEmptyOrNull(couponId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("couponId");
		}
		if(ObjectUtils.isEmptyOrNull(customerId)) {
			throw BizException.CUSTOMER_NOT_LOGIN_ERROR;
		}
		
		List<RecieveInfo> infos = activity.getRecieveInfos();
		RecieveInfo enableInfo = null;
		RecieveCoupon chooseCoupon = null;
		Date now = new Date();
		for(int i = 0;i < infos.size();i++) {
			RecieveInfo info = infos.get(i);
			for(RecieveCoupon coupon:info.getCoupons()) {
				if(now.getTime() >= info.getTimeStart().getTime() && now.getTime() < info.getTimeEnd().getTime() && 
						coupon.getId().equals(couponId) ) {
					enableInfo = info;
					chooseCoupon = coupon;break;
				}
			}
		}
		if(chooseCoupon == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("无效领取");
		}

		//查询是否领取过奖品
		Integer reCount = (Integer) CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.get(customerId+chooseCoupon.getCouponTemplateCode());
		if(reCount == null) {
			CouponRecieve recieve = new CouponRecieve();
			recieve.setCustomerId(customerId);
			reCount = recieveDao.findCount(recieve);
			CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.set(customerId+chooseCoupon.getCouponTemplateCode(), reCount);
		}
		if(reCount > 0) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("已领取过优惠券");
		}
		
		//判断剩余数量
		Integer hasRecieveCount = (Integer) CouponReceiveRedis.RECIEVE_COUNT.get(chooseCoupon.getId());
		if(hasRecieveCount == null) {
			CouponRecieve param = new CouponRecieve();
			param.setCouponTemplate(chooseCoupon.getCouponTemplateCode());
			param.setCreateTimeFrom(enableInfo.getTimeStart());
			param.setCreateTimeTo(enableInfo.getTimeEnd());
			hasRecieveCount = recieveDao.findCount(param);
			CouponReceiveRedis.RECIEVE_COUNT.set(chooseCoupon.getId(), hasRecieveCount);
		}
		if(hasRecieveCount >= chooseCoupon.getLimit()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("该时间段已领完");
		}
		//领取优惠券
		Integer status = 0;
		StatusObjDto<CouponInfo> recieveStatus = couponBiz.receiveAvailableCoupon(chooseCoupon.getCouponTemplateCode(), customerId, userId);
		if(recieveStatus.isOk()) {//保存领取记录
			status = 1;
			CouponInfo cinfo = recieveStatus.getObj();
			CouponRecieve record = new CouponRecieve();
			record.setCouponCode(cinfo.getCode());
			record.setCouponTemplate(chooseCoupon.getCouponTemplateCode());
			record.setCustomerId(customerId);
			record.setFundAccount(customerId);
			record.setUserId(userId);
			record.setCreateTime(new Date());
			recieveDao.insert(record);
			reCount += 1;
			CouponReceiveRedis.CUSTOMER_RECIEVE_STATUS.set(customerId+chooseCoupon.getCouponTemplateCode(), reCount);
		}else {
			//log.error("领取优惠券失败:"+JsonUtils.object2JSON(recieveStatus));
		}
		return new StatusObjDto<Integer>(true,status,StatusDto.SUCCESS,"");
	}
	
	/*
	private CouponDto getCouponInfo(RecieveCoupon coupon) {
		CouponDto couponDto = (CouponDto) CouponReceiveRedis.COUPON_INFO.get(coupon.getId());
		if(couponDto == null) {//查询优惠券信息
			StatusObjDto<CouponInfo>  couponStatus = couponBiz.couponInfo(coupon.getCouponTemplateCode(), null);
			log.debug("---------------"+JsonUtils.object2JSON(couponStatus));
			if(couponStatus.isOk()) {
				CouponInfo couponInfo = couponStatus.getObj();
				couponDto = new CouponDto();
				BeanUtils.copyProperties(couponInfo, couponDto);
				CouponReceiveRedis.COUPON_INFO.set(coupon.getId(),couponDto);
			}
		}
		return couponDto;
	}
	*/
	
	private CouponDto getCouponInfo(String couponTemplate) {
		CouponDto couponDto = (CouponDto) CouponReceiveRedis.COUPON_INFO.get(couponTemplate);
		if(couponDto == null) {//查询优惠券信息
			StatusObjDto<CouponInfo>  couponStatus = couponBiz.couponInfo(couponTemplate, null);
			//log.debug("---------------"+JsonUtils.object2JSON(couponStatus));
			if(couponStatus.isOk()) {
				CouponInfo couponInfo = couponStatus.getObj();
				couponDto = new CouponDto();
				BeanUtils.copyProperties(couponInfo, couponDto);
				CouponReceiveRedis.COUPON_INFO.set(couponTemplate,couponDto);
			}
		}
		return couponDto;
	}
}
