package com.wlzq.activity.brokeragemeeting.biz.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActSigninDao;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.model.ActSignin;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.brokeragemeeting.biz.BrokerageMeetingBiz;
import com.wlzq.activity.brokeragemeeting.dao.ActBrokeragemeetingPersonnelDao;
import com.wlzq.activity.brokeragemeeting.dao.ActBrokeragemeetingVoteDao;
import com.wlzq.activity.brokeragemeeting.dao.ActBrokeragemeetingWordDao;
import com.wlzq.activity.brokeragemeeting.dao.WechatUserDao;
import com.wlzq.activity.brokeragemeeting.dto.BrokerageMeetingVoteResultDto;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingPersonnel;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingVote;
import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingWord;
import com.wlzq.activity.brokeragemeeting.model.WechatUser;
import com.wlzq.activity.brokeragemeeting.utils.BrokeargemeetingRedis;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

@Service
public class BrokerageMeetingBizImpl extends ActivityBaseBiz implements BrokerageMeetingBiz {

	/** 签到活动 */
	private static final String ACT_SIGNIN = "ACTIVITY.BROKERAGECONFERENCE.SIGNIN";
	
	@Autowired
	private ActBrokeragemeetingPersonnelDao actBrokeragemeetingPersonnelDao;
	@Autowired
	private ActBrokeragemeetingWordDao actBrokeragemeetingWordDao;
	@Autowired
	private ActBrokeragemeetingVoteDao actBrokeragemeetingVoteDao;
	@Autowired
	private ActSigninDao actSigninDao;
	@Autowired
	private WechatUserDao wechatUserDao;
	@Autowired
	private ActivityDao activityDao;
	
