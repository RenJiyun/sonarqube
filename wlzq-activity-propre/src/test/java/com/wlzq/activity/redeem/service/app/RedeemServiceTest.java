package com.wlzq.activity.redeem.service.app;

import com.alibaba.fastjson.JSONObject;
import com.wlzq.remote.service.utils.TestUtil;
import org.junit.Test;

/**
 * @author luohc
 * @date 2021/8/23 15:59
 */
public class RedeemServiceTest {

    @Test
    public void receive() {
        JSONObject jo = new JSONObject();
        jo.put("code","2021072203085542129072");
        TestUtil.test("base.couponcooperation.receivelevel2", jo,"tSWzCDYzUUsriBTCutuTvavNCFlXwEAN");
    }
}