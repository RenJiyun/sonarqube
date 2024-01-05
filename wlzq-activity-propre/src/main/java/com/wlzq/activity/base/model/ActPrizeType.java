/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 活动奖品类型
 *
 * @author louie
 */
public class ActPrizeType {
	public static final Integer TYPE_REDEEM = 1;
	public static final Integer TYPE_CARD_PASSWORD = 2;
	public static final Integer TYPE_POINT = 3;

	private String id;
	/** 名称 */
	private String name;
	/** 编码 */
	private String code;
	/** 价值 */
	private Double worth;
	/** 是否为level2奖品，0：否，1：是 */
	private Integer isLevel2;
	/** 类型，1：兑换码，2：卡密码,3:积分 */
	private Integer type;
	/** 使用时间（月） */
	private Integer time;
	/** 备注 */
	private String remark;
	/** 创建时间 */
	private Date createTime;
	/** is_deleted */
	private Integer isDeleted;
	/** 次数限制类型。1：一次性奖品 ；2：每天一次 ；3：每天多次 */
	private Integer limitTimesType;
	/** 每天最大次数 */
	private Integer maxTimesDaily;

	/** 积分(应用场景：奖品可以由多少积分兑换) */
	private Integer point;
	/** 每日总限量 */
	private Integer dailyLimit;
	/** 用户可领取数限量 */
	private Integer limitPerUser;

	public Integer getLimitTimesType() {
		return limitTimesType;
	}

	public void setLimitTimesType(Integer limitTimesType) {
		this.limitTimesType = limitTimesType;
	}

	public Integer getMaxTimesDaily() {
		return maxTimesDaily;
	}

	public void setMaxTimesDaily(Integer maxTimesDaily) {
		this.maxTimesDaily = maxTimesDaily;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ActPrizeType() {
		super();
	}

	@Length(min=0, max=100, message="名称长度必须介于 0 和 100 之间")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Length(min=0, max=100, message="编码长度必须介于 0 和 100 之间")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Double getWorth() {
		return worth;
	}

	public void setWorth(Double worth) {
		this.worth = worth;
	}

	public Integer getIsLevel2() {
		return isLevel2;
	}

	public void setIsLevel2(Integer isLevel2) {
		this.isLevel2 = isLevel2;
	}

	@NotNull(message="类型，1：兑换码，2：卡密码不能为空")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	@Length(min=0, max=500, message="备注长度必须介于 0 和 500 之间")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Integer getPoint() {
		return point;
	}

	public void setPoint(Integer point) {
		this.point = point;
	}

	public Integer getDailyLimit() {
		return dailyLimit;
	}

	public void setDailyLimit(Integer dailyLimit) {
		this.dailyLimit = dailyLimit;
	}

	public Integer getLimitPerUser() {
		return limitPerUser;
	}

	public void setLimitPerUser(Integer limitPerUser) {
		this.limitPerUser = limitPerUser;
	}
}
