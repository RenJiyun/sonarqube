package com.wlzq.activity.base.biz;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.dao.CheckListDao;
import com.wlzq.activity.base.model.CheckList;
import com.wlzq.common.utils.ObjectUtils;
/**
 * 活动审核名单业务类
 * @author 
 * @version 1.0
 */
@Service
public class CheckListBiz{
	@Autowired
	private CheckListDao checkListDao;

	public CheckList infoByUserId(String userId,Integer source) {
		if(ObjectUtils.isEmptyOrNull(userId) || ObjectUtils.isEmptyOrNull(source)) return null;
		
		return 	getCheckList(userId,null,source);
	}

	public CheckList infoByMobile(String mobile,Integer source) {
		if(ObjectUtils.isEmptyOrNull(mobile)|| ObjectUtils.isEmptyOrNull(source)) return null;

		return 	getCheckList(null,mobile,source);		
	}

	private CheckList getCheckList(String userId,String mobile,Integer source) {
		CheckList param = new CheckList();
		param.setUserId(userId);
		param.setMobile(mobile);
		param.setSourceu(source);
		List<CheckList> lists = checkListDao.findList(param);
		
		return lists.size() > 0?lists.get(0):null;
	}
}
