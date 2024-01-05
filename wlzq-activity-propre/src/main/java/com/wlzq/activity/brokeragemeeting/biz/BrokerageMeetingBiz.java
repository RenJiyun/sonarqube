package com.wlzq.activity.brokeragemeeting.biz;

import java.util.List;
import java.util.Map;

import com.wlzq.activity.brokeragemeeting.dto.BrokerageMeetingVoteResultDto;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingPersonnel;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingWord;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusObjDto;

public interface BrokerageMeetingBiz {

	/**
	 * 签到
	 * @param user
	 * @return
	 */
	public StatusObjDto<Map<String, Object> > signIn(AccTokenUser user);

	/**
	 * 揭幕
	 * @param user
	 * @param name
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> unveiling(AccTokenUser user, String name);

	/**
	 * 是否揭幕完毕
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> isUnveilDone();

	/**
	 * 获取签到列表
	 * @param maxOrder
	 * @param maxLength
	 * @return
	 */
	public StatusObjDto<List<ActBrokeragemeetingPersonnel>> signinList(Integer maxOrder, Integer maxLength);

	/**
	 * 游戏开关
	 * @param switchType
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> gameSwitch(Integer switchType);

	/**
	 * 投票
	 * @param user
	 * @param votewords
	 * @param voteCounts
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> gameVote(AccTokenUser user, String votewords, String voteCounts);

	/**
	 * 游戏投票结果
	 * @param maxLength
	 * @return
	 */
	public StatusObjDto<BrokerageMeetingVoteResultDto> voteResult(Integer maxLength);

	
	/**
	 * 游戏投票手速
	 * @param maxLength
	 * @return
	 */
	public StatusObjDto<List<Map<String, Object>>> voteSpeed(Integer maxLength);

	/**
	 * 游戏投票词语
	 * @return
	 */
	public StatusObjDto<List<ActBrokeragemeetingWord>> wordList();

	/**
	 * 游戏个人手速排名
	 * @param user
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> personSpeed(AccTokenUser user);

}
