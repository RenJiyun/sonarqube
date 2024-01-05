package com.wlzq.activity.returnvisit.biz;

public interface ReturnVisitActBiz {

	public int getAvailableDrawsCount(String customerId, String source, String businessType);
	
	public int updateDraws(String customerId, String source, String businessType);
}
