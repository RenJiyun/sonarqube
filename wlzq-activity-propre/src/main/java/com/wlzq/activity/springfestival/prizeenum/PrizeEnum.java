package com.wlzq.activity.springfestival.prizeenum;

public enum PrizeEnum {

	PRIZE_THANKS(0, "谢谢参与", null),
	PRIZE_LEVEL2_30(1, "Level-2 1个月", null),
	PRIZE_JDCARN_200(2, "京东E卡200元", null),
	PRIZE_AIQIYI_30(3, "爱奇艺VIP 1个月", null),
//	PRIZE_INVEST_DISCOUNT_6(4, "投顾产品6折券", null);
	PRIZE_LEVEL2_NEWYEARSDAY_2021(4, "Level-2使用权", "PRIZE.LEVEL2.MONTH3"),
	PRIZE_AIQIYI3_30(5, "8.8折投顾产品优惠券", "PRIZE.COUPON.ADVISERDISCOUNT88"),
	PRIZE_AIQIYIF_30(6, "7折投顾产品优惠券", "PRIZE.COUPON.ADVISERDISCOUNT7"),
	PRIZE_JDCARNS_200(7, "投顾产品免单券", "PRIZE.COUPON.ADVISERFREE");
	
	private Integer type;
	private String prizeName;
	private String prizeCode;
	private PrizeEnum(Integer type, String prizeName, String prizeCode) {
		this.prizeCode = prizeCode;
		this.type = type;
		this.prizeName = prizeName;
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
	public String getPrizeCode() {
		return prizeCode;
	}
	public void setPrizeCode(String prizeCode) {
		this.prizeCode = prizeCode;
	}
	
	public static PrizeEnum getByName(String prizeName) {
		for (PrizeEnum each : PrizeEnum.values()) {
			if (each.getPrizeName().equals(prizeName)) {
				return each;
			}
		}
		return null;
	}

	public static PrizeEnum getByPrizeCode(String code) {
		for (PrizeEnum each : PrizeEnum.values()) {
			String pCode = each.getPrizeCode();
			if (pCode != null && each.getPrizeCode().equals(code)) {
				return each;
			}
		}
		return null;
	}
}
