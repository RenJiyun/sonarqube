/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.brokeragemeeting.model;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 经纪业务会议游戏关键词Entity
 * @author cjz
 * @version 2019-01-21
 */
public class ActBrokeragemeetingWord {
	
	@JsonIgnore
	private Integer id;
	private String word;		// 关键词
	@JsonIgnore
	private Integer status;		// 状态
	
	public ActBrokeragemeetingWord() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Length(min=0, max=64, message="关键词长度必须介于 0 和 64 之间")
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}