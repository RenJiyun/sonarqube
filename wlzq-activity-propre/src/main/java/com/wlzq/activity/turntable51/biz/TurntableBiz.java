package com.wlzq.activity.turntable51.biz;

import com.wlzq.activity.turntable51.dto.TurntableHitDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 大转盘业务接口
 * @author louie
 *
 */
public interface TurntableBiz {

	/**
	 * 转盘抽奖
	 * @param userId
	 * @param timestamp 时间戳
	 * @return
	 */
	public StatusObjDto<TurntableHitDto> turn(String userId,String openId,String customerId,Long timestamp);

}
