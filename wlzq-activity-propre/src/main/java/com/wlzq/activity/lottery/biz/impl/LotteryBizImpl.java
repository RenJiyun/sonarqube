package com.wlzq.activity.lottery.biz.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActLotteryBiz;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dto.LotteryDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.lottery.biz.LotteryBiz;
import com.wlzq.activity.springfestival.dto.LotteryPreviewDto;
import com.wlzq.activity.springfestival.prizeenum.PrizeEnum;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

@Service
public class LotteryBizImpl extends ActivityBaseBiz implements LotteryBiz {

	public final static String ACTIVITY_CODE = new String("ACTIVITY.NEWYEARSDAY.2021.LOTTERY");
	/** 空奖品数量(谢谢参与) */
	private final static Integer EMPTY_PRIZE_COUNT = new Integer(0); 
	/** 奖品redis key*/
	private static final String LOTTERY_PRIZES_KEY = new String("all");
	/** 奖品redis key*/
	private static final String THANKS = new String("谢谢参与");
	/** 每天抽奖次数*/
	private static final Integer DAY_LOTTERY_COUNT = 1;
	/** 最大中奖次数*/
	private static final Integer MAX_HIT_COUNT = 4;
	
	@Autowired 
	private ActLotteryBiz actLotteryBiz;
	@Autowired
	private ActPrizeBiz actPrizeBiz;

	@Override
	public StatusObjDto<LotteryPreviewDto> lotteryPreview(String userId, String openId, String customerId) {
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			if( !isValidAct.getCode().equals(219) || !isValidAct.getCode().equals(210)) {//不在活动时间
				LotteryPreviewDto dto = new LotteryPreviewDto();
				dto.setLeftDays(0);
				dto.setHasLottery(0);
				dto.setLeft(0);
				return new StatusObjDto<LotteryPreviewDto>(true, dto, StatusDto.SUCCESS, "");
			}
			return new StatusObjDto<LotteryPreviewDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
//		if (ObjectUtils.isEmptyOrNull(userId)) {
//			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
//		}
		Date timeStart = DateUtils.getDayStart(new Date());
		Date timeEnd = DateUtils.getDayEnd(new Date());
		int lotteryCount = actLotteryBiz.lotteryCount(ACTIVITY_CODE, null, null, customerId, CodeConstant.CODE_NO, timeStart, timeEnd);
		int left = DAY_LOTTERY_COUNT - lotteryCount;
		Activity act = findActivity(ACTIVITY_CODE);
		Integer leftDays = DateUtils.daysBetween(new Date(), act.getDateTo()) + 1;
		LotteryPreviewDto dto = new LotteryPreviewDto();
		dto.setLeftDays(leftDays);
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
//		if (ObjectUtils.isEmptyOrNull(userId)) {
//			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
//		}
		Date timeStart = DateUtils.getDayStart(new Date());
		Date timeEnd = DateUtils.getDayEnd(new Date());
		/**当日抽奖次数**/
		int lotteryCount = actLotteryBiz.lotteryCount(ACTIVITY_CODE, null, null, customerId, CodeConstant.CODE_NO, timeStart, timeEnd);
		int left = DAY_LOTTERY_COUNT - lotteryCount;
		
		/**无抽奖次数**/
		if (left <= 0) {
			throw ActivityBizException.FINSECTION_ZERO_LOTTERY_COUNT;
		}
		
		List<ActPrize> prizes = actPrizeBiz.findPrize(ACTIVITY_CODE, customerId, null, null, new String(), null, null);
		List<String> prizeCodes = prizes.stream().map(ActPrize :: getCode).collect(Collectors.toList());
		
		LotteryDto dto = new LotteryDto();
		dto.setStatus(LotteryDto.STATUS_NOT_HIT);
		dto.setPrizeName(THANKS);
		/**中奖次数 >= DAY_LOTTERY_COUNT，不再抽奖，直接插入“谢谢参与”抽奖记录**/
		if (prizeCodes.size() >= MAX_HIT_COUNT) {
			actLotteryBiz.saveLottery(ACTIVITY_CODE, userId, openId, customerId, CodeConstant.CODE_NO, null, CodeConstant.CODE_YES, null);
			return new StatusObjDto<LotteryDto>(true, dto, StatusDto.SUCCESS, "");
		}
		
		/**初始化奖品**/
		actLotteryBiz.initPrizes(ACTIVITY_CODE, ActivityRedis.ACT_NEWYEARSDAY_2021_PRIZES, LOTTERY_PRIZES_KEY, EMPTY_PRIZE_COUNT, false);
		dto = actLotteryBiz.lottery(ActivityRedis.ACT_NEWYEARSDAY_2021_PRIZES, LOTTERY_PRIZES_KEY, null);
		actLotteryBiz.saveLottery(ACTIVITY_CODE, userId, openId, customerId, dto.getStatus(), dto.getPrizeId(), CodeConstant.CODE_YES, dto.getRecieveCode());
		if (CodeConstant.CODE_NO.equals(dto.getStatus())) {
			dto.setType(PrizeEnum.PRIZE_THANKS.getType());
			dto.setPrizeName(PrizeEnum.PRIZE_THANKS.getPrizeName());
		} else {
			ActPrize prize = actPrizeBiz.giveOutPrize(ACTIVITY_CODE, "", dto.getPrizeId(), null, userId, openId, customerId, mobile);
			
			if(prize == null) {
				dto.setType(PrizeEnum.PRIZE_THANKS.getType());
				dto.setPrizeName(PrizeEnum.PRIZE_THANKS.getPrizeName());
			}else {
				//移除中奖奖品
				ActivityRedis.ACT_NEWYEARSDAY_2021_PRIZES.sremove(LOTTERY_PRIZES_KEY, "1-"+prize.getId());
				
				PrizeEnum pe = PrizeEnum.getByPrizeCode(prize.getCode());
				dto.setStatus(LotteryDto.STATUS_HIT);
				Integer prizeType = pe != null?pe.getType():prize.getType();
				String prizeName = pe != null?pe.getPrizeName():prize.getName();
				dto.setType(prizeType);
				dto.setPrizeName(prizeName);
			}
		}
		return new StatusObjDto<LotteryDto>(true, dto, StatusDto.SUCCESS, "");
	}

}
