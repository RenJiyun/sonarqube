package com.wlzq.activity.voteworks.dto;

import java.util.List;

/**
 * 点赞dto
 * @author 
 * @version 1.0
 */
public class LikeInfoDto {
	private Long all;
	private Integer total;
	private List<LikeDto> info;
	public Long getAll() {
		return all;
	}
	public void setAll(Long all) {
		this.all = all;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public List<LikeDto> getInfo() {
		return info;
	}
	public void setInfo(List<LikeDto> info) {
		this.info = info;
	}
	
}

