package com.wlzq.activity.voteworks.biz.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.ActivityConstant;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.dto.WinDto;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.redeem.dao.RedeemDao;
import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.activity.voteworks.biz.VoteWorksBiz;
import com.wlzq.activity.voteworks.dao.VoteWorksDao;
import com.wlzq.activity.voteworks.dao.VoteWorksHotDao;
import com.wlzq.activity.voteworks.dao.VoteWorksLikeDao;
import com.wlzq.activity.voteworks.dao.VoteWorksLotteryDao;
import com.wlzq.activity.voteworks.dao.VoteWorksMessageDao;
import com.wlzq.activity.voteworks.dto.LikeDto;
import com.wlzq.activity.voteworks.dto.LikeInfoDto;
import com.wlzq.activity.voteworks.dto.LikeResultDto;
import com.wlzq.activity.voteworks.dto.MessagesDto;
import com.wlzq.activity.voteworks.dto.VoteLotteryDto;
import com.wlzq.activity.voteworks.dto.VoteOverviewDto;
import com.wlzq.activity.voteworks.model.VoteWorks;
import com.wlzq.activity.voteworks.model.VoteWorksHot;
import com.wlzq.activity.voteworks.model.VoteWorksLike;
import com.wlzq.activity.voteworks.model.VoteWorksLottery;
import com.wlzq.activity.voteworks.model.VoteWorksMessage;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.common.utils.RegxUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.basesensitiveword.utils.SensitiveWordFilterUtil;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import com.wlzq.service.base.sys.utils.ImageUtils;

import cn.hutool.core.text.StrSpliter;

/**
 * 818视频作品投票业务接口实现
 * @author louie
 *
 */
@Service
@DependsOn({"springApplicationContext"})
@PropertySource("classpath:application.properties")
public class VoteWorksBizImpl extends ActivityBaseBiz implements VoteWorksBiz {
	
	private Logger logger = LoggerFactory.getLogger(VoteWorksBizImpl.class);
	
	/** 活动编码*/
	private String ACTIVITY_CODE = ActivityConstant.VOTEWORKS_ACTIVITYCODE;
	/** 达到该点赞次数可抽奖*/
	private static final Integer LOTTER_LIKE_COUNT =10;
	
	private static final String VOTEWORK_LIKE_LIMIT = "VOTEWORK_LIKE_LIMIT"; 
	/** 奖品redis key*/
	private static final String LOTTERY_PRIZES_KEY = "all";
	/** 首页热度初始值*/
	private static final Long HOT_INIT=1998l;
			
	@Autowired
	private VoteWorksDao worksDao;

	@Autowired
	private VoteWorksHotDao hotDao;
	
	@Autowired
	private VoteWorksMessageDao messageDao;
	
	@Autowired
	private VoteWorksLikeDao likeDao;
	
	@Autowired
	private VoteWorksLotteryDao lotteryDao;
	
	@Autowired
	private RedeemDao redeemDao;

	private Integer emptyPrizeCount = ActivityConstant.VOTEWORKS_NOPRIZESCOUNT;

	@Autowired
	private	ActPrizeBiz actPrizeBiz;

//    @Autowired
//    private WechatUserBiz userBiz;
	
    @PostConstruct
    public void initPrizesPool() {
    	//initPrizes(true);
    }
    
