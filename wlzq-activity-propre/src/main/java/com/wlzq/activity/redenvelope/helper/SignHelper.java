package com.wlzq.activity.redenvelope.helper;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class SignHelper
{
    
    
    /************************生成Sign值的方法域结束******************************/
    
    //加密
    public static final String encrypt(String key, String password)
    {
        try
        {
            return byte2hex(encrypt(password.getBytes("UTF-8"), key.getBytes("UTF-8")));
        }
        catch (Exception localException)
        {
            System.out.println(localException.getMessage());
        }
        return null;
    }
    
    //加密
    public static byte[] encrypt(byte[] src, byte[] key) throws Exception
    {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(1, securekey, sr);
        return cipher.doFinal(src);
    }
    
    public static String byte2hex(byte[] b)
    {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if ( stmp.length() == 1 )
            {
                hs = hs + "0" + stmp;
            }
            else
            {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
    
    //解密
    public static final String decrypt(String key, String data)
    {
        try
        {
            return new String(decrypt(hex2byte(data.getBytes("UTF-8")), key.getBytes("UTF-8")),"UTF-8");
        }
        catch (Exception localException)
        {
        }
        return null;
    }
    
    public static byte[] hex2byte(byte[] b)
    {
        if ( b.length % 2 != 0 )
        {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2)
        {
            String item = new String(b, n, 2);
            b2[(n / 2)] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
    
    //解密
    public static byte[] decrypt(byte[] src, byte[] key) throws Exception
    {
        SecureRandom sr = new SecureRandom();
        
        DESKeySpec dks = new DESKeySpec(key);
        
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        
        Cipher cipher = Cipher.getInstance("DES");
        
        cipher.init(2, securekey, sr);
        
        return cipher.doFinal(src);
    }
    
    public static String sign(String secret,String nonce_str,String timestamp)
    {
        Map<String,String> sign = new HashMap<String,String>();
        String string1;
        String signature = "";
        
        //注意这里参数名必须全部小写，且必须有序
        string1 = "secrett=" + secret + "&noncestr=" + nonce_str + "&timestamp=" + timestamp;
        
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        
        return signature;
    }
    
    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    
    
}
