package com.wlzq.activity.redenvelope.observer;

import java.util.ArrayList;
import java.util.List;

import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.virtualfin.biz.impl.ExpGoldBizImpl;
import com.wlzq.core.SpringApplicationContext;

public class Observers {
	private static final Class[] observerClasses = new Class[] {ActPrizeBiz.class, ExpGoldBizImpl.class};
	
	public static List<RedEnvelopeObserver> getObservers(){
		List<RedEnvelopeObserver> observers = new ArrayList<RedEnvelopeObserver>();
		for(Class cls:observerClasses) {
			RedEnvelopeObserver observer = (RedEnvelopeObserver) SpringApplicationContext.getBean(cls);
			observers.add(observer);
		}
		return observers;
	}
}