	@Override
	public StatusObjDto<VoteOverviewDto> overview(String userId, String openId, Long timestamp){
		Activity activity = findActivity(ACTIVITY_CODE);
		if(activity == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("活动不存在");
		}
		//2019-06-17 当前活动的总点赞数
		Long likeCount = worksDao.allLikeCount(ACTIVITY_CODE);
		//2019-06-17 当前活动的总热度
		long hotAll = hotDao.hotAll(ACTIVITY_CODE);
		VoteOverviewDto overview = new VoteOverviewDto();
		overview.setLikeCount(likeCount);
		Long hotCount = hotAll + HOT_INIT;
		overview.setHot(hotCount);
		//点赞是否弹窗提示设置
//		Integer popStatus = getPopStatus(openId);
		overview.setPopStatus(CodeConstant.CODE_NO);
		//倒计时
		Date now = new Date();
		Long leftTime = activity.getDateTo().getTime() - now.getTime();
		overview.setLeftTime(leftTime);
		hotAdd(null, null, userId, timestamp);
		return new StatusObjDto<VoteOverviewDto>(true,overview,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<Integer> popSet(String openId, Integer popStatus){
		if(ObjectUtils.isEmptyOrNull(popStatus)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("popStatus");
		}
		if(ObjectUtils.isEmptyOrNull(openId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
		}
		String date = DateUtils.formate(new Date(), "yyyyMMdd");
		String key = openId + date;
		ActivityRedis.ACT_WORKS_VOTE_POP.set(key, popStatus);
		return new StatusObjDto<Integer>(true,CodeConstant.CODE_YES,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<List<VoteWorks>> works(Integer tag,String name, Integer start, Integer end) {
		VoteWorks param = new VoteWorks();
		param.setName(name);
		param.setTag(tag);
		param.setActivitycode(ACTIVITY_CODE);
		List<VoteWorks> works = worksDao.findList(param);
		for(VoteWorks w:works) {
			//if(ObjectUtils.isNotEmptyOrNull(w.getCover())) {
				w.setCover("");
			//}
			if(ObjectUtils.isNotEmptyOrNull(w.getThumbnail())) {
				w.setThumbnail(ImageUtils.getImageUrl(w.getThumbnail()));
			}
		}
		return new StatusObjDto<List<VoteWorks>>(true,works,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<VoteWorks> worksDetail(String no,Integer tag, String userId, String openId, Long timestamp) {
		if(ObjectUtils.isEmptyOrNull(no)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("no");
		}
		if(ObjectUtils.isEmptyOrNull(openId) && ObjectUtils.isEmptyOrNull(userId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId && userId");
		}
		VoteWorks works = worksDao.findByNo(no, tag, ACTIVITY_CODE);
		if(works == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("作品不存在");
		}
		if(ObjectUtils.isNotEmptyOrNull(works.getCover())) {
			List<String> coverArr = Lists.newArrayList(works.getCover().split(","));
			List<String> covers = Lists.newArrayList();
			for (String each : coverArr) {
				if (ObjectUtils.isNotEmptyOrNull(each)) {
					covers.add(ImageUtils.getImageUrl(each));
				}
			}
			works.setCovers(covers);
		}
		//if(ObjectUtils.isNotEmptyOrNull(works.getThumbnail())) {
			works.setThumbnail("");
		//}
		
		//增加热度
		//hotAdd(no, tag, userId, timestamp);
		return new StatusObjDto<VoteWorks>(true,works,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<String> leaveMessage(String no,Integer tag, String userId, String openId, String content) {
		if(ObjectUtils.isEmptyOrNull(no)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("no");
		}
		if(ObjectUtils.isEmptyOrNull(openId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
		}
		if(ObjectUtils.isEmptyOrNull(content)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("content");
		}
		
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<String>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		
		//敏感词检查
		if (this.isSensitive(content)) {
			return new StatusObjDto<String>(false, null, ActivityBizException.ACTIVITY_SENSITIVE_WORDS.getCode(), "content存在敏感词");
		}
		if (this.isXss(content)) {
			return new StatusObjDto<String>(false, null, ActivityBizException.ACTIVITY_SENSITIVE_WORDS.getCode(), "content疑似存在xss");
		}
		
		VoteWorksMessage worksMessage = new VoteWorksMessage();
		worksMessage.setNo(no);
		worksMessage.setTag(tag);
		worksMessage.setContent(content);
		worksMessage.setUserId(userId);
		worksMessage.setOpenId(openId);
		worksMessage.setCreateTime(new Date());
		
		messageDao.insert(worksMessage);
		
		return new StatusObjDto<String>(true,"",StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<MessagesDto> messages(String no,Integer tag,Integer isReturnPortrait,Long minId, Integer start, Integer end) {
		if(ObjectUtils.isEmptyOrNull(no)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("no");
		}
		Integer count = messageDao.count(no, tag, ACTIVITY_CODE);
		List<VoteWorksMessage> messages = messageDao.findMessages(no, tag, ACTIVITY_CODE, minId, start, end);
		isReturnPortrait = isReturnPortrait == null || !isReturnPortrait.equals(CodeConstant.CODE_YES)?CodeConstant.CODE_NO:isReturnPortrait;
		if(isReturnPortrait.equals(CodeConstant.CODE_NO)) {
			for(VoteWorksMessage message: messages) {
				message.setPortrait("");
				message.setNickname("");
			}
		}
		
		MessagesDto messagesDto = new MessagesDto();
		messagesDto.setInfo(messages);
		messagesDto.setAll(count);
		messagesDto.setTotal(messages.size());
		
		return new StatusObjDto<MessagesDto>(true,messagesDto,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<Map<String, Object>> canLike(String uniqueId, Integer tag) {
		if(ObjectUtils.isEmptyOrNull(uniqueId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("uniqueId");
		}
		Integer canLikeStatus = canDoLike(uniqueId, tag)?CodeConstant.CODE_YES:CodeConstant.CODE_NO;

		VoteWorksLike like = new VoteWorksLike();
		like.setUserId(uniqueId);
		like.setTag(tag);
		List<VoteWorksLike> findList = likeDao.findList(like,ACTIVITY_CODE);
		StringBuffer likesNo = new StringBuffer();
		for(VoteWorksLike vo : findList) {
			likesNo.append(vo.getNo()).append(",");
		}
		
		Map<String,Object> map = Maps.newHashMap();
		map.put("status", canLikeStatus);
		map.put("tag", tag);
		map.put("likes", StrSpliter.split(likesNo.toString(), ',', 0, true, true));
		
		return new StatusObjDto<Map<String, Object>>(true,map,StatusDto.SUCCESS,"");
	}
	
	@Transactional
	@Override
	public StatusObjDto<LikeResultDto> like(String no, Integer tag, String userId, String openId, Long timestamp) {
		if(ObjectUtils.isEmptyOrNull(no)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("no");
		}
		if(ObjectUtils.isEmptyOrNull(tag)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("tag");
		}
//		2021-08-25 open
		if(ObjectUtils.isEmptyOrNull(userId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("userId");
		}
		if(ObjectUtils.isEmptyOrNull(openId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
		}
		/*if(ObjectUtils.isEmptyOrNull(timestamp)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("timestamp");
		}
		if(timestamp.toString().length() != 13) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("时间戳精确到毫秒");
		}*/
		
		List<String> split = StrSpliter.split(no, ',', 0, true, true);
		Set<String> setList = new HashSet<String>(split);
		if(tag == 12 && setList.size() != 6) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("必须选择6个对象，已选"+setList.size()+"个");
		}
		if(tag != 12 && setList.size() != 3) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("必须选择3个对象，已选"+setList.size()+"个");
		}
		
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<LikeResultDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
//		2021-08-修改为用ueserId标识
//		checkCanDoLike(openId, tag);
		if(!canDoLike(userId, tag)){
			throw ActivityBizException.ACT_STAFF_VOTE_LIMIT;
		}
//		2021-08-修改为用ueserId标识
//		if(!checkLikeTime(userId,timestamp)) {
//			throw ActivityBizException.VOTE_WORKS_LIKE_FAST;
//		}
		//检查时间间隔是否有效
//		if(!checkLikeTime(openId,timestamp)) {
//			throw ActivityBizException.VOTE_WORKS_LIKE_FAST;
//		}
		
		for(String curNo : setList) {
			//新增点赞记录
			VoteWorksLike like = new VoteWorksLike();
			like.setNo(curNo);
			like.setUserId(userId);
			like.setOpenId(openId);
			like.setTag(tag);
			Date now = new Date();
			like.setLikeDate(DateUtils.getDayStart(now));
			like.setCreateTime(now);
			likeDao.insert(like);
			
			//作品点赞数加1
			VoteWorks works = new VoteWorks();
			works.setNo(curNo);
			works.setTag(tag);
			works.setUpdateTime(new Date());
			worksDao.likeAdd(works);
		}
		
		//缓存点赞数加1
//		cacheLikeAdd(userId, tag);
		
		//是否获奖
//		like.setCreateTimeFrom(DateUtils.getDayStart(now));
//		like.setCreateTimeTo(DateUtils.getDayEnd(now));
//		Integer likeCount = likeDao.likeCount(like, ACTIVITY_CODE);
		
//		Integer canLotteryStatus = likeCount.equals(LOTTER_LIKE_COUNT)?CodeConstant.CODE_YES:CodeConstant.CODE_NO;
		LikeResultDto resultDto = new LikeResultDto();
//		resultDto.setLikeCount(likeCount);
//		resultDto.setStatus(canLotteryStatus);
//		if(canLotteryStatus.equals(CodeConstant.CODE_YES)) {//保存抽奖码信息
//			String lotteryCode = UUID.randomUUID().toString().replaceAll("-", "");
//			resultDto.setLotteryCode(lotteryCode);
//			saveLotteryRecord(userId,openId,lotteryCode);
//		}
	
		return new StatusObjDto<LikeResultDto>(true,resultDto,StatusDto.SUCCESS,"");
	}
	
	@Transactional
	@Override
	public StatusObjDto<VoteLotteryDto> lottery(String userId, String openId,String lotteryCode) {
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			return new StatusObjDto<VoteLotteryDto>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		if(ObjectUtils.isEmptyOrNull(openId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
		}	
		if(ObjectUtils.isEmptyOrNull(lotteryCode)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("lotteryCode");
		}
		
		//点赞次数验证
		Date now = new Date();
		VoteWorksLike like = new VoteWorksLike();
		like.setOpenId(openId);
		like.setCreateTimeFrom(DateUtils.getDayStart(now));
		like.setCreateTimeTo(DateUtils.getDayEnd(now));
		Integer likeCount = likeDao.likeCount(like, ACTIVITY_CODE);
		if(!likeCount.equals(LOTTER_LIKE_COUNT)) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("未到抽奖点赞次数");
		}
		
		VoteWorksLottery param = new VoteWorksLottery();
		param.setOpenId(openId);
		param.setLotteryCode(lotteryCode);
		VoteWorksLottery lottery = lotteryDao.findByOpenIdAndCode(param);
		if(lottery == null || lottery.getStatus().equals(CodeConstant.CODE_YES)) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("无效抽奖");
		}
		
		//2019-06-28 zhaozx 若已有奖品，则不能再中奖
		VoteLotteryDto lotteryDto = new VoteLotteryDto();
		List<ActPrize> myPrizes = actPrizeBiz.findPrize(ACTIVITY_CODE, null, userId, openId, new String(), null, null);
		if (null != myPrizes && myPrizes.size() > 0) {
			lotteryDto.setStatus(CodeConstant.CODE_NO);
			lotteryDto.setType(VoteLotteryDto.TYPE_THANKS);
			updateLotteryRecord(lottery,CodeConstant.CODE_NO);
			return new StatusObjDto<VoteLotteryDto>(true,lotteryDto,StatusDto.SUCCESS,"");
		}
		
		//初始化奖品
		initPrizes(false);
		//抽奖
		String prizeStr = (String) ActivityRedis.ACT_WORKS_VOTE_PRIZES.sRandomMember(LOTTERY_PRIZES_KEY);
		String[] prizes = prizeStr.split("-");
		Integer status = Integer.valueOf(prizes[0]);
		if(status.equals(CodeConstant.CODE_NO)) {
			lotteryDto.setStatus(CodeConstant.CODE_NO);
			lotteryDto.setType(VoteLotteryDto.TYPE_THANKS);
			updateLotteryRecord(lottery,CodeConstant.CODE_NO);
			return new StatusObjDto<VoteLotteryDto>(true,lotteryDto,StatusDto.SUCCESS,"");
		}
		
		Long prizeId = Long.valueOf(prizes[1]);
		ActPrize prize = actPrizeBiz.findPrize(prizeId);
		if(prize == null || !prize.getStatus().equals(ActPrize.STATUS_NOT_SEND)) {
			lotteryDto.setStatus(CodeConstant.CODE_NO);
			lotteryDto.setType(VoteLotteryDto.TYPE_THANKS);
			updateLotteryRecord(lottery,CodeConstant.CODE_NO);
			return new StatusObjDto<VoteLotteryDto>(true,lotteryDto,StatusDto.SUCCESS,"");
		}
		
		//更新抽奖记录
		lottery.setPrizeId(prizeId);
		updateLotteryRecord(lottery,CodeConstant.CODE_YES);
		//删除该奖品缓存
		ActivityRedis.ACT_WORKS_VOTE_PRIZES.sremove(LOTTERY_PRIZES_KEY,prizeStr);
		//更新奖品状态
		actPrizeBiz.updatePrize(userId, openId, prizeId, ActPrize.STATUS_SEND);
		
		Integer type = getPrizeType(prize.getCode());
		lotteryDto.setStatus(CodeConstant.CODE_YES);
		lotteryDto.setType(type);
		lotteryDto.setPrizeName(prize.getName());
		lotteryDto.setWorth(prize.getWorth());
		lotteryDto.setLotteryCode(lottery.getLotteryCode());
		lotteryDto.setRedeemCode(prize.getRedeemCode());
		
		return  new StatusObjDto<VoteLotteryDto>(true,lotteryDto,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusDto leaveMobile(String userId, String openId, String lotteryCode, String mobile) {
		if(ObjectUtils.isEmptyOrNull(openId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
		}
		if(ObjectUtils.isEmptyOrNull(lotteryCode)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("lotteryCode");
		}
		if(ObjectUtils.isEmptyOrNull(mobile)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
		}
		if(!RegxUtils.isMobile(mobile)){
			throw BizException.COMMON_PARAMS_IS_ILLICIT.format("mobile");
		}
		
		VoteWorksLottery param = new VoteWorksLottery();
		param.setOpenId(openId);
		param.setLotteryCode(lotteryCode);
		param.setActivitycode(ACTIVITY_CODE);
		VoteWorksLottery lottery = lotteryDao.findByOpenIdAndCode(param);
		if(lottery == null || lottery.getStatus().equals(CodeConstant.CODE_NO) ||
				lottery.getIsHit().equals(CodeConstant.CODE_NO) || ObjectUtils.isEmptyOrNull(lottery.getPrizeId())||
				ObjectUtils.isNotEmptyOrNull(lottery.getMobile())) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("预留无效");
		}
		
		lottery.setMobile(mobile);
		lottery.setUpdateTime(new Date());
		int count = lotteryDao.update(lottery);
		if(count > 0) {
			return new StatusDto(true,StatusDto.SUCCESS,"");
		}else {
			return new StatusDto(true,StatusDto.FAIL_COMMON,"预留手机号失败");
		}
	}

	@Override
	public StatusObjDto<List<VoteWorks>> ranking(Integer tag, Integer start, Integer end) {
		List<VoteWorks> worksRanking = worksDao.ranking(ACTIVITY_CODE, tag, start, end);
		for(VoteWorks w:worksRanking) {
			//if(ObjectUtils.isNotEmptyOrNull(w.getCover())) {
				w.setCover(null);
			//}
			if(ObjectUtils.isNotEmptyOrNull(w.getThumbnail())) {
				w.setThumbnail(ImageUtils.getImageUrl(w.getThumbnail()));
			}
		}
		return new StatusObjDto<List<VoteWorks>>(true,worksRanking,StatusDto.SUCCESS,"");
	}

	@Override
	public StatusObjDto<LikeInfoDto> likeRecord(String no, Integer tag, Integer start, Integer end) {
		List<LikeDto> likes = likeDao.likes(no, tag, ACTIVITY_CODE, start, end);
		//点赞总人数设置
		Long likeTotalPeople = likeDao.likeTotalPeople(no, ACTIVITY_CODE, tag);
		LikeInfoDto likeInfo = new LikeInfoDto();
		likeInfo.setAll(likeTotalPeople);
		likeInfo.setTotal(likes.size());
		likeInfo.setInfo(likes);
		
		return new  StatusObjDto<LikeInfoDto>(true,likeInfo,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<List<WinDto>> prizes(Integer start,Integer end){
		List<WinDto> prizes = lotteryDao.findPrizes(ACTIVITY_CODE, start, end);
		return new  StatusObjDto<List<WinDto>>(true,prizes,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusObjDto<List<MyPrizeDto>> myPrizes(String userId,String openId){
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		//不可用活动，除了时间外
		if(!isValidAct.isOk() && (isValidAct.getCode() != 219 && isValidAct.getCode() != 210)) {
			return new StatusObjDto<List<MyPrizeDto>>(false,isValidAct.getCode(),isValidAct.getMsg());
		}
		if(ObjectUtils.isEmptyOrNull(openId)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
		}	
		List<ActPrize> myPrizes = actPrizeBiz.findPrize(ACTIVITY_CODE, null, userId, openId, new String(), null, null);
		List<MyPrizeDto> prizes = myPrizes.stream().map(prize -> setMyPrizeDtoByPrize(prize)).collect(Collectors.toList());
		return new StatusObjDto<List<MyPrizeDto>>(true,prizes,StatusDto.SUCCESS,"");
	}
	
	@Override
	public StatusDto sendRecieveMessage() {
		Thread sendMessageT = new Thread(new Runnable() {
			@Override
			public void run() {
				sendPrizeMessage();
			}
		});
		sendMessageT.start();
		return new StatusDto(true,StatusDto.SUCCESS,"调用成功");
	}
	
	private void hotAdd(String no, Integer tag, String uniqueCode, Long timestamp) {
		if(ObjectUtils.isEmptyOrNull(uniqueCode)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("uniqueCode");
		}
		if(ObjectUtils.isEmptyOrNull(timestamp)){
			throw BizException.COMMON_PARAMS_NOT_NULL.format("timestamp");
		}
		if(timestamp.toString().length() != 13) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("时间戳精确到毫秒");
		}
		StatusDto isValidAct = isValid(ACTIVITY_CODE);
		if(!isValidAct.isOk()) {
			return ;
		}
		//检查时间间隔是否有效
		if(!checkHotTime(uniqueCode,timestamp)) {
			return;
		}
		
		no = ObjectUtils.isEmptyOrNull(no)?"0":no;
		VoteWorksHot hot = hotDao.findByNo(no, tag, ACTIVITY_CODE);
		if(hot == null) {
			hot = new VoteWorksHot();
			hot.setNo(no);
			hot.setHot(1l);
			hot.setTag(tag);
			hot.setCreateTime(new Date());
			hotDao.insert(hot);
		}else {
			hot.setUpdateTime(new Date());
			hotDao.hotAdd(hot);
		}
	}
	
	/**
	 * 检查时间间隔，间隔1秒之内非法
	 * @param openid
	 * @param timestamp
	 */
	private boolean checkHotTime(String uniqueCode,Long timestamp) {
		Long lastTime = (Long) ActivityRedis.ACT_WORKS_VOTE_HOT_TIME.get(uniqueCode);
		if(lastTime != null) {
			Long timespan = timestamp - lastTime;
			if(timespan <= 0) {
				return false;
			}
			if(timespan < 1000) {
				return false;
			}
		}
		ActivityRedis.ACT_WORKS_VOTE_HOT_TIME.set(uniqueCode, timestamp);
		return true;
	}

	/**
	 * 检查时间间隔，间隔0.5秒之内非法
	 * @param openid
	 * @param timestamp
	 */
	private boolean checkLikeTime(String uniqueCode,Long timestamp) {
		Long lastTime = (Long) ActivityRedis.ACT_WORKS_VOTE_LIKE_TIME.get(uniqueCode);
		if(lastTime != null) {
			Long timespan = timestamp - lastTime;
			if(timespan <= 0) {
				return false;
			}
			if(timespan < 500) {
				return false;
			}
		}
		ActivityRedis.ACT_WORKS_VOTE_LIKE_TIME.set(uniqueCode, timestamp);
		return true;
	}
	/**
	 * 获取用户当天点赞数
	 * @param uniqueCode
	 * @param tag TODO
	 * @return
	 */
	private boolean canDoLike(String uniqueCode, Integer tag) {
		/*String day = DateUtils.formate(new Date(),"yyyyMMdd");
		Integer count = (Integer) ActivityRedis.ACT_WORKS_VOTE_LIKE_COUNT.get(uniqueCode+day+tag);
		return count == null || count < LOTTER_LIKE_COUNT;*/
		VoteWorksLike like = new VoteWorksLike();
		like.setUserId(uniqueCode);
		like.setTag(tag);
		Integer likeCount = likeDao.likeCount(like, ACTIVITY_CODE);
		if(likeCount > 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 检查是否可点赞
	 * @param openId
	 * @param tag TODO
	 * @return
	 */
	private void checkCanDoLike(String uniqueCode, Integer tag) {
		String day = DateUtils.formate(new Date(),"yyyyMMdd");
		Integer count = (Integer) ActivityRedis.ACT_WORKS_VOTE_LIKE_COUNT.get(uniqueCode+day+tag);
		Integer limit = AppConfigUtils.getInt(VOTEWORK_LIKE_LIMIT);
		if(count != null && count >= limit) {
			throw ActivityBizException.VOTE_WORKS_LIKE_OVER_LIMIT;
		}
		//2019-06-14 改造为调用account服务判断是否已关注
		// 2021-07-01 去掉关注公众号要求
//		Map<String, Object> busparams = Maps.newHashMap();
//		busparams.put("openId", openId);
//		ResultDto result = RemoteUtils.call("account.wechatcooperation.subscribestatus",ApiServiceTypeEnum.COOPERATION,busparams,true);
//		//错误code!=0
//		if(!result.getCode().equals(ResultDto.SUCCESS)) {
//			throw new RuntimeException(result.getMsg());
//		}
//		Integer status = (Integer)result.getData().get("status");
//		if(count != null && count > 0 && (!status.equals(CodeConstant.CODE_YES))) {
//			throw ActivityBizException.VOTE_WORKS_LIKE_NOT_SUB;
//		}
		
	}
	
	/**
	 * 获取用户当天点赞数
	 * @param uniqueCode
	 * @param tag TODO
	 * @return
	 */
	private void cacheLikeAdd(String uniqueCode, Integer tag) {
		String day = DateUtils.formate(new Date(),"yyyyMMdd");
		Integer count = (Integer) ActivityRedis.ACT_WORKS_VOTE_LIKE_COUNT.get(uniqueCode+day+tag);
		count = count == null?1:count + 1;
		ActivityRedis.ACT_WORKS_VOTE_LIKE_COUNT.set(uniqueCode+day+tag,count);
	}
	
	/**
	 * 检查敏感词
	 * @param content
	 * @return
	 */
	private boolean isSensitive(String content) {
		boolean isHave = SensitiveWordFilterUtil.isContainSensitiveWords(content);
		if (isHave) {
			logger.info("活动业务<|>活动留言<|>判断敏感词<|>原内容:" + content + "<|>屏蔽后内容:" + SensitiveWordFilterUtil.doFilter(content));
		}
		return isHave;
	}
	
	/**
	 * 检查Xss
	 * @param content
	 * @return
	 */
	private boolean isXss(String content) {
		return SensitiveWordFilterUtil.isContainXssWords(content);
	}

	private void initPrizes(boolean ignoreCache) {
		if(!ignoreCache) {
			boolean hasInit = ActivityRedis.ACT_WORKS_VOTE_PRIZES.exists(LOTTERY_PRIZES_KEY);
			if(hasInit) return;
		}
		ActivityRedis.ACT_WORKS_VOTE_PRIZES.del(LOTTERY_PRIZES_KEY);
		logger.info("初始化大转盘奖品.............................");
		
		//中奖奖品初始化
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(ACTIVITY_CODE);
		
		if(prizes.size() == 0) {
			logger.info("无可用的大转盘奖品.............................");
			//throw BizException.COMMON_CUSTOMIZE_ERROR.format("无可用奖品");
		}
		
		//奖品，第一位表示是否中奖（0：否，1：是）,第二表示奖品ID
		//第三位表示奖品ID
		String statusPrefix = "1-";
		//添加Level2兑换码奖品
		for(int i = 0;i < prizes.size();i++) {
			String prize = statusPrefix + prizes.get(i);
			ActivityRedis.ACT_WORKS_VOTE_PRIZES.sadd(LOTTERY_PRIZES_KEY, prize);
		}
		//添加空奖品
	    statusPrefix = "0-";
		for(int i = 0;i < emptyPrizeCount;i++) {
			String emptyPrize = statusPrefix + i;
			ActivityRedis.ACT_WORKS_VOTE_PRIZES.sadd(LOTTERY_PRIZES_KEY, emptyPrize);
		}
		Integer total = prizes.size() + emptyPrizeCount;
		logger.info("初始化投票大转盘奖品完毕.............................,total"+total+",prizes:"+prizes.size()+",empty:"+emptyPrizeCount);
	}
	
	/**
	 * 保存抽奖记录
	 * @param userId
	 * @param openId
	 * @param lotteryCode
	 * @return
	 */
	private VoteWorksLottery saveLotteryRecord(String userId,String openId,String lotteryCode) {
		VoteWorksLottery record = new VoteWorksLottery();
		record.setUserId(userId);
		record.setOpenId(openId);
		record.setLotteryCode(lotteryCode);
		record.setCreateTime(new Date());
		lotteryDao.insert(record);
		
		return record;
	}
	
	private VoteWorksLottery updateLotteryRecord(VoteWorksLottery record,Integer isHit) {
		record.setIsHit(isHit);
		record.setStatus(CodeConstant.CODE_YES);
		record.setUpdateTime(new Date());
		lotteryDao.update(record);
		
		return record;
	}
	
	/**
	 * 获取奖品类型
	 * @param prizeCode
	 * @return
	 */
	private Integer  getPrizeType(String prizeCode) {
		if(ObjectUtils.isEmptyOrNull(prizeCode)) return VoteLotteryDto.TYPE_THANKS;
		Integer type = prizeCode.equals("PRIZE.LEVEL2-1")?VoteLotteryDto.TYPE_LEVEL2_1:
			prizeCode.equals("PRIZE.XMAS.REDEEM.LEVEL2-1")?VoteLotteryDto.TYPE_LEVEL2_1:
			prizeCode.equals("PRIZE.JDCARD8")?VoteLotteryDto.TYPE_JDC_8:
			prizeCode.equals("PRIZE.JDCARD20")?VoteLotteryDto.TYPE_JDC_20:
			prizeCode.equals("PRIZE.JDCARD50")?VoteLotteryDto.TYPE_JDC_50:
			prizeCode.equals("PRIZE.JDCARD88")?VoteLotteryDto.TYPE_JDC_88:
			prizeCode.equals("PRIZE.AQYMCARD")?VoteLotteryDto.TYPE_AQYM:VoteLotteryDto.TYPE_THANKS;
		
		return type;
	}

	private void sendPrizeMessage() {
		logger.info("作品投票抽奖中奖消息发送开始...");
		
		int pageSize = 20,pageIndex=1;
		int count = 0;
		while(true) {
			int[] page = buildPage(pageIndex,pageSize);
			List<VoteWorksLottery> lotteries = lotteryDao.findNotSend(ACTIVITY_CODE, page[0],page[1]); 
			for(VoteWorksLottery lottery:lotteries) {
				Integer type = lottery.getPrizeType();
				if(type.equals(ActPrize.TYPE_REDEEM)) { //发送兑换码短信
				}else if(type.equals(ActPrize.TYPE_CARD_PASSWORD)){//发送卡密码短信
				}
				lottery.setUpdateTime(new Date());
				lotteryDao.updateToSend(lottery);
			}
			count += lotteries.size();
			if(lotteries.size() < pageSize) {
				break;
			}
			pageIndex++;
		}
		logger.info("作品投票抽奖中奖消息发送完毕，共发送：" + count);
	}

	private Integer getPopStatus(String uniqueCode) {
		String date = DateUtils.formate(new Date(),"yyyyMMdd");
		String key = uniqueCode + date;
		try {
			Integer popStatus = (Integer) ActivityRedis.ACT_WORKS_VOTE_POP.get(key);
			popStatus = popStatus == null?CodeConstant.CODE_YES:popStatus;
			return popStatus;
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return CodeConstant.CODE_YES;
	}
	
	private  int[] buildPage(Integer pageIndex, Integer pageSize) {
		pageIndex = pageIndex == null || pageIndex <= 0 ? 1 : pageIndex;
		pageSize = pageSize == null || pageSize <= 0 ? 5 : pageSize;

		int rowStart = (pageIndex - 1) * pageSize;

		int rowEnd = 1;
		if (rowStart == 0) {
			if (pageSize - Integer.MAX_VALUE == 0) {
				rowEnd = pageSize;
			} else {
				rowEnd = pageSize;
			}

		} else {
			rowEnd = rowStart + pageSize;
		}

		return new int[] { rowStart, rowEnd };
	}
	
	private MyPrizeDto setMyPrizeDtoByPrize(ActPrize prize) {
		MyPrizeDto myPrizeDto = new MyPrizeDto();
		myPrizeDto.setSourceText(ActivityConstant.VOTEWORKS_SOURCETEXT);
		
		myPrizeDto.setType(prize.getType());
		myPrizeDto.setName(prize.getName());
		myPrizeDto.setRedeemCode(prize.getRedeemCode());
		myPrizeDto.setCardNo(prize.getCardNo());
		myPrizeDto.setCardPassword(prize.getCardPassword());
		
		
		Redeem redeem = redeemDao.findByCode(prize.getRedeemCode());
		//临时处理，设置level2 1个月
		myPrizeDto.setTime(1);
		myPrizeDto.setValidityDateFrom(redeem.getValidityDateFrom());
		myPrizeDto.setValidityDateTo(redeem.getValidityDateTo());
		myPrizeDto.setCreateTime(redeem.getCreateTime());
		myPrizeDto.setRecieveTime(redeem.getTakeTime());
		return myPrizeDto;
	}
	
}
