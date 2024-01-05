package com.wlzq.activity.expoturntable.biz;

import java.util.List;
import java.util.Map;

import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusObjDto;

public interface FinanceExpo2019Biz {

	/**
	 * 签到
	 * @param user
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> signIn(AccTokenUser user);
	
	/**
	 * 获取签到列表
	 * @param maxOrder
	 * @param maxLength
	 * @return
	 */
	public StatusObjDto<List<Map<String, Object>>> signinList(Integer maxOrder, Integer maxLength);
	
	/**
	 * 游戏开关
	 * @param switchType
	 * @param scene
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> gameSwitch(Integer switchType, Integer scene);
	
	/**
	 * 投票
	 * @param user
	 * @param scene
	 * @param counts
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> gameShake(AccTokenUser user, Integer scene, String counts, Integer maxLength);

	/**
	 * 摇一摇走势
	 * @param scene
	 * @param length
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> shakeTrend(Integer scene, Integer length);

	/**
	 * 成绩
	 * @param scene
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> shakeRank(Integer scene);

	public StatusObjDto<Map<String, Object>> personelRecord(Integer scene, AccTokenUser user);

	public StatusObjDto<Map<String, Object>> playerInfo(String playerIds);
}
