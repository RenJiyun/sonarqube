package com.wlzq.activity.voteworks.biz;

import java.util.List;
import java.util.Map;

import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.dto.WinDto;
import com.wlzq.activity.voteworks.dto.LikeInfoDto;
import com.wlzq.activity.voteworks.dto.LikeResultDto;
import com.wlzq.activity.voteworks.dto.MessagesDto;
import com.wlzq.activity.voteworks.dto.VoteLotteryDto;
import com.wlzq.activity.voteworks.dto.VoteOverviewDto;
import com.wlzq.activity.voteworks.model.VoteWorks;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 818视频作品投票业务接口
 * @author louie 
 *
 */
public interface VoteWorksBiz {
	
	/**
	 * 概览
	 * @param userId TODO
	 * @param openId
	 * @param timestamp
	 * @return
	 */
	public StatusObjDto<VoteOverviewDto> overview(String userId, String openId, Long timestamp);

	/**
	 * 点赞是否弹窗提示设置
	 * @param openId
	 * @param popStatus 是否弹窗，0：否，1：是
	 * @return
	 */
	public StatusObjDto<Integer> popSet(String openId, Integer popStatus);
	
	/**
	 * 作品列表
	 * @param tag TODO
	 * @param name
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<VoteWorks>> works(Integer tag,String name,Integer start, Integer end);
	
	/**
	 * 作品详情
	 * @param no
	 * @param tag TODO
	 * @param userId TODO
	 * @return
	 */
	public StatusObjDto<VoteWorks> worksDetail(String no,Integer tag, String userId, String openId, Long timestamp);

	/**
	 * 留言
	 * @param no
	 * @param tag TODO
	 * @param userId
	 * @param openId
	 * @param message
	 * @return
	 */
	public StatusObjDto<String> leaveMessage(String no,Integer tag,String userId,String openId, String content);
	
	/**
	 * 留言列表
	 * @param no
	 * @param tag TODO
	 * @param minId 留言截止ID
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<MessagesDto> messages(String no,Integer tag,Integer isReturnPortrait,Long minId,Integer start, Integer end);

	/**
	 * 是否可点赞
	 * @param openId
	 * @param tag TODO
	 * @return
	 */
	public StatusObjDto<Map<String, Object>> canLike(String openId, Integer tag);
	
	/**
	 * 点赞
	 * @param no
	 * @param tag TODO
	 * @param userId
	 * @param openId
	 * @param timestamp
	 * @return 是否可抽奖，0：否，1：是
	 */
	public StatusObjDto<LikeResultDto> like(String no,Integer tag,String userId,String openId, Long timestamp);
	
	/**
	 * 抽奖
	 * @param userId
	 * @param openId
	 * @param lotteryCode
	 * @return
	 */
	public StatusObjDto<VoteLotteryDto> lottery(String userId,String openId,String lotteryCode);
	
	/**
	 * 留发送奖励短信手机号
	 * @param userId
	 * @param openId
	 * @param lotteryCode
	 * @param mobile
	 * @return
	 */
	public StatusDto leaveMobile(String userId,String openId, String lotteryCode,String mobile);

	/**
	 * 排名
	 * @param tag TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<List<VoteWorks>>  ranking(Integer tag,Integer start, Integer end);

	/**
	 * 点赞记录
	 * @param no
	 * @param tag TODO
	 * @param start
	 * @param end
	 * @return
	 */
	public StatusObjDto<LikeInfoDto>  likeRecord(String no,Integer tag,Integer start, Integer end);
	
	/**
	 * 中奖列表
	 * @param start
	 * @param end
	 * @return
	 */
	StatusObjDto<List<WinDto>> prizes(Integer start,Integer end);
	
	/**
	 * 我的奖品列表
	 * @param userId TODO
	 * @param openId TODO
	 * @return
	 */
	StatusObjDto<List<MyPrizeDto>> myPrizes(String userId,String openId);
	
	/**
	 * 发送中奖消息
	 * @return
	 */
	StatusDto sendRecieveMessage();
}
