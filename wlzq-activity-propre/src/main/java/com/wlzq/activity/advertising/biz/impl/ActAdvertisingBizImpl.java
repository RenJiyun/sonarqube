package com.wlzq.activity.advertising.biz.impl;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.advertising.biz.ActAdvertisingBiz;
import com.wlzq.activity.advertising.dao.ActAdvertisingDao;
import com.wlzq.activity.advertising.model.ActAdvertising;

import com.wlzq.activity.advertising.redis.advertisingRedis;
import com.wlzq.activity.guess.dto.MarketUserDto;
import com.wlzq.common.constant.CheckcodeTypeE;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.common.utils.RegxUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.base.CheckcodeBiz;
import com.wlzq.remote.service.common.base.PushBiz;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class ActAdvertisingBizImpl implements ActAdvertisingBiz {

	@Autowired
	private ActAdvertisingDao actAdvertisingDao;

	@Autowired
	private CheckcodeBiz checkcodeBiz;


	@Autowired
	private PushBiz pushBiz;

	//验证码发送模板
	private static final String SMS_CHECKCODE_TEMPLATE = "ACTIVITY.SALE.CHECKCODE";
	private static final String ACTIVITY_MARKET_MOBILE = "activity.market.mobile";
	private static final String SALE_PAGEENCODING = "sale.page.pageencoding";
	private static final String ACCOUNT_ADVERT_REMIND = "ACCOUNT.ADVERT.REMIND";
	public static final String  ACT_ACTIVITY_INC = "incADVERTISING:ADVERTISING:INC";
	public static final String ACCOUNT_ADVERT_CUSTOM="ACCOUNT.ADVERT.CUSTOM";



	@Override
	public StatusDto saveActAdvertising(String phoneNumber, String pageEncoding,String wlzqstatId,String imageCheckCode,String checkCode,String deviceId,
										String pgm,String kw,String grp,String pln,String chn) {

//		if(ObjectUtils.isEmptyOrNull(imageCheckCode)) {
//			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("验证码为空!");
//		}

		if(ObjectUtils.isEmptyOrNull(checkCode)) {
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("验证码为空!");
		}


		if(ObjectUtils.isEmptyOrNull(pageEncoding)) {
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("页面编码为空!");
		}

		if(ObjectUtils.isEmptyOrNull(wlzqstatId)) {
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("wlzqstatId为空!");
		}

		if(ObjectUtils.isEmptyOrNull(phoneNumber)) {
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("手机号为空!");
		}

		if(ObjectUtils.isEmptyOrNull(chn)) {
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("广告渠道为空!");
		}

		if(!RegxUtils.isMobile(phoneNumber)) {
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("手机号无效!");
		}

//		StatusDto result = checkcodeBiz.verityImageCheckcode(CheckcodeTypeE.USER_REGIST, wlzqstatId, imageCheckCode, deviceId);
//		if(!result.isOk()) {
//			return new StatusDto(false,1,result.getMsg());
//		}

		ActAdvertising  act =  new ActAdvertising();
		act.setMobile(phoneNumber);
		act.setChn(chn);
		List<ActAdvertising> actAdvertisingList = actAdvertisingDao.findList(act);
		if(actAdvertisingList.size() > 0){
			throw ActivityBizException.COMMON_CUSTOMIZE_ERROR.format("您已提交成功，稍后专属服务人员将与您联系!");
		}

		StatusDto verifyStatus = checkcodeBiz.verifyCheckcode(CheckcodeTypeE.BYTED_SALE, phoneNumber, checkCode, "");
		if(!verifyStatus.isOk()) {
			return new StatusObjDto<>(false,verifyStatus.getCode(),verifyStatus.getMsg());
		}

		List<String> pageencodinglist = AppConfigUtils.getList(SALE_PAGEENCODING,",");
		List<Object> params = new ArrayList<>();
		params.add(phoneNumber);
		for(String str: pageencodinglist){
			String pageNum = str.split(":")[0];
			String pageEd = str.split(":")[1];
			if(  "1".equals(pageNum)){
				params.add(pageEd);
			}else if( "2".equals(pageNum)){
				params.add(pageEd);
			}else if( "3".equals(pageNum)){
				params.add(pageEd);
			}else if( "4".equals(pageNum)){
				params.add(pageEd);
			}else if("5".equals(pageNum)){
				params.add(pageEd);
			}else if("6".equals(pageNum)){
				params.add(pageEd);
			}else if("7".equals(pageNum)){
				params.add(pageEd);
			}
		}


		MarketUserDto marketUserDto = new MarketUserDto();
		marketUserDto.setChn(chn);
		List<MarketUserDto> marketUserDtoList = actAdvertisingDao.findListMarketUser(marketUserDto);

		ActAdvertising actAdvertising = new ActAdvertising();
		if(marketUserDtoList.size() > 0) {
			int index = (int) (advertisingRedis.ACT_ACTIVITY_INFO.inc(ACT_ACTIVITY_INC) % marketUserDtoList.size());
			String account = marketUserDtoList.get(index).getAccount();
			String marketMobile = marketUserDtoList.get(index).getMarketMobile();
			actAdvertising.setMarketAccount(account);
			actAdvertising.setMarketMobile(marketMobile);
		}

		actAdvertising.setId("");
		actAdvertising.setChn(chn == null?"":chn);
		actAdvertising.setGrp(grp == null?"":grp);
		actAdvertising.setKw(kw == null?"":kw);
		actAdvertising.setPgm(pgm == null?"":pgm);
		actAdvertising.setPln(pln == null?"":pln);
		actAdvertising.setPageEncoding(pageEncoding);
		actAdvertising.setRemarks("");
		actAdvertising.setFollowStatus(0);
		actAdvertising.setMobile(phoneNumber);
		actAdvertising.setCreateTime(new Date());
		actAdvertisingDao.insert(actAdvertising);

		if(marketUserDtoList.size() > 0) {
			pushBiz.sendSms(ACCOUNT_ADVERT_REMIND, actAdvertising.getMarketMobile(), params);
			pushBiz.sendSms(ACCOUNT_ADVERT_CUSTOM, phoneNumber, params);
		}


		return new  StatusDto(true,0,"");
	}

	@Override
	public StatusDto sendCheckCode(String mobile) {
		if(ObjectUtils.isEmptyOrNull(mobile)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
		}
		if(!RegxUtils.isMobile(mobile)) {
			throw BizException.COMMON_PARAMS_IS_ILLICIT.format("mobile");
		}
		return checkcodeBiz.sendCheckcode(SMS_CHECKCODE_TEMPLATE, CheckcodeTypeE.BYTED_SALE, mobile, "");
	}
}
