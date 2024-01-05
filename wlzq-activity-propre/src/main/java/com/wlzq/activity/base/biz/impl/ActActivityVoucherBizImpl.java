package com.wlzq.activity.base.biz.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.biz.ActActivityVoucherBiz;
import com.wlzq.activity.base.dao.ActActivityVoucherDao;
import com.wlzq.activity.base.model.ActActivityVoucher;

@Service
public class ActActivityVoucherBizImpl implements ActActivityVoucherBiz {

	@Autowired
	private ActActivityVoucherDao actActivityVoucherDao;
	
	@Override
	public ActActivityVoucher generateVoucher(String userId, String activityCode) {
		ActActivityVoucher aav = new ActActivityVoucher();
		aav.setActivityCode(activityCode);
		aav.setStatus(ActActivityVoucher.STATUS_VALID);
		aav.setUserId(userId);
		aav.setCreateTime(new Date(System.currentTimeMillis()));
		actActivityVoucherDao.insert(aav);
		
		aav.setVoucher(this.frontCompWithZero(aav.getId().intValue(), 6));
		actActivityVoucherDao.update(aav);
		return aav;
	}

	private String frontCompWithZero(int SourceDate, int formatLength) {
		String newString = String.format("%0" + formatLength + "d", SourceDate);
		return newString;
	}
	
}
