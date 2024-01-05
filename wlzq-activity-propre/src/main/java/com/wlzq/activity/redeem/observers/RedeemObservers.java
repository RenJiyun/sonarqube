package com.wlzq.activity.redeem.observers;

import java.util.ArrayList;
import java.util.List;

import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.core.SpringApplicationContext;

public class RedeemObservers {
	
	private static final Class[] observerClasses = new Class[] {ActPrizeBiz.class};
	
	public static List<RedeemObserver> getObservers(){
		List<RedeemObserver> observers = new ArrayList<RedeemObserver>();
		for(Class cls:observerClasses) {
			RedeemObserver observer = (RedeemObserver) SpringApplicationContext.getBean(cls);
			observers.add(observer);
		}
		return observers;
	}
}
