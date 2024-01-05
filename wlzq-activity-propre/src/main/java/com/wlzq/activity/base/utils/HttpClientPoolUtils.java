package com.wlzq.activity.base.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;


/**
 * @author wolf<songj @ huizhuang.com>
 * @description httpclient version<4.5.2>工具类
 */
public class HttpClientPoolUtils {

    private static PoolingHttpClientConnectionManager cm;
    private static String EMPTYSTR = "";
    private static String UTF_8 = "UTF-8";
    private static SSLContextBuilder builder = null;
    private static SSLConnectionSocketFactory sslsf = null;

    protected static final Logger LOGGER = LoggerFactory.getLogger(HttpClientPoolUtils.class);
    private static final Logger REQ_LOGGER = LoggerFactory.getLogger("ReqLogger");

    private static void init() {
        try {
            builder = new SSLContextBuilder();
            // 全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslsf = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new PlainConnectionSocketFactory())
                    .register("https", sslsf)
                    .build();
            // httpclient连接数设置
            if (cm == null) {
                cm = new PoolingHttpClientConnectionManager(registry);
                //整个连接池最大连接数
                cm.setMaxTotal(50);
                //每路由最大连接数，默认值是2
                cm.setDefaultMaxPerRoute(5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        init();
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                //				.setStaleConnectionCheckEnabled(true)
                .build();
        return HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm).setDefaultRequestConfig(defaultRequestConfig).build();
    }

    /**
     * @param url String
     * @return String
     * @description 普通get请求
     */
    public static String httpGetRequest(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    /**
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     * @description 带参数get请求
     */
    public static String httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParamsToNvps(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

    /**
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     * @description 带参数|header头get请求
     */
    public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParamsToNvps(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet);
    }

    /**
     * @param url
     * @return
     * @description 普通post请求
     */
    public static String httpPostRequest(String url) {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }

    /**
     * @param url
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     * @description 带参数post请求
     */
    public static String httpPostRequest(String url, Map<String, Object> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = covertParamsToNvps(params);

        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        String result = getResult(httpPost);

        Object[] reqParams = new Object[]{url, JSON.toJSONString(params), result, DateUtil.dateFormat("yyyy-mm-dd HH:mm:ss")};
        REQ_LOGGER.debug("[REQUEST][ url={} ][reqparams={} ][response={} ] [time={}]", reqParams);

        return result;
    }

    /**
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     * @description 带参数|header头get请求
     */
    public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), (String) param.getValue());
        }

        ArrayList<NameValuePair> pairs = covertParamsToNvps(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return getResult(httpPost);
    }

    /**
     * 参数转换成 key-value 对
     *
     * @param params
     * @return
     */
    private static ArrayList<NameValuePair> covertParamsToNvps(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }

        return pairs;
    }


    /**
     * @param request
     * @return
     * @description 处理输出
     */
    private static String getResult(HttpRequestBase request) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            int responseCodeOk = 200;
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(1000)
                    .setSocketTimeout(3000000).setConnectTimeout(3000000).build();
            request.setConfig(requestConfig);
            CloseableHttpResponse response = httpClient.execute(request);
            LOGGER.info("请求结果:{}", JSONObject.toJSONString(response));
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (responseCode == responseCodeOk && entity != null) {
                String result = EntityUtils.toString(entity);
                response.close();
                LOGGER.info("请求输出处理：{}", result);
                return result;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return EMPTYSTR;
    }
}
