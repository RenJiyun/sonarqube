package com.wlzq.activity.quant.biz;

import java.util.List;

import com.wlzq.activity.quant.model.ActQuantTeam;
import com.wlzq.activity.quant.model.ActQuantTeamStratDto;
import com.wlzq.core.Page;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 建模大赛
 * @author zhaozx
 *
 */
public interface ActQuantBiz {

	/**
	 * 策略评估数据回传
	 * @param busiparams
	 * @return
	 */
	StatusObjDto<Integer> strategyEvaluation(String busiparams);

	/**
	 * 查找队伍
	 * @param teamId
	 * @param leader
	 * @param orderType TODO
	 * @param accountType
	 * @return
	 */
	StatusObjDto<List<ActQuantTeamStratDto>> findteams(String teamId, String leader, Integer orderType, Integer accountType, Page page);

	/**
	 * 
	 * @param mobile
	 * @return
	 */
	StatusObjDto<Integer> voteStatus(String mobile);

	/**
	 * 投票
	 * @param mobile
	 * @param openId
	 * @param userId
	 * @param teamId
	 * @return
	 */
	StatusObjDto<Integer> vote(String mobile, String openId, String userId, String teamId);

	/**
	 * 人气榜
	 * @param teamIdOrLeader
	 * @param page
	 * @return
	 */
	StatusObjDto<List<ActQuantTeam>> hotteams(String teamIdOrLeader, Page page);
	
}
