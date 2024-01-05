package com.wlzq.activity.redenvelope.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @描述: 红包发送地址加解密工具类
 * @版权: Copyright (c) 2015 
 * @公司: 思迪科技 
 * @作者: 程标
 * @版本: 1.0 
 * @创建日期: 2018年1月20日
 * @创建时间: 上午11:22:15
 */
public class RedPactetHelper
{
    /**
     * 
     * 描述：红包参数加密加签，输出可直接调用地址
     * @author 程标
     * @created 2018年1月20日 上午11:24:35
     * @since 
     * @param url 红包地址
     * @param params（参数：包括mch_no 商户英文简称，scene_no 红包场景值，scene_key 红包流水号，user_id 用户ID，amount 红包金额，descript 红包描述信息）
     * @param secret(秘钥)
     * @param version(版本号  默认v1.0)
     * @return url拼装后地址
     */
    public static String assemUrl(String url, Map<String, String> params, String secret, String version)
    {
        try
        {
            //参数加密
            String encryptedParam = SignHelper.encrypt(secret, JSON.toJSONString(params));
            //创建时间戳
            String timestamp = create_timestamp();
            //加签
            String signature = SignHelper.sign(secret, encryptedParam, timestamp);
            //结果拼接
            StringBuilder builder = new StringBuilder(url);
            builder.append("?params=");
            //拼接加密参数
            builder.append(encryptedParam);
            //拼接商户
            builder.append(URLEncoder.encode("&", "utf-8"));
            builder.append("mch_no=").append(params.get("mch_no"));
            //拼接红包场景
            builder.append(URLEncoder.encode("&", "utf-8"));
            builder.append("scene_no=").append(params.get("scene_no"));
            //拼接时间戳
            builder.append(URLEncoder.encode("&", "utf-8"));
            builder.append("timestamp=").append(timestamp);
            //凭借加签结果
            builder.append(URLEncoder.encode("&", "utf-8"));
            builder.append("signature=").append(signature);
            //拼接版本号
            if ( version != null && !"".equals(version) )
            {
                builder.append(URLEncoder.encode("&", "utf-8"));
                builder.append("version=").append(version);
            }
            else
            {
                builder.append(URLEncoder.encode("&", "utf-8"));
                builder.append("version=").append("V1.0.0");
            }
            return builder.toString();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
        
    }
    
    /**
     * 
     * 描述：解密并校验
     * @author 程标
     * @created 2018年1月20日 下午1:28:44
     * @since 
     * @param param
     * @param secret
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> checkSign(Map<String, String> param, String secret)
    {
        Map<String, String> map = new HashMap<String, String>();
        String encryptedParam = param.get("params");
        String signature = param.get("signature");
        String timestamp = param.get("timestamp");
        String signaturecheck = SignHelper.sign(secret, encryptedParam, timestamp);
        if ( signature.equals(signaturecheck) )
        {
            map.put("check", "true");
            String decryptData = SignHelper.decrypt(secret, encryptedParam);
            if ( decryptData == null || "".equals(decryptData) )
            {
                map.put("error_no", "-4");
                map.put("error_info", "校验失败，请求包体不合法");
            }
            else
            {
                map = JSON.parseObject(decryptData, HashMap.class);
                map.put("error_no", "0");
                map.put("error_info", "解密成功");
            }
            
        }
        else
        {
            map.put("error_no", "-5");
            map.put("error_info", "校验失败，请求包体被篡改！");
        }
        return map;
    }
    
    /**
     * 
     * 描述：回调结果进行解密
     * @author 程标
     * @created 2018年1月20日 下午9:53:29
     * @since 
     * @param secret 加密秘钥
     * @param result
     * @return
     */
    public static final Map<String,String> decrypt(String secret, String result)
    {
        //解密结果
        String jsonStr=SignHelper.decrypt(secret, result);
        //结果解析
        return JSON.parseObject(jsonStr, HashMap.class);
    }
    
    /**
     * 
     * 描述：创建当前日期时间戳
     * @author 程标
     * @created 2018年1月20日 上午11:40:54
     * @since 
     * @return 当前日期时间戳
     */
    private static String create_timestamp()
    {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
    
/*    public static void main(String[] args)
    {
        try
        {
            *//*************************加密实例**************************//*
            //红包发送地址
            String url = "http://htzq.xue998.cn/servlet/htzqOAuth?redirect_url=http%3a%2f%2fhtzq.xue998.cn/servlet/htzqAct";
            //参数拼接
            Map<String, String> params = new HashMap<String, String>();
            //商户英文简称
            params.put("mch_no", "haitongsec");
            //红包场景值
            params.put("scene_no", "00001");
            //红包流水号
            params.put("scene_key", "ouE9Vt5k-bHjtl7hc0WI3vc2hhac");
            //用户ID
            params.put("user_id", "ouE9Vt5k-bHjtl7hc0WI3vc2hhac");
            //红包金额
            params.put("amount", "0.01");
            //加密秘钥
            String secret = "d7167ac302943cee053048fd63c7a563";
            //调用加密，输出能直接调用的带参数红包地址
            System.out.println(RedPactetHelper.assemUrl(url, params, secret, null));
            
            *//*******************返回结果进行解密***************************//*
            String result1="C28EA68CBE263335EDCB8938EBD704D6EBC466D169DB95354C1F085C8BD3B8C8598097D03885EB52611D02EFCA1D889B1AD0C69D057244788A7946B0E8928B521A0CEB44B080607264B9EA1152DCD6C5707092B0EF5C5A58A03113925E0E0927F9A78507DAB9ED8587F338E2A93430E922B8E882DD71AFBCCC32AA07399FC29A9A968FD70AA288B130DEF826BB33C01CBE4FE49B2C615DBC26F6D468B5609F8E40E1849F0743A92C9C9C7CDD4569E363580CD2ED6EA1EAC6DBC7479C296DF6FC";
            String secret1="ae3d93f6c077fdc3a69e3eb5e377a438e716213f";
            Map<String,String> resultMap=RedPactetHelper.decrypt(secret1, result1);
            String amount=resultMap.get("amount");
            String result=resultMap.get("result");
            String scene_key=resultMap.get("scene_key");
            String user_id=resultMap.get("user_id");
            String send_flag=resultMap.get("send_flag");
            String payment_time=resultMap.get("payment_time");
            System.out.println(resultMap.toString());
            
            
            
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
}
