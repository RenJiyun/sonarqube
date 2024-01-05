package com.wlzq.activity.base.model;

import com.google.common.collect.Lists;
import com.wlzq.activity.base.biz.ActLotteryBiz;
import com.wlzq.activity.base.biz.ActivityBiz;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wlzq
 */
public enum ActLotteryEnum {

    FINSECTION_THANKS(0.95, 1.0, null),
    FINSECTION_VEST_COUPON(0.35, 0.95, "PRIZE.818.2019.2.2.COUPON.VEST"),
    FINSECTION_LEVEL2(0.05, 0.35, "PRIZE.818.2019.2.2.COUPON.LEVEL2"),
    FINSECTION_REDENVELOPE(0.0, 0.05, "PRIZE.818.2019.2.2.COUPON.REDENVELOPE"),

    OLDCUSRETVIS2021_LEVEL2_1M(0.2692, 1.0, "PRIZE.CUSRETVIS2021.LEVEL2.1M"),
    OLDCUSRETVIS2021_LEVEL2_3M(0.0256, 0.2692, "PRIZE.CUSRETVIS2021.LEVEL2.3M"),
    OLDCUSRETVIS2021_JD10(0.0012, 0.0256, "PRIZE.CUSRETVIS2021.JD10"),
    OLDCUSRETVIS2021_JD20(0.0, 0.0012, "PRIZE.CUSRETVIS2021.JD20"),

    CYBREFORM202009_LEVEL2_1M(0.2372, 1.0, "PRIZE.CYBANSER.REDEEM1M"),
    CYBREFORM202009_LEVEL2_3M(0.0084, 0.2372, "PRIZE.CYBANSER.REDEEM3M"),
    CYBREFORM202009_JD10(0.0008, 0.0084, "PRIZE.CYBANSER.JDK10"),
    CYBREFORM202009_YKCARD(0.0, 0.0008, "PRIZE.CYBANSER.YKCARD"),


    /**
     * 38财女节客户抽奖
     **/
    LOTTER_202338_DISCOUNT5_1(0.0, 0.0571, "PRIZE.COUPON.INVEST.202338.DISCOUNT5"),
    LOTTER_202338_LEVEL2_1M_1(0.0571, 0.5714, "PRIZE.LEVEL2.202338.1MONTH"),
    LOTTER_202338_REDB_18(0.5714, 0.8571, "PRIZE.REDB.202338.1.8"),
    LOTTER_202338_REDB_38(0.8571, 1.0, "PRIZE.REDB.202338.3.8"),

    /**
     * 38财女节用户户抽奖
     **/
    LOTTER_202338_DISCOUNT5_2(0.0, 0.0857, "PRIZE.COUPON.INVEST.202338.DISCOUNT5"),
    LOTTER_202338_LEVEL2_1M_2(0.0857, 1.0, "PRIZE.LEVEL2.202338.1MONTH"),


    LOTTERY_2023_12FREE_03(0.0, 0.4, "PRIZE.REDB.2023.DAILY.LOTTERY.0.3", "0.3元红包", new BigDecimal("0.3")),
    LOTTERY_2023_12FREE_05(0.4, 0.75, "PRIZE.REDB.2023.DAILY.LOTTERY.0.5", "0.5元红包", new BigDecimal("0.5")),
    LOTTERY_2023_12FREE_08(0.75, 0.9895, "PRIZE.REDB.2023.DAILY.LOTTERY.0.8", "0.8元红包", new BigDecimal("0.8")),
    LOTTERY_2023_12FREE_12(0.9895, 0.9995, "PRIZE.REDB.2023.DAILY.LOTTERY.1.2", "1.2元红包", new BigDecimal("1.2")),
    LOTTERY_2023_12FREE_88(0.9995, 1.0, "PRIZE.REDB.2023.DAILY.LOTTERY.88", "88元红包", new BigDecimal("88")),
    LOTTERY_2023_12FREE_1M(0.0, 1.0, "PRIZE.LEVEL2.202303.DAILY.LOTTERY.1MONTH", "免费level2一个月", BigDecimal.ONE),


