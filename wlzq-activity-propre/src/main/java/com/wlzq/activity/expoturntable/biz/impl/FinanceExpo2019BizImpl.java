package com.wlzq.activity.expoturntable.biz.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.model.ActSignin;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.base.redis.BaseRedis;
import com.wlzq.activity.expoturntable.biz.FinanceExpo2019Biz;
import com.wlzq.activity.expoturntable.dao.ActFinexpo19ShakeDao;
import com.wlzq.activity.expoturntable.model.ActFinexpo19Shake;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

@Service
public class FinanceExpo2019BizImpl extends ActivityBaseBiz implements FinanceExpo2019Biz {
	
	/** 签到活动 */
	private static final String ACT_SIGNIN = "ACTIVITY.FINANCEEXPO2019";
	
	private static final String KEY_SINGINCODE = "signInCode";
	private static final String KEY_NICKNAME = "nickName";
	private static final String KEY_HEADIMAGEURL = "headImageUrl";
	
	@Autowired
	private ActivityDao activityDao;
	@Autowired
	private ActFinexpo19ShakeDao actFinexpo19ShakeDao;

	@Override
	public StatusObjDto<Map<String, Object>> signIn(AccTokenUser user) {
		Activity act = super.findActivity(ACT_SIGNIN);
		StatusDto isValid = super.isValid(act);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		if (user == null) { // 未登录
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.NOT_LOGIN_ERROR.getCode(), ActivityBizException.NOT_LOGIN_ERROR.getMsg());
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String userId = user.getUserId();
		Integer scene = this.getScene(act);
		
		resultMap.put("scene", scene);
		resultMap.put(KEY_NICKNAME, user.getNickName());
		resultMap.put(KEY_HEADIMAGEURL, user.getPortrait());
		
		ActFinexpo19Shake shake = this.getShake(userId, scene);
		if (shake != null) {
			resultMap.put(KEY_SINGINCODE, String.valueOf(shake.getId()));
			return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		
		shake = new ActFinexpo19Shake();
		shake.setScene(scene);
		shake.setUserId(userId);
		shake.setTotalCount(0);
		shake.setStatus(ActFinexpo19Shake.STATUS_VALID);
		shake.setLastCount(0);
		actFinexpo19ShakeDao.insert(shake);
		
		shake.setSigninId(shake.getId());
		actFinexpo19ShakeDao.update(shake);
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	
	@Override
	public StatusObjDto<List<Map<String, Object>>> signinList(Integer maxOrder,	Integer maxLength) {
		Activity act = super.findActivity(ACT_SIGNIN);
		StatusDto isValid = super.isValid(act);
		if (!isValid.isOk()) {
			return new StatusObjDto<List<Map<String, Object>>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		
		Integer scene = this.getScene(act);
		List<Map<String, Object>> list = actFinexpo19ShakeDao.findSigninListCodeNotNull(scene, maxOrder, maxLength);
		if (list != null) {
			return new StatusObjDto<List<Map<String, Object>>>(true, list, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		return new StatusObjDto<List<Map<String, Object>>>(false, null, ActivityBizException.ACTIVITY_NO_STATISTICS.getCode(), ActivityBizException.ACTIVITY_NO_STATISTICS.getMsg());
	}

	@Override
	public StatusObjDto<Map<String, Object>> gameSwitch(Integer switchType, Integer scene) {
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
		String remark = act.getRemark();
		String oldScene = null;
		if (remark != null && remark.trim().length() > 0) {
			String[] arr = remark.split(";");
			oldScene = arr[1];
		} else {
			remark = "暂停;1";
			oldScene = "1";
		}
		
		if (switchType.intValue() == 1) {
			act.setRemark("开始;" + oldScene);
			resultMap.put("resultmsg", "游戏开始");
			resultMap.put("scene", Integer.parseInt(oldScene));
		} else if (switchType.intValue() == 2) {
			act.setRemark("暂停;" + oldScene);
			resultMap.put("resultmsg", "游戏暂停");
			resultMap.put("scene", Integer.parseInt(oldScene));
		} else if (switchType.intValue() == 0) {
			Integer newScene = Integer.parseInt(oldScene) + 1;
			act.setRemark("暂停;" + newScene);
			resultMap.put("resultmsg", "游戏数据重置并暂停,开始新一局游戏");
			resultMap.put("scene", newScene);
			clearDate();
		} else {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ACTIVITY_PARAMS_FORMAT_ERROR.getCode(), "switchType不正确");
		}
		activityDao.update(act);
		BaseRedis.ACT_ACTIVITY_INFO.del(ACT_SIGNIN);
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	
	public void clearDate() {
		ActSignin qry = new ActSignin();
		qry.setActCode(ACT_SIGNIN);
		
		actFinexpo19ShakeDao.updateInvalid();
	}

	@Override
	public StatusObjDto<Map<String, Object>> gameShake(AccTokenUser user, Integer scene, String counts, Integer maxLength) {
		Activity act = super.findActivity(ACT_SIGNIN);
		StatusDto isValid = super.isValid(act);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		if (!(user != null && user.getUserId() != null)) { // 未登录
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.NOT_LOGIN_ERROR.getCode(), ActivityBizException.NOT_LOGIN_ERROR.getMsg());
		}
		if (!(scene != null && scene > 0)) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "scene不能为空");
		}
		if (!(counts != null && counts.length() > 0)) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "counts不能为空");
		}
		if (!(maxLength != null && maxLength > 0)) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "maxLength不能为空");
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int r = this.isSceneMatch(act, scene);
		if (r == 2) {
			resultMap.put("resultCode", 2);
			resultMap.put("resultmsg", "游戏未开始");
			return new StatusObjDto<Map<String, Object>>(false, resultMap, -1, "游戏未开始");
		} else if (r == 3) {
			resultMap.put("resultCode", 3);
			resultMap.put("resultmsg", "场次不匹配");
			return new StatusObjDto<Map<String, Object>>(false, resultMap, -1, "场次不匹配");
		} else if (r == -1) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ILLEGAL_ACCESS.getCode(), ActivityBizException.ILLEGAL_ACCESS.getMsg());
		}
		
		String userId = user.getUserId();
		ActFinexpo19Shake shake = this.getShake(userId, scene);
		
		int totalCount = 0;
		int lastCount = 0;
		String[] countArr = counts.split(",");
		if (countArr.length > maxLength.intValue()) {
			String t = "";
			for (int i = 0; i < countArr.length; i++) {
				if (i == maxLength.intValue()) {
					break;
				}
				if (i == 0) {
					t = countArr[i];
				} else {
					t = t + "," + countArr[i];
				}
			}
			counts = t;
			countArr = counts.split(",");
		}
		for (int i = 0; i < countArr.length; i++) {
			totalCount = totalCount + Integer.parseInt(countArr[i]);
			if (i == countArr.length - 1) {
				lastCount = Integer.parseInt(countArr[i]);
			}
		}
		if (shake == null) {
			shake = new ActFinexpo19Shake();
			shake.setUserId(userId);
			shake.setScene(scene);
			shake.setStatus(ActFinexpo19Shake.STATUS_VALID);
			shake.setCounts(counts);
			shake.setTotalCount(totalCount);
			shake.setLastCount(lastCount);
			actFinexpo19ShakeDao.insert(shake);
		} else {
			shake.setCounts(counts);
			shake.setTotalCount(totalCount);
			shake.setLastCount(lastCount);
			actFinexpo19ShakeDao.update(shake);
		}
		
		resultMap.put("resultCode", 1);
		resultMap.put("resultmsg", "记录成功");
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	
	private ActFinexpo19Shake getShake(String userId, Integer scene) {
		ActFinexpo19Shake qry = new ActFinexpo19Shake();
		qry.setUserId(userId);
		qry.setScene(scene);
		qry.setStatus(ActFinexpo19Shake.STATUS_VALID);
		List<ActFinexpo19Shake> list = actFinexpo19ShakeDao.findList(qry);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public StatusObjDto<Map<String, Object>> shakeTrend(Integer scene, Integer length) {
		Activity act = super.findActivity(ACT_SIGNIN);
		StatusDto isValid = super.isValid(act);
		if (!isValid.isOk()) {
			return new StatusObjDto<Map<String, Object>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		if (!(scene != null && scene > 0)) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "scene不能为空");
		}
		if (!(length != null && length > 0)) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.COMMON_PARAMS_NOT_NULL.getCode(), "length不能为空");
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int r = this.isSceneMatch(act, scene);
		if (r == 2) {
			resultMap.put("resultCode", 2);
			resultMap.put("resultmsg", "游戏未开始");
			return new StatusObjDto<Map<String, Object>>(false, resultMap, -1, "游戏未开始");
		} else if (r == 3) {
			resultMap.put("resultCode", 3);
			resultMap.put("resultmsg", "场次不匹配");
			return new StatusObjDto<Map<String, Object>>(false, resultMap, -1, "场次不匹配");
		} else if (r == -1) {
			return new StatusObjDto<Map<String, Object>>(false, null, ActivityBizException.ILLEGAL_ACCESS.getCode(), ActivityBizException.ILLEGAL_ACCESS.getMsg());
		}
		resultMap.put("resultCode", 1);
		resultMap.put("resultmsg", "");
		
		
		List<ActFinexpo19Shake> list = this.getRank(scene, length);
		Integer index = this.getSum(scene);
		resultMap.put("index", index);
		resultMap.put("total", list==null?0:list.size());
		resultMap.put("info", list);
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	
	private List<ActFinexpo19Shake> getRank(Integer scene, Integer length) {
		ActFinexpo19Shake qry = new ActFinexpo19Shake();
		qry.setStatus(ActFinexpo19Shake.STATUS_VALID);
		qry.setScene(scene);
		qry.setSizeCount(length);
		return actFinexpo19ShakeDao.findList(qry);
	}
	
	private Integer getSum(Integer scene) {
		ActFinexpo19Shake qry = new ActFinexpo19Shake();
		qry.setStatus(ActFinexpo19Shake.STATUS_VALID);
		qry.setScene(scene);
		return actFinexpo19ShakeDao.getSum(qry);
	}
	
	/**
	 * 判断传入的场次是否匹配
	 * @param act
	 * @param scene
	 * @return
	 */
	private int isSceneMatch(Activity act, Integer scene) {
		String remark = act.getRemark();
		String gameStatus = null;
		String nowScene = null;
		if (remark != null && remark.trim().length() > 0) {
			String[] arr = remark.split(";");
			gameStatus = arr[0];
			nowScene = arr[1];
			if ("暂停".equals(gameStatus)) {
				return 2;
			} else {
				if (!nowScene.equals(String.valueOf(scene))) {
					return 3;
				}
			}
		} else {
			return -1;
		}
		return 1;
	}
	
	private Integer getScene(Activity act) {
		String remark = act.getRemark();
		String nowScene = null;
		if (remark != null && remark.trim().length() > 0) {
			String[] arr = remark.split(";");
			nowScene = arr[1];
			return Integer.parseInt(nowScene);
		}
		return 1;
	}

	@Override
	public StatusObjDto<Map<String, Object>> shakeRank(Integer scene) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("resultCode", 1);
		resultMap.put("resultmsg", "");
		
		List<ActFinexpo19Shake> list = this.getRank(scene, 5);
		if (list != null && list.size() > 0) {
			Map<String, Object> lMap = new HashMap<String, Object>();
			for (int i = 0; i < list.size(); i++) {
				ActFinexpo19Shake shake = list.get(i);
				if (i == 0) {
					Map<String, Object> tMap = new HashMap<String, Object>();
					tMap.put("toalCount", shake.getTotalCount());
					tMap.put("lastCount", shake.getLastCount());
					tMap.put("nickName", shake.getNickName());
					tMap.put("headImageUrl", shake.getPortrait());
					resultMap.put("totalCountWinner", tMap);
					
					
					lMap.put("toalCount", shake.getTotalCount());
					lMap.put("lastCount", shake.getLastCount());
					lMap.put("nickName", shake.getNickName());
					lMap.put("headImageUrl", shake.getPortrait());
					resultMap.put("lastCountWinner", lMap);
				} else {
					Integer shakeLastCount = shake.getLastCount();
					Integer oldLastCount = (Integer) lMap.get("lastCount");
					if (shakeLastCount.intValue() > oldLastCount.intValue()) {
						lMap.put("toalCount", shake.getTotalCount());
						lMap.put("lastCount", shake.getLastCount());
						lMap.put("nickName", shake.getNickName());
						lMap.put("headImageUrl", shake.getPortrait());
						resultMap.put("lastCountWinner", lMap);
					}
				}
				
			}
		}
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}

	@Override
	public StatusObjDto<Map<String, Object>> personelRecord(Integer scene, AccTokenUser user) {
		ActFinexpo19Shake me = this.getShake(user.getUserId(), scene);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("isWin", 0);
		resultMap.put("totalCount", me.getTotalCount());
		resultMap.put("lastCount", me.getLastCount());
		resultMap.put("headImageUrl", me.getPortrait());
		resultMap.put("nickName", me.getNickName());
		
		int r = 0;
		List<ActFinexpo19Shake> list = this.getRank(scene, 5);
		if (list != null && list.size() > 0) {
			Map<String, Integer> lMap = new HashMap<String, Integer>();
			for (int i = 0; i < list.size(); i++) {
				ActFinexpo19Shake s = list.get(i);
				if (i == 0) {
					if (s.getId().intValue() == me.getId().intValue()) {
						r = r + 1;
					}
					
					lMap.put("id", s.getId());
					lMap.put("lastCount", s.getLastCount());
				} else {
					int maxLastCount = lMap.get("lastCount").intValue();
					int thisLastCount = s.getLastCount().intValue();
					if (thisLastCount > maxLastCount) {
						lMap.put("id", s.getId());
						lMap.put("lastCount", s.getLastCount());
					}
				}
			}
			
			if (lMap.get("id").intValue() == me.getId().intValue()) {
				r = r + 2;
			}
		}
		resultMap.put("isWin", r);
		
		
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}

	@Override
	public StatusObjDto<Map<String, Object>> playerInfo(String playerIds) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String[] ids = playerIds.split(",");
		List<Integer> l = new ArrayList<Integer>();
		for (int i = 0; i < ids.length; i++) {
			l.add(Integer.parseInt(ids[i]));
		}
		ActFinexpo19Shake shake = new ActFinexpo19Shake();
		shake.setPlayerIds(l);
		List<Map<String, Object>> list = actFinexpo19ShakeDao.findListByPlayerIds(shake);
		resultMap.put("total", list == null? 0 : list.size());
		resultMap.put("info", list);
		return new StatusObjDto<Map<String, Object>>(true, resultMap, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
	}
	

}
