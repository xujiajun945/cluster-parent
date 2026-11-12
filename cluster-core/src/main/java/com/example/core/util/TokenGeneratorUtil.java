package com.example.core.util;

import java.security.SecureRandom;

/**
 *
 * @author xujiajun
 * @since 2026/11/10
 */
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
        return AuthUtils.aesEncryptByDefault(series);
    }

    public static boolean validateToken(String series, String token) {
        if (token == null || series == null) {
            return false;
        }
        return token.equals(generateToken(series));
    }

    // 测试
    public static void main(String[] args) {
        String series = generate16BitToken();
        String token  = generate16BitToken();
        System.out.println("series = " + series);
        System.out.println("token  = " + token);
    }
}