package com.wlzq.activity.base.service.cooperation;

import com.alibaba.fastjson.JSONObject;
import com.wlzq.remote.service.utils.TestUtil;
import org.junit.Test;

/**
 * @author luohc
 * @date 2021/7/14 14:07
 */
//@RunWith(SpringRunner.class)
public class CommonCooperationTest {
    @Test
    public void receive() {
        TestUtil.test("activity.couponcooperation.receive",
                "{\"userId\":\"orGdDZbqoXdtFFCRfUNGrTSGjANAAHtn\"," +
                        "\"customerId\":null," +
                        "\"activityCode\":\"ACTIVITY.2021818.PROMOTIONS\"," +
                        "\"prizeType\":\"PRIZE.act2021818.JDK100\"}");
    }

    @Test
    public void receiveMyUserId() {
        TestUtil.test("activity.couponcooperation.receive",
                "{\"userId\":\"hBIlrXzHoNtNlVoyXbfXcfLNFUsCMAUH\"," +
                        "\"customerId\":null," +
                        "\"activityCode\":\"ACTIVITY.2021818.PROMOTIONS\"," +
                        "\"prizeType\":\"COUPON.INVEST.68.2021818PROM\"}");
    }

    /** 双11 领取奖品 */
    @Test public void receiveCoop_11_11() {
        TestUtil.test("activity.couponcooperation.receive",
                "{\"userId\":\"hBIlrXzHoNtNlVoyXbfXcfLNFUsCMAUH\"," +
                        "\"customerId\":null," +
                        "\"activityCode\":\"ACTIVITY.2021DOUBLE11.PROMOTIONS\"," +
                        "\"prizeAmount\":31," +
                        "\"prizeType\":\"COUPON.RED.PACKET.2021.11.11\"}");
    }

    /** 双11 领取奖品 */
    @Test public void receiveCoop_11_11_2() {
        TestUtil.test("activity.couponcooperation.receive",
                "{\"userId\":\"ziEVOpBECiCjQsgbnNeQWsbxZhlimYNa\"," +
                        "\"customerId\":null," +
                        "\"activityCode\":\"ACTIVITY.2021DOUBLE11.PROMOTIONS\"," +
                        "\"prizeAmount\":1," +
                        "\"prizeType\":\"COUPON.RED.PACKET.2021.11.11\"}");
    }

    /** 双11 领取奖品 */
    @Test public void aaaa() {
        TestUtil.test("\tservice.productcooperation.findadviserorders",
                "{\"userId\":\"ziEVOpBECiCjQsgbnNeQWsbxZhlimYNa\"," +
                        "\"customerId\":null," +
                        "\"activityCode\":\"ACTIVITY.2021DOUBLE11.PROMOTIONS\"," +
                        "\"prizeAmount\":1," +
                        "\"prizeType\":\"COUPON.RED.PACKET.2021.11.11\"}");
    }


    @Test public void 双12领取奖品_同花顺() {
        TestUtil.test("activity.couponcooperation.receive",
                "{\"userId\":\"gDhmEnnVcSVzyYdQHtWJulAlIywzeHyE\"," +
                        "\"prizeType\":\"COUPON.INVEST.7DAY.FREE.20211212\"," +
                        "\"activityCode\":\"ACTIVITY.2021.1212.LEVEL2.PROMOTE\"}");
    }


    @Test public void 双12领取奖品_同花顺2() {
        JSONObject json = new JSONObject();
        json.put("userId","nmeVNgCUPRngfkmlhHgRrEElbtbvRRdc");
        json.put("activityCode","ACTIVITY.2021DOUBLE11.PROMOTIONS");
        json.put("prizeType","COUPON.RED.PACKET.2021.11.11");
        json.put("prizeAmount",100);
        json.put("customerId","");

        TestUtil.test("activity.couponcooperation.receive",json);
    }



}