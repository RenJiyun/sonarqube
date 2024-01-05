package com.wlzq.activity.checkin.biz;

import java.util.Date;
import java.util.List;

import com.wlzq.activity.checkin.dto.CheckInDto;
import com.wlzq.activity.checkin.dto.CheckInPrizeDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 签到业务接口
 * @author louie
 *
 */
public interface CheckInBiz {

	/**
	 * 签到情况
	 * @param userId
	 * @param 微信openid
	 * @return
	 */
	public StatusObjDto<CheckInDto> status(String openid);
	
	/**
	 * 检查是否签到
	 * @param openid
	 * @param checkInDate
	 * @return
	 */
	public StatusObjDto<Integer> hasCheckIn(String openid,Date checkInDate);
	
	/**
	 * 签到
	 * @param userId
	 * @param 微信openid
	 * @param type 签到类型,1:正常签到,2:补签
	 * @param fillDate 补签日期，格式：yyyy-MM-dd
	 * @return
	 */
	public StatusObjDto<CheckInPrizeDto>  checkIn(String userId,String openid,Integer type,String fillDate);
	
	/**
	 * 获取补签机会
	 * @param userId 用户id
	 * @param openid
	 * @return
	 */
	public StatusObjDto<Integer> getOpportunity(String userId,String openid);

	/**
	 * 获取签到奖品
	 * @param 微信openid
	 * @return
	 */
	public StatusObjDto<List<CheckInPrizeDto>> getPrize(String openid);
}
