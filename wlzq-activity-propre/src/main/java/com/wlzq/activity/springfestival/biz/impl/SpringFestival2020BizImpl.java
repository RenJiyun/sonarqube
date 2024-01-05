package com.wlzq.activity.springfestival.biz.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActLotteryBiz;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActShareBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.springfestival.biz.SpringFestival2020Biz;
import com.wlzq.activity.springfestival.dto.LotteryPreviewDto;
import com.wlzq.activity.springfestival.prizeenum.PrizeEnum;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;

@Service
public class SpringFestival2020BizImpl extends ActivityBaseBiz implements SpringFestival2020Biz {

	public final static String ACTIVITY_CODE = new String("ACTIVITY.SPRINGFESTIVAL.2020.LOTTERY");
	public final static String ACTIVITY_CODE_RECEVIE = new String("ACTIVITY.SPRINGFESTIVAL.2020.RECEVIE");
	private final static Integer EMPTY_PRIZE_COUNT = new Integer(3000);
	/** 奖品redis key*/
	private static final String LOTTERY_PRIZES_KEY = new String("all");
	/** 奖品redis key*/
	private static final String THANKS = new String("谢谢参与");
	
	private final static Integer STATUS_YES = new Integer(1);
	private final static Integer STATUS_NO = new Integer(0);
	
	@Autowired 
	private ActLotteryBiz actLotteryBiz;
	@Autowired 
	private ActShareBiz actShareBiz;
	@Autowired
	private ActPrizeBiz actPrizeBiz;
	
	@Override
	public StatusObjDto<LotteryPreviewDto> lotteryPreview(String userId, String openId, String customerId) {
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<LotteryPreviewDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		if (ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		Date timeStart = DateUtils.getDayStart(new Date());
		Date timeEnd = DateUtils.getDayEnd(new Date());
		int lotteryCount = actLotteryBiz.lotteryCount(ACTIVITY_CODE, userId, openId, customerId, STATUS_YES, timeStart, timeEnd);
		int shareCount = actShareBiz.shareCount(ACTIVITY_CODE, userId, openId, customerId, STATUS_YES, timeStart, timeEnd);
		
		int shareIncLottery = shareCount > 0 ? 1 : 0;
		int left = 1 + shareIncLottery - lotteryCount;
		
		LotteryPreviewDto dto = new LotteryPreviewDto();
		dto.setHasLottery(lotteryCount);
		dto.setLeft(left);
		return new StatusObjDto<LotteryPreviewDto>(true, dto, StatusDto.SUCCESS, "");
	}

	@Override
	@Transactional
	public StatusObjDto<LotteryDto> lottery(String userId, String openId, String customerId, String mobile) {
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<LotteryDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		if (ObjectUtils.isEmptyOrNull(userId)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		Date timeStart = DateUtils.getDayStart(new Date());
		Date timeEnd = DateUtils.getDayEnd(new Date());
		/**当日抽奖次数**/
		int dailyCount = actLotteryBiz.lotteryCount(ACTIVITY_CODE, userId, openId, customerId, STATUS_YES, timeStart, timeEnd);
		int shareCount = actShareBiz.shareCount(ACTIVITY_CODE, userId, openId, customerId, STATUS_YES, timeStart, timeEnd);
		int shareIncLottery = shareCount > 0 ? 1 : 0;
		int left = 1 + shareIncLottery - (int)dailyCount;
		
		/**无抽奖次数**/
		if (left <= 0) {
			throw ActivityBizException.FINSECTION_ZERO_LOTTERY_COUNT;
		}
		
		
		List<ActPrize> prizes = actPrizeBiz.findPrize(ACTIVITY_CODE, null, userId, null, new String(), null, null);
		List<String> prizeCodes = prizes.stream().map(ActPrize :: getCode).collect(Collectors.toList());
		
		LotteryDto dto = new LotteryDto();
		dto.setStatus(LotteryDto.STATUS_NOT_HIT);
		dto.setPrizeName(THANKS);
		/**中奖次数 >= 3，不再抽奖，直接插入“谢谢参与”抽奖记录**/
		if (prizeCodes.size() >= 3) {
			actLotteryBiz.saveLottery(ACTIVITY_CODE, userId, openId, customerId, STATUS_NO, null, STATUS_YES, null);
			return new StatusObjDto<LotteryDto>(true, dto, StatusDto.SUCCESS, "");
		}
		
		/**初始化奖品**/
		actLotteryBiz.initPrizes(ACTIVITY_CODE, ActivityRedis.ACT_SPRINGFESTIVAL_2020_PRIZES, LOTTERY_PRIZES_KEY, EMPTY_PRIZE_COUNT, false);
		dto = actLotteryBiz.lottery(ActivityRedis.ACT_SPRINGFESTIVAL_2020_PRIZES, LOTTERY_PRIZES_KEY, prizeCodes);
		actLotteryBiz.saveLottery(ACTIVITY_CODE, userId, openId, customerId, dto.getStatus(), dto.getPrizeId(), STATUS_YES, dto.getRecieveCode());
		if (STATUS_NO.equals(dto.getStatus())) {
			dto.setType(PrizeEnum.PRIZE_THANKS.getType());
			dto.setPrizeName(PrizeEnum.PRIZE_THANKS.getPrizeName());
		} else {
			if (PrizeEnum.getByName(dto.getPrizeName()) == null) {
				dto = new LotteryDto();
				dto.setStatus(CodeConstant.CODE_NO);
				dto.setType(PrizeEnum.PRIZE_THANKS.getType());
				dto.setPrizeName(PrizeEnum.PRIZE_THANKS.getPrizeName());
			} else {
				dto.setType(PrizeEnum.getByName(dto.getPrizeName()).getType());
				ActPrize prize = actPrizeBiz.giveOutPrize(ACTIVITY_CODE, "", dto.getPrizeId(), null, userId, openId, customerId, mobile);
				if(prize == null) {
					throw BizException.COMMON_CUSTOMIZE_ERROR.format("领取失败");
				}
			}
		}
		return new StatusObjDto<LotteryDto>(true, dto, StatusDto.SUCCESS, "");
	}

	public static void openDateCheck(Date openDate) {
		Date year2019Start = DateUtils.parseDate("2019-01-01", "yyyy-MM-dd");
//		Date year2019End = DateUtils.getYearEnd(year2019Start);
		if (openDate == null || openDate.before(year2019Start)) {
			throw ActivityBizException.ACT_ILL_OPENDATE;
		}
	}
	
	
}