    LOTTERY_VAS_PAZ_SWEEPING_ROBOT(0.0, 0.0001, "PRIZE.VAS.PZB.ROBOT", "米家扫地机器人", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_TENT(0.0001, 0.0011, "PRIZE.VAS.PZB.TENT", "京东京造天幕帐篷", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_TENT_DRONE(0.0011, 0.0021, "PRIZE.VAS.PZB.DRONE", "LOPOM航拍无人机", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_LCD_HANDWRITING(0.0021, 0.0071, "PRIZE.VAS.PZB.HANDWRITING", "汉王液晶手写屏", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_FRISBEE(0.0071, 0.0121, "PRIZE.VAS.PZB.FRISBEE", "麦瑞克飞盘", BigDecimal.ONE),

    LOTTERY_VAS_PAZ_JDK_100(0.0121, 0.0126, "PRIZE.VAS.PZB.JDK100", "100元京东卡", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_JKD_50(0.0126, 0.0136, "PRIZE.VAS.PZB.JDK50", "50元京东卡", BigDecimal.ONE),
    LOTTERY_VAS_FUEL_CARD_50(0.0136, 0.0156, "PRIZE.VAS.PZB.FUEL.CARD50", "50元中石油加油卡", BigDecimal.ONE),

    LOTTERY_VAS_PAZ_588(0.0156, 0.05, "PRIZE.VAS.PZB.RED.PACKET.588", "5.88元红包", new BigDecimal("5.88")),
    LOTTERY_VAS_PAZ_288(0.05, 0.1, "PRIZE.VAS.PZB.RED.PACKET.288", "2.88元红包", new BigDecimal("2.88")),
    LOTTERY_VAS_PAZ_188(0.1, 0.2, "PRIZE.VAS.PZB.RED.PACKET.188", "1.88元红包", new BigDecimal("1.88")),
    LOTTERY_VAS_PAZ_088(0.2, 0.5, "PRIZE.VAS.PZB.RED.PACKET.088", "0.88元红包", new BigDecimal("0.88")),
    LOTTERY_VAS_PAZ_018(0.5, 1.0, "PRIZE.VAS.PZB.RED.PACKET.018", "0.18元红包", new BigDecimal("0.18")),

    LOTTERY_VAS_PAZ_ZTZS(0.0, 0.25, "PRIZE.VAS.PZB.ZTZS", "涨停助手一个月", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_L2(0.25, 0.75, "PRIZE.VAS.PZB.L2", "Level-2一个月", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_COUPON50(0.75, 0.875, "PRIZE.VAS.PZB.COUPON50", "50元决策资讯产品代金券", BigDecimal.ONE),
    LOTTERY_VAS_PAZ_COUPON100(0.875, 1.0, "PRIZE.VAS.PZB.COUPON100", "100元决策资讯产品代金券", BigDecimal.ONE),

    // 以下为公告全知道活动抽奖奖品
    // 第一奖池
    LOTTERY_GGQZD_L2_FREE_30(0.0, 0.33, "PRIZE.GGQZD.L2.FREE.30", "Level-2一个月", BigDecimal.ONE),
    LOTTERY_GGQZD_RED_PACKET_288(0.33, 0.59, "PRIZE.GGQZD.RED.PACKET.288", "2.88元红包", new BigDecimal("2.88")),
    LOTTERY_GGQZD_RED_PACKET_188(0.59, 0.85, "PRIZE.GGQZD.RED.PACKET.188", "1.88元红包", new BigDecimal("1.88")),
    LOTTERY_GGQZD_IQIYI(0.85, 0.8501, "PRIZE.GGQZD.IQIYI", "爱奇艺视频月卡", BigDecimal.ONE),
    LOTTERY_GGQZD_XTJJDS1(0.8501, 1.0, "PRIZE.GGQZD.XTJJDS1", "形态掘金大师一个月", BigDecimal.ONE),

    // 第二奖池
    LOTTERY_GGQZD_RED_PACKET_088(0.0, 0.50, "PRIZE.GGQZD.RED.PACKET.088", "0.88元红包", new BigDecimal("0.88")),
    LOTTERY_GGQZD_RED_PACKET_688(0.50, 0.55, "PRIZE.GGQZD.RED.PACKET.688", "6.88元红包", new BigDecimal("6.88")),
    LOTTERY_GGQZD_RED_PACKET_888(0.55, 0.56, "PRIZE.GGQZD.RED.PACKET.888", "8.88元红包", new BigDecimal("8.88")),
    LOTTERY_GGQZD_JCZX_100(0.56, 1.0, "PRIZE.GGQZD.JCZX.100", "100元决策资讯产品代金券", BigDecimal.ONE),

    // 兜底奖池
    LOTTERY_GGQZD_JCZX_50(0.0, 1.0, "PRIZE.GGQZD.JCZX.50", "50元决策资讯产品代金券", BigDecimal.ONE);

    public static List<String> TOP_PRIZE_CODES_LOTTERY_PZB = getTopPrizeCodes(ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY_PZB);

    private Double minValue;
    private Double maxValue;
    private String prizeTypeCode;
    private String prizeName;
    private BigDecimal amount;

    ActLotteryEnum(Double minVaule, Double maxValue, String prizeTypeCode) {
        this.maxValue = maxValue;
        this.minValue = minVaule;
        this.prizeTypeCode = prizeTypeCode;
    }

    ActLotteryEnum(Double minVaule, Double maxValue, String prizeTypeCode, String prizeName, BigDecimal amount) {
        this.maxValue = maxValue;
        this.minValue = minVaule;
        this.prizeTypeCode = prizeTypeCode;
        this.prizeName = prizeName;
        this.amount = amount;
    }

    public String getPrizeName() {
        return prizeName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getPrizeTypeCode() {
        return prizeTypeCode;
    }

    public void setPrizeTypeCode(String prizeTypeCode) {
        this.prizeTypeCode = prizeTypeCode;
    }

    public static ActLotteryEnum getActLottery(List<ActLotteryEnum> list, Double random) {
        for (ActLotteryEnum each : list) {
            if (random.compareTo(each.minValue) >= 0 && random.compareTo(each.maxValue) == -1) {
                return each;
            }
        }
        return ActLotteryEnum.FINSECTION_THANKS;
    }

    /**
     * 根据活动编码和用户登录情况，返回可抽奖的对象
     */
    public static List<ActLotteryEnum> getActLotteryEnumList(String activityCode, String userId, String customerId) {
        List<ActLotteryEnum> list = Lists.newArrayList();
        if (ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY_PZB.equals(activityCode)) {
            list.add(LOTTERY_VAS_PAZ_JKD_50);
            list.add(LOTTERY_VAS_PAZ_588);
            list.add(LOTTERY_VAS_PAZ_288);
            list.add(LOTTERY_VAS_PAZ_188);
            list.add(LOTTERY_VAS_PAZ_088);
            list.add(LOTTERY_VAS_PAZ_018);
        }

        if (ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY.equals(activityCode)) {
            list.add(LOTTERY_2023_12FREE_03);
            list.add(LOTTERY_2023_12FREE_05);
            list.add(LOTTERY_2023_12FREE_08);
            list.add(LOTTERY_2023_12FREE_12);
            list.add(LOTTERY_2023_12FREE_88);
        }

        if (ActLotteryBiz.ACTIVITY_202338_LOTTERY1.equals(activityCode) || ActLotteryBiz.ACTIVITY_202338_LOTTERY2.equals(activityCode)) {
            /**客户号不为空奖品列表**/
            if (StringUtils.isNotEmpty(customerId)) {
                list.add(LOTTER_202338_DISCOUNT5_1);
                list.add(LOTTER_202338_LEVEL2_1M_1);
                list.add(LOTTER_202338_REDB_18);
                list.add(LOTTER_202338_REDB_38);
                /**客户号为空，用户号不为空奖品列表**/
            } else if (StringUtils.isNotEmpty(userId)) {
                list.add(LOTTER_202338_DISCOUNT5_2);
                list.add(LOTTER_202338_LEVEL2_1M_2);
            }
        }
        return list;
    }

    public static List<ActLotteryEnum> getActLotteryEnumListSecond(String activityCode, String userId, String customerId) {
        List<ActLotteryEnum> list = Lists.newArrayList();
        if (ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY.equals(activityCode)) {
            list.add(LOTTERY_2023_12FREE_1M);
        }
        if (ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY_PZB.equals(activityCode)) {
            list.add(LOTTERY_VAS_PAZ_ZTZS);
            list.add(LOTTERY_VAS_PAZ_L2);
            list.add(LOTTERY_VAS_PAZ_COUPON50);
            list.add(LOTTERY_VAS_PAZ_COUPON100);
        }
        return list;
    }


    public static List<String> getTopPrizeCodes(String activityCode) {
        List<String> list = Lists.newArrayList();
        if (ActivityBiz.ACTIVITY_2023_12_FREE_LOTTERY_PZB.equals(activityCode)) {
            list.add(LOTTERY_VAS_PAZ_SWEEPING_ROBOT.getPrizeTypeCode());
            list.add(LOTTERY_VAS_PAZ_TENT.getPrizeTypeCode());
            list.add(LOTTERY_VAS_PAZ_TENT_DRONE.getPrizeTypeCode());
            list.add(LOTTERY_VAS_PAZ_LCD_HANDWRITING.getPrizeTypeCode());
            list.add(LOTTERY_VAS_PAZ_FRISBEE.getPrizeTypeCode());
            list.add(LOTTERY_VAS_PAZ_JDK_100.getPrizeTypeCode());
            list.add(LOTTERY_VAS_PAZ_JKD_50.getPrizeTypeCode());
            list.add(LOTTERY_VAS_FUEL_CARD_50.getPrizeTypeCode());
        }
        return list;
    }

}
