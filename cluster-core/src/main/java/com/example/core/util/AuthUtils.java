package com.example.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@Slf4j
public class AuthUtils {

    /**
     * 密匙，必须16位
     */
    private static final String DEFAULT_KEY = "f4k9f5w7f8g4er26";

    /**
     * 偏移量
     */
    private static final String OFFSET = "5e8y6w45ju8w9jq8";

    /**
     * 编码
     */
    private static final String ENCODING = "UTF-8";

    /**
     * 算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 默认的加密算法，CBC模式
     */
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private AuthUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String aesEncryptByDefault(String data) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(DEFAULT_KEY.getBytes(StandardCharsets.US_ASCII), ALGORITHM);
        // CBC模式偏移量IV
        IvParameterSpec iv = new IvParameterSpec(OFFSET.getBytes());
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encrypted = cipher.doFinal(data.getBytes(ENCODING));
            // 加密后再使用BASE64做转码
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("加密失败!", e);
        }
        return StringUtils.EMPTY;
    }

    public static String aesDecryptByDefault(String data) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(DEFAULT_KEY.getBytes(StandardCharsets.US_ASCII), ALGORITHM);
        // CBC模式偏移量IV
        IvParameterSpec iv = new IvParameterSpec(OFFSET.getBytes());
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
            // 先用base64解码
            byte[] buffer = Base64.getDecoder().decode(data);
            byte[] encrypted = cipher.doFinal(buffer);
            return new String(encrypted, ENCODING);
        } catch (Exception e) {
            log.error("解密失败!", e);
        }
        return StringUtils.EMPTY;
    }

    public static String aesEncrypt(String data, String key) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), ALGORITHM);
        // CBC模式偏移量IV
        IvParameterSpec iv = new IvParameterSpec(OFFSET.getBytes());
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encrypted = cipher.doFinal(data.getBytes(ENCODING));
            // 加密后再使用BASE64做转码
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("加密失败!", e);
        }
        return StringUtils.EMPTY;
    }

    public static String aesDecrypt(String data, String key) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), ALGORITHM);
        // CBC模式偏移量IV
        IvParameterSpec iv = new IvParameterSpec(OFFSET.getBytes());
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
            // 先用base64解码
            byte[] buffer = Base64.getDecoder().decode(data);
            byte[] encrypted = cipher.doFinal(buffer);
            return new String(encrypted, ENCODING);
        } catch (Exception e) {
            log.error("解密失败!", e);
        }
        return StringUtils.EMPTY;
    }

}