	@Override
	public StatusObjDto<Map<String, Object> > signIn(AccTokenUser user) {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		if (!(user != null && user.getOpenid() != null)) { // 未登录
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.NOT_LOGIN_ERROR.getCode(), ActivityBizException.NOT_LOGIN_ERROR.getMsg());
		}
		// 检查微信用户是否存在
		WechatUser wechatUser = this.findWechatUserByOpenId(user.getOpenid());
		if (wechatUser == null) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_WECHATUSER_NOTFOUND.getCode(), ActivityBizException.ACTIVITY_WECHATUSER_NOTFOUND.getMsg());
		}

		// 检查是否已经在列表中
		ActBrokeragemeetingPersonnel people = this.isInList(wechatUser.getNickname());
		if (people == null) {
			people = new ActBrokeragemeetingPersonnel();
			people.setUserId(user.getUserId());
			people.setOpenId(user.getOpenid());
			people.setNickName(wechatUser.getNickname());
			people.setHeadImageUrl(wechatUser.getHeadimgurl());
			people.setIsDeleted(ActBrokeragemeetingPersonnel.ISDELETE_NO);
			people.setIsLigtedUp(ActBrokeragemeetingPersonnel.ISLIGHTEDUP_NO);
			people.setIsNeedUnveiling(ActBrokeragemeetingPersonnel.ISNEEDUNVEILING_NO);
			people.setIsUnveiled(ActBrokeragemeetingPersonnel.ISUNVEILED_NO);
			actBrokeragemeetingPersonnelDao.insert(people);
		} else {
			people.setUserId(user.getUserId());
			people.setOpenId(user.getOpenid());
			people.setNickName(wechatUser.getNickname());
			people.setHeadImageUrl(wechatUser.getHeadimgurl());
			if (people.getIsDeleted() == null) {
				people.setIsDeleted(ActBrokeragemeetingPersonnel.ISDELETE_NO);
			}
			if (people.getIsLigtedUp() == null) {
				people.setIsLigtedUp(ActBrokeragemeetingPersonnel.ISLIGHTEDUP_NO);
			}
			if (people.getIsNeedUnveiling() == null) {
				people.setIsNeedUnveiling(ActBrokeragemeetingPersonnel.ISNEEDUNVEILING_NO);
			}
			if (people.getIsUnveiled() == null) {
				people.setIsUnveiled(ActBrokeragemeetingPersonnel.ISUNVEILED_NO);
			}
			
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("name", people.getName());
		resultMap.put("nickName", people.getNickName());
		resultMap.put("headImageUrl", people.getHeadImageUrl());

		// 检查是否已签到
		ActSignin signin = this.getSignin(user.getUserId());
		if (signin != null && signin.getStatus() != null && signin.getStatus().intValue() == ActSignin.STATUS_VALID.intValue()) {
			resultMap.put("signinCode", people.getSignOrder());
			return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}

		signin = new ActSignin();
		signin.setActCode(ACT_SIGNIN);
		signin.setUserId(user.getUserId());
		signin.setSigninTime(new Date());
		signin.setStatus(ActSignin.STATUS_VALID);
		signin.setSignInCode(String.valueOf(actBrokeragemeetingPersonnelDao.findMaxId() - 1));
		int r = actSigninDao.insert(signin);
		if (r > 0) {
//			actSigninDao.update(signin);
			people.setSigninTime(signin.getSigninTime());
			people.setSignOrder(signin.getSignInCode());
			actBrokeragemeetingPersonnelDao.update(people);
			
			resultMap.put("signinCode", people.getSignOrder());
			return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		} else {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_DATABASE_ERROR.getCode(), ActivityBizException.ACTIVITY_DATABASE_ERROR.getMsg());
		}

	}
	
	private WechatUser findWechatUserByOpenId(String openId) {
		WechatUser wechatUser = null;
		wechatUser = (WechatUser) BrokeargemeetingRedis.ACT_WECHATUSER.get(openId);
		if (wechatUser == null) {
			// 检查微信用户是否存在
			wechatUser = wechatUserDao.findByOpenId(openId);
			BrokeargemeetingRedis.ACT_WECHATUSER.set(openId, wechatUser);
		}
		return wechatUser;
	}
	
	/**
	 * 获取签到信息
	 * @param userId
	 * @return
	 */
	private ActSignin getSignin(String userId) {
		ActSignin qry = new ActSignin();
		qry.setActCode(ACT_SIGNIN);
		qry.setUserId(userId);
		qry.setStatus(ActSignin.STATUS_VALID);
		List<ActSignin> list = actSigninDao.findList(qry);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * 是否在列表之中
	 * @param nickName
	 * @return
	 */
	private ActBrokeragemeetingPersonnel isInList(String nickName) {
		if (nickName != null && nickName.length() > 0) {
			ActBrokeragemeetingPersonnel qry = new ActBrokeragemeetingPersonnel();
			qry.setNickName(nickName);
			List<ActBrokeragemeetingPersonnel> list = actBrokeragemeetingPersonnelDao.findList(qry);
			if (list != null && list.size() > 0) {
				if (list.size() == 1) {
					return list.get(0);
				} else {
					
				}
			}
		}
		return null;
	}
	
	private ActBrokeragemeetingPersonnel isInListByuserId(String userId) {
		if (userId != null && userId.length() > 0) {
			ActBrokeragemeetingPersonnel qry = new ActBrokeragemeetingPersonnel();
			qry.setUserId(userId);
			List<ActBrokeragemeetingPersonnel> list = actBrokeragemeetingPersonnelDao.findList(qry);
			if (list != null && list.size() > 0) {
				return list.get(0);
			}
		}
		return null;
	}
	
	/**
	 * 通过userId查找
	 * @param userId
	 * @return
	 */
	private ActBrokeragemeetingPersonnel findByUserId(String userId) {
		if (userId != null && userId.length() > 0) {
			ActBrokeragemeetingPersonnel qry = new ActBrokeragemeetingPersonnel();
			qry.setUserId(userId);
			List<ActBrokeragemeetingPersonnel> list = actBrokeragemeetingPersonnelDao.findList(qry);
			if (list != null && list.size() > 0) {
				return list.get(0);
			}
		}
		return null;
	}
	
	/**
	 * 是否在列表之中
	 * @param name
	 * @return
	 */
	private ActBrokeragemeetingPersonnel isInListByName(String name) {
		if (name != null && name.length() > 0) {
			ActBrokeragemeetingPersonnel qry = new ActBrokeragemeetingPersonnel();
			qry.setName(name);
			List<ActBrokeragemeetingPersonnel> list = actBrokeragemeetingPersonnelDao.findList(qry);
			if (list != null && list.size() > 0) {
				return list.get(0);
			}
		}
		return null;
	}

	@Override
	public StatusObjDto<Map<String, Object>> unveiling(AccTokenUser user, String name) {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		
		ActBrokeragemeetingPersonnel people = null;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (name != null && name.trim().length() > 0) {
			people = this.isInListByName(name);
		} else { // token和name不能同时为空
			if (!(user != null && user.getOpenid() != null)) { // 未登录
				return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.NOT_LOGIN_ERROR.getCode(), ActivityBizException.NOT_LOGIN_ERROR.getMsg());
			}
			people = this.getPersonnel(user);
			if (people == null) {
				return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_WECHATUSER_NOTFOUND.getCode(), ActivityBizException.ACTIVITY_WECHATUSER_NOTFOUND.getMsg());
			}
		}
		
		resultMap.put("name", people.getName());
		resultMap.put("nickName", people.getNickName());
		resultMap.put("isNeedUnveiling", people.getIsNeedUnveiling());
		resultMap.put("isUnveiled", people.getIsUnveiled());
		
		// 需要揭幕
		if (people.getIsNeedUnveiling() != null && people.getIsNeedUnveiling().intValue() == ActBrokeragemeetingPersonnel.ISNEEDUNVEILING_YES.intValue()) {
			// 还没揭幕
			if (people.getIsUnveiled() != null && people.getIsUnveiled().intValue() != ActBrokeragemeetingPersonnel.ISUNVEILED_YES) {
				people.setIsUnveiled(ActBrokeragemeetingPersonnel.ISUNVEILED_YES);
				int r = actBrokeragemeetingPersonnelDao.update(people);
				if (r > 0) {
					resultMap.put("isUnveiled", people.getIsUnveiled());
				}
			}
		}
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}

	@Override
	public StatusObjDto<Map<String, Object>> isUnveilDone() {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		
		List<ActBrokeragemeetingPersonnel> list = actBrokeragemeetingPersonnelDao.findNeedUnveilingSigninList();
		int wholeSize = 0;
		int cnt = 0;
		if (list != null && list.size() > 0) {
			wholeSize = list.size();
			for (ActBrokeragemeetingPersonnel ab : list) {
				if (ACT_SIGNIN.equals(ab.getActCode())) {
					if (ab.getIsUnveiled() != null && ab.getIsUnveiled().intValue() == ActBrokeragemeetingPersonnel.ISUNVEILED_YES.intValue()) {
						cnt++;
					}
				}
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("isUnveilDone", 0);
		if (wholeSize > 0 && wholeSize == cnt) {
			map.put("isUnveilDone", 1);
			return new StatusObjDto<Map<String, Object>>(true, map, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		
		return new StatusObjDto<Map<String, Object>>(true, map, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}

	@Override
	public StatusObjDto<List<ActBrokeragemeetingPersonnel>> signinList(Integer maxOrder, Integer maxLength) {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<List<ActBrokeragemeetingPersonnel>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		
		List<ActBrokeragemeetingPersonnel> list = actBrokeragemeetingPersonnelDao.findListSigninList(maxOrder, maxLength);
		if (list != null) {
			return new StatusObjDto<List<ActBrokeragemeetingPersonnel>>(true, list, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		return new StatusObjDto<List<ActBrokeragemeetingPersonnel>>(false, null, ActivityBizException.ACTIVITY_NO_STATISTICS.getCode(), ActivityBizException.ACTIVITY_NO_STATISTICS.getMsg());
	}

	@Override
	public StatusObjDto<Map<String, Object>> gameSwitch(Integer switchType) {
		Activity act = activityDao.findActivityByCode(ACT_SIGNIN);
		StatusDto isValid = super.isValid(act);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		
		if (switchType == null) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_PARAMS_NOTNULL.getCode(), "switchType不能为空");
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("isDone", 1);
		resultMap.put("resultmsg", "");
		if (switchType.intValue() == 1) {
			act.setRemark("游戏状态:开始");
			resultMap.put("resultmsg", "游戏开始");
		} else if (switchType.intValue() == 2) {
			act.setRemark("游戏状态:暂停");
			resultMap.put("resultmsg", "游戏暂停");
		} else if (switchType.intValue() == 0) {
			act.setRemark("游戏状态:暂停");
			resultMap.put("resultmsg", "游戏数据重置并暂停");
			clearGameData();
		} else if (switchType.intValue() == 23) {
			act.setRemark("游戏状态:暂停");
			Integer max = actBrokeragemeetingPersonnelDao.findMaxId();
			Integer step = (max - 1) * (-1);
//			Integer step = max * (-1);
			actBrokeragemeetingPersonnelDao.alterStep(step);
			max = actBrokeragemeetingPersonnelDao.findMaxId();
			actBrokeragemeetingPersonnelDao.alterStep(1);
			resultMap.put("resultmsg", "重置签到数据并暂停");
			actSigninDao.deleteByActCode(ACT_SIGNIN);
			actBrokeragemeetingPersonnelDao.clearSigninCode();
			actBrokeragemeetingPersonnelDao.clearUnveiled();
			clearGameData();
		} else {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_PARAMS_FORMAT_ERROR.getCode(), "switchType不正确");
		}
		activityDao.update(act);
		
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	
	/**
	 * 清除游戏数据
	 */
	private void clearGameData() {
		actBrokeragemeetingVoteDao.clearGameData();
	}

	@Override
	public StatusObjDto<Map<String, Object>> gameVote(AccTokenUser user, String votewords, String voteCounts) {
		Activity act = activityDao.findActivityByCode(ACT_SIGNIN);
		StatusDto isValid = super.isValid(act);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		if (!(user != null && user.getUserId() != null)) { // 未登录
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.NOT_LOGIN_ERROR.getCode(), ActivityBizException.NOT_LOGIN_ERROR.getMsg());
		}
		
		
		
		if (!(votewords != null && votewords.trim().length() > 0)) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "votewords不能为空");
		}
		if (!(voteCounts != null && voteCounts.trim().length() > 0)) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "voteCounts不能为空");
		}
		String[] voteWords = votewords.split(",");
		String[] counts = voteCounts.split(",");
		if (voteWords != null && counts != null) {
			if (counts.length != voteWords.length) {
				return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_IS_ILLICIT.getCode(), "voteCounts和votewords数量不对等");
			}
		} else {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "voteCounts和votewords不能为空");
		}
		
		String name = null;
		String nickName = null;
		ActBrokeragemeetingPersonnel personnel = this.isInListByuserId(user.getUserId());
		if (personnel == null) {
			WechatUser wechatUser = findWechatUserByOpenId(user.getOpenid());
			if (wechatUser == null) {
				return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_WECHATUSER_NOTFOUND.getCode(), ActivityBizException.ACTIVITY_WECHATUSER_NOTFOUND.getMsg());
			}
			nickName = wechatUser.getNickname();
			name = user.getUserName();
		} else {
			name = personnel.getName();
			nickName = personnel.getNickName();
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("resultCode", 2);
		resultMap.put("resultmsg", "游戏未开始");
		if ("游戏状态:开始".equals(act.getRemark())) {
			for (int i = 0; i < voteWords.length; i++) {
				ActBrokeragemeetingVote vote = null;
				Integer voteCount = Integer.parseInt(counts[i]);
				String voteWord = voteWords[i];
				ActBrokeragemeetingVote qry = new ActBrokeragemeetingVote();
				qry.setUserId(user.getUserId());
				qry.setWord(voteWord);
				qry.setStatus(ActBrokeragemeetingVote.STATUS_VALID);
				List<ActBrokeragemeetingVote> list = actBrokeragemeetingVoteDao.findList(qry);
				if (list != null && list.size() > 0) {
					vote = list.get(0);
					vote.setVoteCount(voteCount);
					actBrokeragemeetingVoteDao.update(vote);
					resultMap.put("resultmsg", "新增投票次数成功");
				} else { 
					vote = new ActBrokeragemeetingVote();
					vote.setVoteCount(voteCount);
					vote.setWord(voteWord);
					vote.setUserId(user.getUserId());
					vote.setName(name);
					vote.setNickName(nickName);
					vote.setStatus(ActBrokeragemeetingVote.STATUS_VALID);
					actBrokeragemeetingVoteDao.insert(vote);
					resultMap.put("resultmsg", "更新投票次数成功");
				}
				resultMap.put("resultCode", 1);
			}
			
		}		
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}

	@Override
	public StatusObjDto<BrokerageMeetingVoteResultDto> voteResult(Integer maxLength) {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<BrokerageMeetingVoteResultDto>(false, null, isValid.getCode(), isValid.getMsg());
		}
		List<ActBrokeragemeetingVote> list = actBrokeragemeetingVoteDao.findWordCountResult(maxLength);
		Integer personNum = actBrokeragemeetingVoteDao.findPersonNum();
		if (personNum == null) {
			personNum = 0;
		}
		BrokerageMeetingVoteResultDto dto = new BrokerageMeetingVoteResultDto();
		if (list != null) {
			dto.setList(list);
			dto.setPersonNum(personNum);
			return new StatusObjDto<BrokerageMeetingVoteResultDto>(true, dto, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		return new StatusObjDto<BrokerageMeetingVoteResultDto>(false, null, ActivityBizException.ACTIVITY_NO_STATISTICS.getCode(), ActivityBizException.ACTIVITY_NO_STATISTICS.getMsg());
	}

	@Override
	public StatusObjDto<List<Map<String, Object>>> voteSpeed(Integer maxLength) {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<List<Map<String, Object>>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		
		List<Map<String, Object>> list = actBrokeragemeetingVoteDao.findFastResult(maxLength);
		if (list != null) {
			return new StatusObjDto<List<Map<String, Object>>>(true, list, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		return new StatusObjDto<List<Map<String, Object>>>(false, null, ActivityBizException.ACTIVITY_NO_STATISTICS.getCode(), ActivityBizException.ACTIVITY_NO_STATISTICS.getMsg());
	}

	@Override
	public StatusObjDto<List<ActBrokeragemeetingWord>> wordList() {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<List<ActBrokeragemeetingWord>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		ActBrokeragemeetingWord qry = new ActBrokeragemeetingWord();
		qry.setStatus(1);
		List<ActBrokeragemeetingWord> list = actBrokeragemeetingWordDao.findList(qry);
		return new StatusObjDto<List<ActBrokeragemeetingWord>>(true, list, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}

	@Override
	public StatusObjDto<Map<String, Object>> personSpeed(AccTokenUser user) {
		StatusDto isValid = super.isValid(ACT_SIGNIN);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		if (!(user != null && user.getUserId() != null)) { // 未登录
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.NOT_LOGIN_ERROR.getCode(), ActivityBizException.NOT_LOGIN_ERROR.getMsg());
		}
		
		List<Map<String, Object>> list = actBrokeragemeetingVoteDao.findFastRanking(user.getUserId());
		if (list != null && list.size() > 0) {
			Map<String, Object> map = list.get(0);
			return new StatusObjDto<Map<String, Object>>(true, map, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_NO_STATISTICS.getCode(), ActivityBizException.ACTIVITY_NO_STATISTICS.getMsg());
	}
	
	/**
	 * 获取与会人员，没有则新增
	 * @param user
	 * @return
	 */
	private ActBrokeragemeetingPersonnel getPersonnel(AccTokenUser user) {
		ActBrokeragemeetingPersonnel people = null;
		people = this.findByUserId(user.getUserId());
		if (people == null) {
			WechatUser wechatUser = null;
			wechatUser = this.findWechatUserByOpenId(user.getOpenid());
			if (wechatUser == null) {
				return null;
			}
			people = this.isInList(wechatUser.getNickname());
			if (people == null) {
				people = new ActBrokeragemeetingPersonnel();
				people.setUserId(user.getUserId());
				people.setOpenId(user.getOpenid());
				people.setNickName(wechatUser.getNickname());
				people.setHeadImageUrl(wechatUser.getHeadimgurl());
				people.setIsDeleted(ActBrokeragemeetingPersonnel.ISDELETE_NO);
				people.setIsLigtedUp(ActBrokeragemeetingPersonnel.ISLIGHTEDUP_NO);
				people.setIsNeedUnveiling(ActBrokeragemeetingPersonnel.ISNEEDUNVEILING_NO);
				people.setIsUnveiled(ActBrokeragemeetingPersonnel.ISUNVEILED_NO);
				actBrokeragemeetingPersonnelDao.insert(people);
			}
		}
		return people;
	}

}
