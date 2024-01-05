/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.base.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 活动奖品管理Entity
 * @author louie
 * @version 2018-05-25
 */
@Data
@Accessors(chain = true)
public class ActPrize {
	public static final Integer STATUS_NOT_SEND = 1;
	public static final Integer STATUS_SEND = 2;
	public static final Integer STATUS_USED = 3;
	public static final Integer STATUS_OCCUPY = 4;

	/** level2 */
	public static final Integer TYPE_REDEEM = 1;
	/** 京东卡 */
	public static final Integer TYPE_CARD_PASSWORD = 2;
	/** 积分 */
	public static final Integer TYPE_POINT = 3;
	/** 优惠券 */
	public static final Integer TYPE_COUPON = 4;
	/** 红包 */
	public static final Integer TYPE_REDENVELOPE = 5;
	/** 第一财经 */
	public static final Integer TYPE_FIRST_FIN_CARD = 6;
	/** 爱奇艺 */
	public static final Integer TYPE_AIQY = 7;
	/** 优酷 */
	public static final Integer TYPE_YOUKU = 8;
	/** 实物 */
	public static final Integer TYPE_DELIVERED = 15;
	/** 红包, 用于替换 5 */
	public static final Integer TYPE_NEW_REDENVELOPE = 9;
	/** 代金券 */
	public static final Integer TYPE_DJQ = 16;
	/** ALV2 */
	public static final Integer TYPE_ALV2 = 14;
	/** 兑换码和优惠券 */
	public static final Integer TYPE_REDEEM_AND_COUPON = 10;

	private Long id;
	/** 类型，1：兑换码，2：卡密码，4：优惠券 */
	private Integer type;
	/** 编码 */
	private String code;
	/** 编码 */
	private List<String> codes;
	/** 奖品名称 */
	private String name;
	/** 奖品使用时间 */
	private Integer time;
	/** 兑换码/优惠券编码 */
	private String redeemCode;
	/** 卡编码 */
	private String cardNo;
	/** 卡密码 */
	private String cardPassword;
	/** 用户ID */
	private String userId;
	/** openID */
	private String openId;
	/** 昵称 */
	private String nickName;
	/** 价值 */
	private Double worth;
	/** 状态，1：未发出，2：已发出, 3:已使用，4：已占用待领取 */
	private Integer status;
	/** 活动编码 */
	private String activityCode;
	/** 活动编码 */
	private List<String> activityCodes;
	/** 活动组编码 */
	private String activityGroupCode;
	private Date createTime;
	/** 更新时间 */
	private Date updateTime;
	/** 领取时间 */
	private Date receiveTime;
	/** 领取客户号 */
	private String customerId;
	/** 领取手机号 */
	private String mobile;
	/** 红包金额 */
	private Integer redEnvelopeAmt;
	/** 红包链接 */
	private String redEnvelopeUrl;
	/** 联合已占用的券 */
	private Integer unionOccupy;
	/** 起始时间 */
	private Date updateTimeFrom;
	/** 终止时间 */
	private Date updateTimeTo;
	private String remark;
	/** 奖品类型数组 */
	private String[] priceTypes;
	/** 唯一类型: 1-手机号 + 客户号, 2-手机号 or 客户号, 3-手机号, 4-客户号 */
	private Integer uniqueType;
	/** 奖品编码是否夸活动唯一 */
	private Boolean globalUniquePrizeType;
	/** 次数限制类型。1：一次性奖品 ；2：每天一次 ；3：每天多次 */
	private Integer limitTimesType;
	/** 每天最大次数 */
	private Integer maxTimesDaily;
	/** 是否中奖 */
	private Integer hit;
	/** 积分(应用场景：奖品可以由多少积分兑换) */
	private Integer point;
	private String activityName;
	private List<Long> ids;
	private Integer isDeleted;
	private Integer redenvelopeAmt;
}
