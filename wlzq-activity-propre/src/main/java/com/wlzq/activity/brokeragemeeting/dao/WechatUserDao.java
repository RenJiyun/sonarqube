package com.wlzq.activity.brokeragemeeting.dao;

import com.wlzq.activity.brokeragemeeting.model.WechatUser;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;

/**
 * 微信用户DAO接口
 * @author louie
 * @version 2017-08-31
 */
@MybatisScan
public interface WechatUserDao  extends CrudDao<WechatUser>{
	
	WechatUser findByOpenId(String openId);
	
	void updateByOpenId(WechatUser user);
}