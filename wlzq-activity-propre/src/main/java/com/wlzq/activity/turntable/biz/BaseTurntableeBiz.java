package com.wlzq.activity.turntable.biz;

import java.util.List;

import com.wlzq.activity.base.dto.UserPrizeDto;
import com.wlzq.activity.turntable.dto.TurntableeHitDto;
import com.wlzq.activity.turntable.dto.TurntableePrizeDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 大转盘业务接口
 * @author louie
 *
 */
public interface BaseTurntableeBiz {

	/**
	 * 转盘抽奖
	 * @param userId
	 * @param customerId TODO
	 * @param mobile TODO
	 * @param activity TODO
	 * @param timestamp 时间戳
	 * @return
	 */
	public StatusObjDto<TurntableeHitDto> turn(String userId,String customerId, String mobile, String activity, Long timestamp);

//	/**
//	 * 用户奖品记录
//	 * @param userId
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	public StatusObjDto<List<UserPrizeDto>> userPrizes(String userId,Integer start,Integer end);
//	
//	/**
//	 * 游戏中奖列表
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	public StatusObjDto<List<TurntableePrizeDto>> prizes(Integer start,Integer end);
//
//	/**
//	 * 未使用奖品数
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	public StatusObjDto<Integer> findNotUsePrizeCount(String userId,String activity);

}
