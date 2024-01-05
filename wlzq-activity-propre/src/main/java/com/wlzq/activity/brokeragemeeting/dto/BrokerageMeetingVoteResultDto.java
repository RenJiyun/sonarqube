package com.wlzq.activity.brokeragemeeting.dto;

import java.util.List;

import com.wlzq.activity.brokeragemeeting.model.ActBrokeragemeetingVote;

public class BrokerageMeetingVoteResultDto {

	private List<ActBrokeragemeetingVote> list;
	
	private Integer personNum;

	public List<ActBrokeragemeetingVote> getList() {
		return list;
	}

	public void setList(List<ActBrokeragemeetingVote> list) {
		this.list = list;
	}

	public Integer getPersonNum() {
		return personNum;
	}

	public void setPersonNum(Integer personNum) {
		this.personNum = personNum;
	}

}
