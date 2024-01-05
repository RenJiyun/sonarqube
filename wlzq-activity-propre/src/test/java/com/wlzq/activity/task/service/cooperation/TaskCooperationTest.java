package com.wlzq.activity.task.service.cooperation;

import com.wlzq.activity.task.dto.ActTaskReqDto;
import com.wlzq.common.utils.HttpClientUtils;
import com.wlzq.remote.service.utils.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * @author luohc
 * @date 2021/7/12 17:20
 */
@RunWith(SpringRunner.class)
public class TaskCooperationTest {

    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000000);
        factory.setReadTimeout(2000000);
        restTemplate = new RestTemplate(factory);
    }

    /**
     * 可以多次重复跑
     */
    @Test public void status(){
        ActTaskReqDto reqDto = new ActTaskReqDto()
                .setUserId("YWPZPHAzJnwnSekfPOfRoTfuJedtDYSb").setCustomerId("20000062")
                .setTask("5")
                .setBeginDate(1622792748000L).setEndDate(1630941548000L);
        TestUtil.test("activity.task.status",reqDto);
    }

    /**
     * 万联绩牛双十一淘金游园会0元抽iphone活动（对接需求）
     */
    @Test public void status5(){
        ActTaskReqDto reqDto = new ActTaskReqDto()
                .setUserId("nmeVNgCUPRngfkmlhHgRrEElbtbvRRdc")
                .setTask("5")
                .setBeginDate(1638316800000L).setEndDate(1641859200000L);
        TestUtil.test("activity.task.status",reqDto);
    }
    @Test public void statusTask8() {
        ActTaskReqDto reqDto = new ActTaskReqDto()
//                .setUserId("YWPZPHAzJnwnSekfPOfRoTfuJedtDYSb")
                .setCustomerId("10000008")
                .setTask("8")
                .setBeginDate(1632707984000L).setEndDate(1637978384999L);
        TestUtil.test("activity.task.status",reqDto);
    }
    @Test public void statusTask5_jn() {
        ActTaskReqDto reqDto = new ActTaskReqDto().setAccessToken("yvqYwKIKlJZHgvXPHSmBXUfxtTqvtYUq").setTask("5")
                .setBeginDate(1632707984000L).setEndDate(1637978384999L);
        TestUtil.test("activity.task.status",reqDto);
    }
    @Test public void statusTask8_jn() {
        ActTaskReqDto reqDto = new ActTaskReqDto().setAccessToken("pFQgSBcLKuwClQRrItbZLyMNbinPcrST").setTask("8")
                .setBeginDate(1609430400000L).setEndDate(1640966400000L);
        TestUtil.test("activity.task.status",reqDto);
    }
    @Test public void statusTask4_jn() {
        ActTaskReqDto reqDto = new ActTaskReqDto().setAccessToken("EplcvJeqSGbqqBkPdodihoUEgbabPENS").setTask("4")
                .setBeginDate(1609430400000L).setEndDate(1640966400000L);
        TestUtil.test("activity.task.status",reqDto);
    }

    @Test public void 查询订阅订单列表findadviserorders() {
        ActTaskReqDto reqDto = new ActTaskReqDto()
                .setBeginDate(1609430400000L).setEndDate(1640966400000L);
        TestUtil.test("service.productcooperation.findadviserorders",reqDto);
    }



    @Test public void 获取登录信息() {
        ActTaskReqDto reqDto = new ActTaskReqDto().setAccessToken("EplcvJeqSGbqqBkPdodihoUEgbabPENS");
        TestUtil.test("account.authcooperation.fortuneinfo",reqDto);
    }

    @Test public void 获取登录信息2() {
        ActTaskReqDto reqDto = new ActTaskReqDto().setAccessToken("EplcvJeqSGbqqBkPdodihoUEgbabPENS");
        TestUtil.test("account.authcooperation.fortuneinfobyauthtoken",reqDto);
    }

//    @Test public void getcode() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("busKey","t7iuzglqbudhqq4e2n");
//        jsonObject.put("type","13");
//        jsonObject.put("redirectUrl","http://zy.test.jiniutech.cn/");
////        jsonObject.put("redirectUrl","http%3A%2F%2Fzy.test.jiniutech.cn%2F");
//        TestUtil.test("account.auth.code",jsonObject);
//    }
//    @Test public void getaccesstoken() {
//        ActTaskReqDto reqDto = new ActTaskReqDto().setAccessToken("yvqYwKIKlJZHgvXPHSmBXUfxtTqvtYUq").setTask("8")
//                .setBeginDate(1635091200000L).setEndDate(1635177599999L);
//        TestUtil.test("activity.task.status",reqDto);
//    }






    @Test public void statusTask5() {
        String url = "http://localhost:8081/api/cooperate.do?method=activity.task.status&params=%7B%22customerId%22%3A%2220000102%22%2C%22beginDate%22%3A1628750473750%2C%22task%22%3A%225%22%2C%22userId%22%3A%22TEokCEpudoigUTWWPAvgtMqqIvlKRvYg%22%7D&k=flkti1ud10zabkurpz&noncestr=e33160f9b926e640a8c19227696f3d14";
        Object object = HttpClientUtils.doGet(url, null);
        System.out.println(object);
    }




}