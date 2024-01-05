package com.wlzq.activity.base.biz.impl;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActMessageBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.dao.ActMessageDao;
import com.wlzq.activity.base.model.ActMessage;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.AccUser;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.common.account.AccountUserBiz;
import com.wlzq.service.base.sys.basesensitiveword.utils.SensitiveWordFilterUtil;

/**
 * 
 * @author cjz
 *
 */
@Service
public class ActMessageBizImpl extends ActivityBaseBiz implements ActMessageBiz {
	
	private Logger logger = LoggerFactory.getLogger(ActMessageBizImpl.class);
	
	private static final String UTF8 = "utf-8";
	
	@Autowired
	private ActMessageDao actMessageDao;
	@Autowired
	private AccountUserBiz accountUserBiz;

	@Override
	public StatusObjDto<ActMessage> leavingMessage(AccTokenUser user, String activityCode, String content) {
		StatusDto isValid = super.isValid(activityCode);
		if (!isValid.isOk()) {
			logger.info("活动业务<|>活动留言<|>结果:失败<|>code:" + isValid.getCode()+"<|>msg:"+isValid.getMsg());
			return new StatusObjDto<ActMessage>(false, null, isValid.getCode(), isValid.getMsg());
		}
		// 检查token
		if (user == null) {
			logger.info("活动业务<|>活动留言<|>结果:失败<|>code:" + ActivityBizException.ACTIVITY_NOT_LOGIN.getCode()+"<|>msg:"+ActivityBizException.ACTIVITY_NOT_LOGIN.getMsg());
			return new StatusObjDto<ActMessage>(false, null, ActivityBizException.ACTIVITY_NOT_LOGIN.getCode(), ActivityBizException.INTERNSHIP_NOT_LOGIN.getMsg());
		}
		// 检查参数
		if (ObjectUtils.isEmptyOrNull(content)) {
			return new StatusObjDto<ActMessage>(false, null, ActivityBizException.ACTIVITY_PARAMS_NOTNULL.getCode(), "content参数不能为空");
		}
		if (this.getStringLength(content) > ActMessage.CONTENT_LEN) {
			return new StatusObjDto<ActMessage>(false, null, ActivityBizException.ACTIVITY_PARAMS_FORMAT_ERROR.getCode(), "content长度过长");
		}
		if (this.isSensitive(content)) {
			return new StatusObjDto<ActMessage>(false, null, ActivityBizException.ACTIVITY_SENSITIVE_WORDS.getCode(), "content存在敏感词");
		}
		if (this.isXss(content)) {
			return new StatusObjDto<ActMessage>(false, null, ActivityBizException.ACTIVITY_SENSITIVE_WORDS.getCode(), "content疑似存在xss");
		}
		
		ActMessage msg = this.toLeaveMessage(user, activityCode, content);
		if (msg != null) {
			return new StatusObjDto<ActMessage>(true, null, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		} else {
			return new StatusObjDto<ActMessage>(false, null, ActivityBizException.ACTIVITY_DATABASE_ERROR.getCode(), ActivityBizException.ACTIVITY_DATABASE_ERROR.getMsg());
		}
	}
	
	/**
	 * 留言
	 * @param user
	 * @param activityCode
	 * @param content
	 * @return
	 */
	private ActMessage toLeaveMessage(AccTokenUser user, String activityCode, String content) {
		ActMessage msg = new ActMessage();
		msg.setActCode(activityCode);
		msg.setContent(content);
		msg.setMsgTime(new Date(System.currentTimeMillis()));
		msg.setStatus(ActMessage.STATUS_VALID);
		msg.setUserId(user.getUserId());
		int r = actMessageDao.insert(msg);
		if (r > 0) {
			return msg;
		}
		return null;
	}
	
	/**
	 * 返回utf8编码的实际长度
	 * @param str
	 * @return
	 */
	private int getStringLength(String str) {
		if (str != null && str.trim().length() > 0) {
			try {
				return str.getBytes(UTF8).length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return 0;
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

	@Override
	public StatusObjDto<List<ActMessage>> getMsgList(Integer maxOrder, Integer length, String activityCode) {
		StatusDto isValid = super.isValid(activityCode);
		if (!isValid.isOk()) {
			logger.info("活动业务<|>活动留言<|>结果:失败<|>code:" + isValid.getCode()+"<|>msg:"+isValid.getMsg());
			return new StatusObjDto<List<ActMessage>>(false, null, isValid.getCode(), isValid.getMsg());
		}
		List<ActMessage> msgList = actMessageDao.findListByMaxId(activityCode, maxOrder, length);
		if (msgList != null && msgList.size() > 0) {
			return new StatusObjDto<List<ActMessage>>(true, msgList, CodeConstant.SUCCESS, CodeConstant.SUCCESS_MSG);
		}
		return new StatusObjDto<List<ActMessage>>(true, msgList, CodeConstant.SUCCESS, "没有数据");
	}

	@Override
	public boolean isLeftMessage(String activityCode, String openId) {
		return this.isLeftMsg(activityCode, openId, null);
	}

	@Override
	public boolean isLeftValidMessage(String activityCode, String openId) {
		return this.isLeftMsg(activityCode, openId, ActMessage.STATUS_VALID);
	}
	
	/**
	 * 是否有活动留言
	 * @param activityCode
	 * @param openId
	 * @param msgStatus
	 * @return
	 */
	private boolean isLeftMsg(String activityCode, String openId, Integer msgStatus) {
		StatusDto isValid = super.isValid(activityCode);
		if (isValid.isOk()) {
			AccUser accuser = accountUserBiz.findByOpenId(openId);
			if (accuser != null) {
				ActMessage qry = new ActMessage();
				qry.setActCode(activityCode);
				qry.setUserId(accuser.getUserId());
				if (msgStatus != null) {
					qry.setStatus(ActMessage.STATUS_VALID);
				}
				List<ActMessage> list = actMessageDao.findList(qry);
				if (list != null && list.size() > 0) {
					return true;
				}
			}
		}
		return false;
	}

}
