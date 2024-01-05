package com.wlzq.activity.base.biz;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.dao.ActShareDao;
import com.wlzq.activity.base.model.ActShare;
import com.wlzq.common.utils.ObjectUtils;
/**
 * 活动分享业务类
 * @author 
 * @version 1.0
 */
@Service
public class ActShareBiz{
	@Autowired
	private ActShareDao shareDao;

	/**
	 * 保存分享信息
	 * @param type 类型，1：微信分享
	 * @param activity 活动编码
	 * @param userId
	 * @param openId
	 * @param customerId
	 * @return
	 */
	public int saveShare(Integer type,String activity,String userId,String openId,String customerId) {
		ActShare share = new ActShare();
		share.setActivity(activity);
		share.setType(type);
		share.setUserId(userId);
		share.setOpenid(openId);
		share.setCustomerId(customerId);
		share.setCreateTime(new Date());
		return shareDao.insert(share);
	}

	/**
	 * 查询分享次数
	 * @param activityCode
	 * @param userId
	 * @param timeFrom
	 * @param timeTo
	 * @return
	 */
	public int shareCount(String activityCode,String userId,Date timeFrom,Date timeTo) {
		if(ObjectUtils.isEmptyOrNull(activityCode)) return 0;
		if(ObjectUtils.isEmptyOrNull(userId)) return 0;
		ActShare share = new ActShare();
		share.setActivity(activityCode);
		share.setUserId(userId);
		share.setTimeStart(timeFrom);
		share.setTimeEnd(timeTo);
		return  shareDao.findCount(share);
	}
	
	/**
	 * 分享次数统计
	 * @param activity
	 * @param userId
	 * @param openId
	 * @param customerId
	 * @param threeInOne
	 * @return
	 */
	public int shareCount (String activity, String userId, String openId, String customerId, Integer threeInOne, Date timeStart, Date timeEnd) {
		ActShare shareEntity = new ActShare();
		shareEntity.setActivity(activity);
		shareEntity.setUserId(userId);
		shareEntity.setOpenid(openId);
		shareEntity.setCustomerId(customerId);
		shareEntity.setTimeStart(timeStart);
		shareEntity.setTimeEnd(timeEnd);
		shareEntity.setThreeInOne(threeInOne);
		int shareCount = shareDao.findCount(shareEntity);
		return shareCount;
	}

}
