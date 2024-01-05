/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.l2recieve.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * level2领取活动奖品Entity
 * @author louie
 * @version 2018-05-03
 */
public class Level2RecievePrize  {
	public static final Integer TYPE_NEW_OPEN = 1;
	public static final Integer TYPE_NEW_EFFECTIVE = 2;
	public static final Integer TYPE_NEW_CREDIT = 3;
	public static final Integer TYPE_INVITE = 4;
	
	private Integer type;		// 类型，1：新开户，2：新增有效户，3：新开信用账户，4：邀请好友
	private Integer productId;  //产品ID
	private Integer goodsId;		// 商品ID
	private String goodsName;		// 商品名称
	private Integer goodsTime;		// 商品时间
	private Integer timeType;		// 时间类型，0：日，1：月
	
	public Level2RecievePrize() {
		super();
	}

	@NotNull(message="类型，1：新开户，2：新增有效户，3：新开信用账户，4：邀请好友不能为空")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	@NotNull(message="商品ID不能为空")
	public Integer getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
	}
	
	@Length(min=0, max=64, message="商品名称长度必须介于 0 和 64 之间")
	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	
	public Integer getGoodsTime() {
		return goodsTime;
	}

	public void setGoodsTime(Integer goodsTime) {
		this.goodsTime = goodsTime;
	}
	
	public Integer getTimeType() {
		return timeType;
	}

	public void setTimeType(Integer timeType) {
		this.timeType = timeType;
	}
	
}