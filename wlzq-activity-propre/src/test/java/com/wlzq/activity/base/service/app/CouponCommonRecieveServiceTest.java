package com.wlzq.activity.base.service.app;

import com.alibaba.fastjson.JSONObject;
import com.wlzq.remote.service.utils.TestUtil;
import org.junit.Test;

/**
 * @author luohc
 * @date 2021/10/13 18:25
 */
public class CouponCommonRecieveServiceTest {

    @Test
    public void recieve新客大礼包领取优惠券奖品() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("activityCode","ACTIVITY.NEW.CUSTOMER.GIFTBAG");
        jsonObject.put("prizeType","COUPON.INVEST.NEW.CUSTOMER.GIFTBAG1.202110,COUPON.INVEST.NEW.CUSTOMER.GIFTBAG2.202110,COUPON.INVEST.NEW.CUSTOMER.GIFTBAG3.202110,COUPON.INVEST.NEW.CUSTOMER.GIFTBAG4.202110");
        TestUtil.test("activity.commoncouponrecieve.receive",jsonObject,
                "hmLTAbevpgmltKukSQInHQUyecoAWtMO","QGlLTmTjCYVWfxwflydVznJjevjyPjsW");
    }

    @Test
    public void recieve新客大礼包领取优惠券奖品2() {
        JSONObject jsonObject = new JSONObject();
        TestUtil.test("activity.commoncouponrecieve.receivenewcustomergiftbag",jsonObject,
                "hmLTAbevpgmltKukSQInHQUyecoAWtMO","QGlLTmTjCYVWfxwflydVznJjevjyPjsW");
    }
    @Test public void recieve新客大礼包领取优惠券奖品3() {
        JSONObject jsonObject = new JSONObject();
        TestUtil.test("activity.commoncouponrecieve.receivenewcustomergiftbag",jsonObject,
                "hmLTAbevpgmltKukSQInHQUyecoAWtMO","ZoWZXOYzWZTJPpgcwvwjtzlSTkpfXnbx");
    }

    @Test public void 领取用户号占用的奖品() {
        TestUtil.test("activity.common.receiveoccupydprize","",
                "tSWzCDYzUUsriBTCutuTvavNCFlXwEAN","zAFeWJUGKDTijcxjQtkGQXwNgGtYgcyW");
    }


}