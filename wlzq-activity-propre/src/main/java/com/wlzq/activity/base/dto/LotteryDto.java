package com.wlzq.activity.base.dto;

/**
 * 抽奖中奖dto
 * 
 * @author
 * @version 1.0
 */
public class LotteryDto {
	public static final Integer STATUS_NOT_HIT = 0;
	public static final Integer STATUS_HIT = 1;
	public static final Integer STATUS_RECIEVE_FAILED = 2;
	public static final Integer TYPE_LEVEL2_1 = 1;
	public static final Integer TYPE_JDECARD_8 = 2;
	public static final Integer TYPE_COUPON_DISCOUNT_3 = 3;

	private Integer redEnvelopeAmt; // 红包金额
	private String redEnvelopeUrl; // 红包链接

	/** 状态，0：未中奖，1：中奖,2:领取失败 */
	private Integer status;
	/** 奖品类型 */
	private Integer type;
	/** 价值 */
	private Double worth;
	/** 奖品名称 */
	private String prizeName;
	/** 领奖编号 */
	private String recieveCode;
	
	private Integer couponType;		// 优惠券类型,1:兑换码，2：京东卡，4：优惠券
	private String description;  //奖品描述
	private Integer time;		// 使用时间（月）
	private Double discount;   //折扣
	private String openUrl; //(优惠券)打开地址
	private String cardNo;		// 卡编码
	private String cardPassword;		// 卡密码
	
	private Long prizeId;		//奖品Id

	public Integer getRedEnvelopeAmt() {
		return redEnvelopeAmt;
	}

	public String getRedEnvelopeUrl() {
		return redEnvelopeUrl;
	}

	public void setRedEnvelopeAmt(Integer redEnvelopeAmt) {
		this.redEnvelopeAmt = redEnvelopeAmt;
	}

	public void setRedEnvelopeUrl(String redEnvelopeUrl) {
		this.redEnvelopeUrl = redEnvelopeUrl;
	}

	public static final LotteryDto getThanks() {
		LotteryDto dto = new LotteryDto();
		dto.setStatus(STATUS_NOT_HIT);
		return dto;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getPrizeName() {
		return prizeName;
	}

	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}

	public Double getWorth() {
		return worth;
	}

	public void setWorth(Double worth) {
		this.worth = worth;
	}

	public String getRecieveCode() {
		return recieveCode;
	}

	public void setRecieveCode(String recieveCode) {
		this.recieveCode = recieveCode;
	}

	public Integer getCouponType() {
		return couponType;
	}

	public void setCouponType(Integer couponType) {
		this.couponType = couponType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	public Double getDiscount() {
		return discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public String getOpenUrl() {
		return openUrl;
	}

	public void setOpenUrl(String openUrl) {
		this.openUrl = openUrl;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardPassword() {
		return cardPassword;
	}

	public void setCardPassword(String cardPassword) {
		this.cardPassword = cardPassword;
	}
	
	public Long getPrizeId() {
		return prizeId;
	}
	
	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}

}
