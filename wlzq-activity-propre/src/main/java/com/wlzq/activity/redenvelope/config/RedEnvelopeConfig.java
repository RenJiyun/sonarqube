package com.wlzq.activity.redenvelope.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pay.sd.service.redenvelope") 
public class RedEnvelopeConfig {
	private String url;
	private String mchNo;
	private String sceneNo;
	private String secret;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMchNo() {
		return mchNo;
	}
	public void setMchNo(String mchNo) {
		this.mchNo = mchNo;
	}
	public String getSceneNo() {
		return sceneNo;
	}
	public void setSceneNo(String sceneNo) {
		this.sceneNo = sceneNo;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}

}
