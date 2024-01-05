package com.wlzq.activity.voteworks.dto;

import java.util.List;

import com.wlzq.activity.voteworks.model.VoteWorksMessage;

/**
 * 留言dto
 * @author 
 * @version 1.0
 */
public class MessagesDto {	
	private Integer all;     //总留言数
	private Integer total; //当前页留言数
	private List<VoteWorksMessage> info;		// 留言
	
	public List<VoteWorksMessage> getInfo() {
		return info;
	}
	public void setInfo(List<VoteWorksMessage> info) {
		this.info = info;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public Integer getAll() {
		return all;
	}
	public void setAll(Integer all) {
		this.all = all;
	}
	
}

