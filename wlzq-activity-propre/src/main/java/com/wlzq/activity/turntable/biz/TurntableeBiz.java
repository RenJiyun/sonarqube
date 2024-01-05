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
public interface TurntableeBiz {

	/**
	 * 转盘抽奖
	 * @param userId
	 * @param timestamp 时间戳
	 * @return
	 */
	public StatusObjDto<TurntableeHitDto> turn(String userId,Long timestamp);

	/**
	 * 用户奖品记录
	 * @param activity TODO
	 * @param userId
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<UserPrizeDto>> userPrizes(String activity,String userId,Integer start, Integer end);
	
	/**
	 * 游戏中奖列表
	 * @param activity TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<TurntableePrizeDto>> prizes(String activity,Integer start, Integer end);

	/**
	 * 未使用奖品数
	 * @param activity TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<Integer> findNotUsePrizeCount(String userId, String activity);

	/**
	 * 分享
	 * @param userId
	 * @return
	 */
	public StatusObjDto<Integer> share(String userId);

}
