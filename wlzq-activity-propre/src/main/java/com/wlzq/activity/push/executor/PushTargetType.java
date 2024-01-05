package com.wlzq.activity.push.executor;


public enum PushTargetType {
	
	CUSTOMER_ID(1, "客户号"),
	MOBILE(2, "手机号"),
	FUND_ACCOUNT(3, "资金账号"),
	WECHAT_OPENID(4, "openid");
	
	private  Integer targetType;
	private  String targetCode;
	
	PushTargetType(Integer targetType, String targetCode) {
		this.targetType = targetType;
		this.targetCode = targetCode;
	}

	public Integer getTargetType() {
		return targetType;
	}

	public void setTargetType(Integer targetType) {
		this.targetType = targetType;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

}
