package com.wlzq.activity.base.biz.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActSigninBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActSigninDao;
import com.wlzq.activity.base.model.ActSignin;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 
 * @author cjz
 *
 */
@Service
public class ActSigninBizImpl extends ActivityBaseBiz implements ActSigninBiz {

	private Logger logger = LoggerFactory.getLogger(ActSigninBizImpl.class);
	
	/** 金交会签到抽奖活动 */
	private static final String ACTIVITY_EXPOTURNTABLE = "ACTIVITY.EXPOTURNTABLE";
	
	@Autowired
	private ActSigninDao actSigninDao;
	//@Autowired
	//private WechatUserBiz wechatUserBiz;
	
	@Override
	public StatusObjDto<ActSignin> signIn(AccTokenUser user, String activityCode) {
		StatusDto isValid = super.isValid(activityCode);
		if (!isValid.isOk()) {
			logger.info("活动业务<|>活动签到<|>结果:失败<|>code:" + isValid.getCode()+"<|>msg:"+isValid.getMsg());
			return new StatusObjDto<ActSignin>(false, null, isValid.getCode(), isValid.getMsg());
		}
		if (!(user != null && user.getOpenid() != null)) { // 未登录
			logger.info("活动业务<|>活动签到<|>结果:失败<|>code:" + ActivityBizException.ACTIVITY_NOT_LOGIN.getCode()+"<|>msg:"+ActivityBizException.INTERNSHIP_NOT_LOGIN.getMsg());
			return new StatusObjDto<ActSignin>(false, null, ActivityBizException.ACTIVITY_NOT_LOGIN.getCode(), ActivityBizException.INTERNSHIP_NOT_LOGIN.getMsg());
		}
		
		StatusObjDto<Integer> result = new StatusObjDto<Integer>(true,0,StatusDto.SUCCESS,"");//wechatUserBiz.subscribestatus(user.getOpenid());
		boolean isFollow = false;
		if (result != null) {
			if (result.isOk() && result.getObj() == 1) { // 已关注
				isFollow = true;
			} else if (!result.isOk() && result.getCode() == 1) { // 接口异常情况，思迪接口异常算成功
				isFollow = true;
			}		
		} else {
			isFollow = true;
		}
		if (!isFollow) {
			logger.info("活动业务<|>活动签到<|>结果:失败<|>code:" + ActivityBizException.ACTIVITY_NOT_FOLLOW.getCode()+"<|>msg:"+ActivityBizException.ACTIVITY_NOT_FOLLOW.getMsg());
			return new StatusObjDto<ActSignin>(false, null, ActivityBizException.ACTIVITY_NOT_FOLLOW.getCode(), ActivityBizException.ACTIVITY_NOT_FOLLOW.getMsg());
		}
		
		// 检查是否已签到
		ActSignin actSignIn = this.isSignIn(user, activityCode);
		if (actSignIn != null) {
			logger.info("活动业务<|>活动签到<|>结果:成功<|>活动编码:"+activityCode+"<|>userId:"+user.getUserId());
			return new StatusObjDto<ActSignin>(true, actSignIn,	CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		// 签到
		actSignIn = this.toSignIn(user, activityCode);
		actSignIn = this.isSignIn(user, activityCode);
		if (actSignIn != null) {
			logger.info("活动业务<|>活动签到<|>结果:成功<|>活动编码:"+activityCode+"<|>userId:"+user.getUserId());
			return new StatusObjDto<ActSignin>(true, actSignIn, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		} else {
			return new StatusObjDto<ActSignin>(false, null, ActivityBizException.ACTIVITY_DATABASE_ERROR.getCode(), ActivityBizException.ACTIVITY_DATABASE_ERROR.getMsg());
		}
	}
	
	/**
	 * 同一活动是否已经签到
	 * @param user
	 * @param activityCode
	 * @return
	 */
	private ActSignin isSignIn(AccTokenUser user, String activityCode) {
		ActSignin qry = new ActSignin();
		qry.setActCode(activityCode);
		qry.setUserId(user.getUserId());
		qry.setStatus(ActSignin.STATUS_VALID);
		List<ActSignin> list = actSigninDao.findWechatInfoList(qry);
//		List<ActSignin> list = actSigninDao.findList(qry);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 保存签到
	 * @param user
	 * @param activityCode
	 * @return
	 */
	private ActSignin toSignIn(AccTokenUser user, String activityCode) {
		ActSignin actSignIn = new ActSignin();
		actSignIn.setActCode(activityCode);
		actSignIn.setSignInCode(this.generateSignInCode(activityCode));
		actSignIn.setSigninTime(new Date(System.currentTimeMillis()));
		actSignIn.setStatus(ActSignin.STATUS_VALID);
		actSignIn.setUserId(user.getUserId());
		int r = actSigninDao.insert(actSignIn);
		if (r > 0) {
			return actSignIn;
		}
		return null;
	}
	
	/**
	 * 生成签到码
	 * @param activityCode
	 * @return
	 */
	private String generateSignInCode(String activityCode) {
		if (ACTIVITY_EXPOTURNTABLE.equals(activityCode)) {
			return null;
		} // 在这里写生成规则
		return null;
	}

	@Override
	public StatusObjDto<List<ActSignin>> getSignInList(String activityCode, Integer pageIndex, Integer pageSize) {
		StatusDto isValid = super.isValid(activityCode);
		if (!isValid.isOk()) {
			return new StatusObjDto<List<ActSignin>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		List<ActSignin> signInList = actSigninDao.findValidSignInList(activityCode, pageIndex, pageSize);
		if (signInList != null) {
			return new StatusObjDto<List<ActSignin>>(true, signInList, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		return new StatusObjDto<List<ActSignin>>(false, null, ActivityBizException.ACTIVITY_NO_STATISTICS.getCode(), ActivityBizException.ACTIVITY_NO_STATISTICS.getMsg());
	}
}
