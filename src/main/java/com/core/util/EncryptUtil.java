package com.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Created by sunpeng
 */
public class EncryptUtil {

    private final static Log logger = LogFactory.getLog(EncryptUtil.class);

    private static String password = "ABCDEF";

    private static String ENCRYPT_TYPE_AES = "AES";

    /**
     * @param s
     * @return
     */
    public final static String md5(String s) {

        try {
            byte[] strTemp = s.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            return binaryToHex(mdTemp.digest());
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * @param content
     * @return byte[]
     * @throws Exception
     */
    private final static byte[] aes(String content, int mode) throws Exception {

        byte[] target = content.getBytes("UTF-8");

        if (mode == Cipher.DECRYPT_MODE) {
            target = hexToBinary(content);
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPT_TYPE_AES);
        keyGenerator.init(128, new SecureRandom(password.getBytes()));

        SecretKey secretKey = keyGenerator.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, ENCRYPT_TYPE_AES);

        Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE_AES);

        cipher.init(mode, secretKeySpec);

        return cipher.doFinal(target);
    }

    /**
     * @param content
     * @return
     * @throws Exception
     */
    public final static byte[] encrypt(String content) throws Exception {
        return aes(content, Cipher.ENCRYPT_MODE);
    }

    /**
     * @param content
     * @return
     * @throws Exception
     */
    public final static String getEncryptHex(String content) throws Exception {

        return binaryToHex(encrypt(content));
    }

    /**
     * @param content
     * @return
     * @throws Exception
     */
    public final static byte[] decrypt(String content) throws Exception {
        return aes(content, Cipher.DECRYPT_MODE);
    }

    public final static String getDecryptStr(String content) throws Exception {
        return new String((decrypt(content)));
    }

    /**
     * @param buf
     * @return
     */
    public final static String binaryToHex(byte buf[]) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * @param hexStr
     * @return
     */
    public final static byte[] hexToBinary(String hexStr) {

        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /*public static void main(String[] args) {
        System.out.println(EncryptUtil.md5("123456"));
    }*/
}
