package com.wlzq.activity.virtualfin.service.app;

import com.wlzq.common.utils.HttpClientUtils;
import com.wlzq.remote.service.utils.TestUtil;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;

/**
 * @author luohc
 * @date 2021/7/19 17:08
 */
public class VirtualFinServiceTest {
    String host = "http://127.0.0.1:8081/api/cooperate?";

    @Test
    public void dotask(){
        String url= host+"method=activity.virtualfin.dotask&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%2C%22taskCode%22%3A%22TASK.ACT.818.2021.ARTICLE.LIKE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=9529e1a119cb5c4576b0f03c67f78ea9&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN";
        HttpClientUtils.doGet(url,null);
    }

    @Test
    public void products2(){
        TestUtil.test("activity.virtualfin.products","{\"activityCode\":\"ACTIVITY.2021818.EXPERIENCE\"}","teqEiiVrTyebtyBVrVhxcwzdSpkozRao");
    }

    @Test
    public void testEncode() throws UnsupportedEncodingException {
        String org = "{\"activityCode\":\"ACTIVITY.2021818.EXPERIENCE\"}";
        String decode = URLEncoder.encode(org, "UTF-8");
        String re = "%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D";
        assertEquals(decode,re);
    }


    /**s
     * 产品列表 - customer
     */
    @Test
    public void products(){
//        String url= host+"method=activity.virtualfin.products" +
//                "&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D" +
//                "&k=js6rFh3MYMnyE8IF9E" +
//                "&token=teqEiiVrTyebtyBVrVhxcwzdSpkozRao" +
//                "&noncestr=3c02d1f12e73472cb40e490aa549648e"
//                ;
        String url= host+"method=activity.virtualfin.products&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=40cec7b1b85259eae408882e13f45c71&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN&custtoken=jgpPntBhCacIQdiEWxXaGDGNMBgxjqlX";
        HttpClientUtils.doGet(url,null);
    }


    /** 首次登录这个活动 */
    @Test
    public void firstlogin(){
        String url=host+"method=activity.virtualfin.firstlogin&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=7fed583e935207773b16d925775e9625&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN";
        HttpClientUtils.doGet(url,null);
    }


    /** 我的兑换码 */
    @Test
    public void mycouponcenter(){
        String url=host+"method=base.coupon.mycouponcenter&params=%7B%22pageIndex%22%3A1%2C%22pageSize%22%3A500%2C%22status%22%3A2%7D&k=js6rFh3MYMnyE8IF9E&noncestr=a731650ca2ba7ba0b6db8c4626dd133a&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN";
        HttpClientUtils.doGet(url,null);
    }



    /**
     * 收益明细 （就是红包流水） - 也包括提现记录
     */
    @Test
    public void redenvelopeflow(){
        String url=host+"method=activity.virtualfin.redenvelopeflow&params=%7B%22pageIndex%22%3A1%2C%22pageSize%22%3A25%2C%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=a490e82e75c5b24f9c85020e3349e104&token=teqEiiVrTyebtyBVrVhxcwzdSpkozRao";
        HttpClientUtils.doGet(url,null);
    }

    /**
     * 首页
     */
    @Test
    public void expgoldoverview(){
        String url=host+"method=activity.virtualfin.expgoldoverview&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=417fe9d875704de8c0b745d1b54149f5&token=BbGaRUXONsarVZEfzbbQkYTLkygTofOk";
        HttpClientUtils.doGet(url,null);
    }


    /**
     * 体验金明细
     */
    @Test
    public void goldflow(){
        String url=host+"method=activity.virtualfin.goldflow&params=%7B%22pageIndex%22%3A1%2C%22pageSize%22%3A25%2C%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=bbde7bfbd57dad24e64f256a2188089b&token=BbGaRUXONsarVZEfzbbQkYTLkygTofOk";
        HttpClientUtils.doGet(url,null);
    }

    /**
     * 买产品
     */
    @Test
    public void buyproduct(){
        String url=host+"method=activity.virtualfin.buyproduct&params=%7B%22price%22%3A%221000%22%2C%22productCode%22%3A%22PRODUCT.818.2020.3DAYS%22%2C%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=b2bbe39321b96b6ba8bd9b40c183c879&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN&custtoken=MxrVLERvAYKTemwFTTpjFvYIdcqfzUBy";
        HttpClientUtils.doGet(url,null);
    }

    /**
     * 订单列表
     */
    @Test
    public void orders(){
        String url=host+"method=activity.virtualfin.orders&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=8986b350200d30803f213398a7f63bf6&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN";
        HttpClientUtils.doGet(url,null);
    }

    /**红包提现
     */
    @Test
    public void withdraw() {
        String url= host+"method=activity.virtualfin.withdraw&params=%7B%22quantity%22%3A2.88%2C%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=6d11795bde6b4f9d01dff2eeca00fc23&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN";
        HttpClientUtils.doGet(url,null);
    }

    /**
     * 跑马灯
     */
    @Test
    public void getlastamountflow() {
        //{"activityCode":"ACTIVITY.2021818.EXPERIENCE"}
        String url="http://127.0.0.1:8081/api/cooperate?method=activity.virtualfin.getlastamountflow" +
                "&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D" +
                "&k=js6rFh3MYMnyE8IF9E" +
                "&noncestr=fb9fce406558c79e29d8378acba8bc14" +
                "&token=hmLTAbevpgmltKukSQInHQUyecoAWtMO" +
                "&custtoken=JGrllIebeBXGEFLWoSMZFmrLQPxRskwl";
        System.out.println(HttpClientUtils.doGet(url, null));
    }

    /**
     * 刷新任务状态
     */
    @Test
    public void flushacttaskstatus() {
        //{"activityCode":"ACTIVITY.2021818.EXPERIENCE"}
        //德耿
        String url = host+ "method=activity.virtualfin.flushacttaskstatus&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=254a5c1650b2ebcd4bd5ad43198bc10e&token=BbGaRUXONsarVZEfzbbQkYTLkygTofOk&custtoken=yVBlHEVWvfwSKyMNbJXjTyxreqqFVOxt";
        //hc
//        String url = host+ "method=activity.virtualfin.flushacttaskstatus&params=%7B%22activityCode%22%3A%22ACTIVITY.2021818.EXPERIENCE%22%7D&k=js6rFh3MYMnyE8IF9E&noncestr=4c00abfd44c4125afbedcc6340eae2e8&token=tSWzCDYzUUsriBTCutuTvavNCFlXwEAN&custtoken=vGGBsmUbsyWpgdEIjcTlkmfDZLEhDWoF";
        String s = HttpClientUtils.doGet(url, null);
        System.out.println(s);
    }






}