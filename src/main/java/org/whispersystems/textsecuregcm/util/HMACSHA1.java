package org.whispersystems.textsecuregcm.util;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class HMACSHA1 {
    private static final String MAC_NAME = "HmacSHA1";    
    private static final String ENCODING = "UTF-8";  
    private static String ByteToHexString(byte[] bytearray)
    {
        StringBuilder stringBuilder = new StringBuilder("");  
        if (bytearray == null || bytearray.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < bytearray.length; i++) {  
            int v = bytearray[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString(); 
    } 
    public static String HmacSHA1Encrypt(String encryptText, String encryptKey)     
    {           
        try {
            byte[] data=encryptKey.getBytes(ENCODING);  
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称  
            SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);   
            //生成一个指定 Mac 算法 的 Mac 对象  
            Mac mac = Mac.getInstance(MAC_NAME);   
            //用给定密钥初始化 Mac 对象  
            mac.init(secretKey);    
              
            byte[] text = encryptText.getBytes(ENCODING);    
            //完成 Mac 操作      

            return ByteToHexString(mac.doFinal(text));
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return null;
    }
    
}
