/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.voteworks.model;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 投票作品Entity
 * @author louie
 * @version 2018-08-10
 */
public class VoteWorks {
	private String no;		// 编号
	private String name;		// 名称
	private String department;		// 部门
	private String cover;		// 封面
	private List<String> covers;	// 封面列表
	private String thumbnail;		// 缩略图
	private Long likeCount;		// 点赞数
	private Long hot;		// 热度
	private Integer tag;
	private Integer order;		// 排行
	@JsonIgnore
	private Date updateTime;		// 更新时间
	@JsonIgnore
	private String activitycode;	//活动编码
	private String  aliyunUrl;		// 阿里云链接
	private String brief;	//备注
	
	public VoteWorks() {
		super();
	}

	@Length(min=1, max=64, message="编号长度必须介于 1 和 64 之间")
	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}
	
	@Length(min=1, max=128, message="名称长度必须介于 1 和 128 之间")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	@Length(min=1, max=128, message="封面长度必须介于 1 和 128 之间")
	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}
	
	@Length(min=1, max=128, message="缩略图长度必须介于 1 和 128 之间")
	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}
	
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Long getHot() {
		return hot;
	}

	public void setHot(Long hot) {
		this.hot = hot;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
	
	public String getActivitycode() {
		return activitycode;
	}
	
	public void setActivitycode(String activitycode) {
		this.activitycode = activitycode;
	}
	
	public String getAliyunUrl() {
		return aliyunUrl;
	}
	
	public void setAliyunUrl(String aliyunUrl) {
		this.aliyunUrl = aliyunUrl;
	}
	
	public Integer getTag() {
		return tag;
	}
	
	public void setTag(Integer tag) {
		this.tag = tag;
	}
	
	public List<String> getCovers() {
		return covers;
	}
	
	public void setCovers(List<String> covers) {
		this.covers = covers;
	}

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}
	
}