/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * level2领取活动领取记录Entity
 * @author louie
 * @version 2018-05-03
 */
public class Level2Recieve  {
	public static final Integer STATUS_OPEN_SUCCESS = 1;
	public static final Integer STATUS_NOT_OPEN = 2;
	public static final Integer STATUS_NOT_ACTIVE = 3;
	public static final Integer STATUS_NOT_VALID = 4;
	private Long id;
	private Integer goodsId;		// 商品ID
	private String mobile;		// Level2开通手机号
	private Integer type;		// 类型，1：新开户，2：新增有效户，3：新开信用账户，4：邀请好友
	private Long inviteId;		// 邀请ID
	private String userId;		// 用户ID
	private String orderNo;		// 订单号
	private Integer notityStatus;		// ths通知状态，0：失败，1：成功
	private String notityMessage;		// ths通知提示
	private Date createTime;		// 创建时间
	private Date activeTime;		// 自动激活时间
	private Integer status;		// 状态，1：已开通，2：未开通，3:条件不符
	private Date takeTime;		// 领取时间
	private String remark;		// 备注
	
	public Level2Recieve() {
		super();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@NotNull(message="商品ID不能为空")
	public Integer getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
	}
	
	@Length(min=1, max=11, message="Level2开通手机号长度必须介于 1 和 11 之间")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	@NotNull(message="类型，1：新开户，2：新增有效户，3：新开信用账户，4：邀请好友不能为空")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public Long getInviteId() {
		return inviteId;
	}

	public void setInviteId(Long inviteId) {
		this.inviteId = inviteId;
	}

	@Length(min=0, max=64, message="用户ID长度必须介于 0 和 64 之间")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=0, max=100, message="订单号长度必须介于 0 和 100 之间")
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
	public Integer getNotityStatus() {
		return notityStatus;
	}

	public void setNotityStatus(Integer notityStatus) {
		this.notityStatus = notityStatus;
	}
	
	public String getNotityMessage() {
		return notityMessage;
	}

	public void setNotityMessage(String notityMessage) {
		this.notityMessage = notityMessage;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getTakeTime() {
		return takeTime;
	}

	public void setTakeTime(Date takeTime) {
		this.takeTime = takeTime;
	}
	
	@Length(min=0, max=255, message="备注长度必须介于 0 和 255 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Date activeTime) {
		this.activeTime = activeTime;
	}
	
}