package com.example.core.util;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@Slf4j
public class TokenGeneratorUtil {

    // base62 字符表
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int LENGTH = 16;

    /**
     * 生成16位Base62安全随机串，用于 remember-me 的 series / token
     *
     * @return 16位随机字符串
     */
    public static String generate16BitToken() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(BASE62_CHARS.length());
            sb.append(BASE62_CHARS.charAt(index));
        }
        return sb.toString();
    }

    public static String generateSeries() {
        return generate16BitToken();
    }

    public static String generateToken(String series) {
        return generateToken(series, null);
    }

    public static String generateToken(String series, String secret) {
        if (secret == null || secret.isEmpty()) {
            return AuthUtils.aesEncryptByDefault(series);
        }
        return AuthUtils.aesEncrypt(series, secret);
    }

    public static boolean validateToken(String series, String token) {
        return validateToken(series, token, null);
    }

    public static boolean validateToken(String series, String token, String secret) {
        if (token == null || series == null) {
            return false;
        }
        return token.equals(generateToken(series, secret));
    }

    public static boolean validateCookieValue(String cookieValue) {
        return validateCookieValue(cookieValue, null);
    }

    public static boolean validateCookieValue(String cookieValue, String secret) {
        if (StringUtils.isEmpty(cookieValue)) {
            return false;
        }
        String[] parts = decodeCookieValue(cookieValue);
        if (parts == null) {
            return false;
        }
        String series = parts[0];
        String token = parts[1];
        return validateToken(series, token, secret);
    }

    public static String encodeCookieValue(String series, String token) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString((series + ":" + token).getBytes());
    }

    public static @Nullable String[] decodeCookieValue(String cookieValue) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            String value = new String(decoder.decode(cookieValue));
            String[] parts = value.split(":");
            if (parts.length != 2) {
                return null;
            }
            return parts;
        } catch (Exception e) {
            log.error("decodeCookieValue error", e);
        }
        return null;
    }

    // 测试
    public static void main(String[] args) {
        String series = generate16BitToken();
        String token  = generate16BitToken();
        System.out.println("series = " + series);
        System.out.println("token  = " + token);
    }
}