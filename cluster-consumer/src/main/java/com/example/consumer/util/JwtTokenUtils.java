package com.example.consumer.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 * 一个完整的jwt分三个部分：
 * 1. header（头）
 *      {
 *          "typ": "JWT",  // 声明类型：当前为jwt
 *          "alg": "HS256"  // 声明使用的加密算法: 当前为HMAC_SHA256
 *      }
 *      base64编码之后获得header
 * 2. payload（载荷）: 存放有效信息
 *      {
 *          "iss": "user-center",  // iss: jwt签发者
 *          "exp": "2026-10-21 14:56:00",  // exp: 签名失效时间
 *          "iat": "2026-10-21 12:56:00",  // iat: 签名签发时间
 *          // ... 其他有效信息
 *      }
 *      base64编码之后获得payload
 * 3. signature（签证）
 *      // secret: 加密密钥
 *      signature = HMAC_SHA256(base64(header) + "." + base64(payload), secret)
 *      JWTToken = base64(header) + "." + base64(payload) + "." + signature
 *
 * @author xujiajun
 * @since 2026/10/21
 */
public class JwtTokenUtils {

    private static final String ISSUER = "cluster-user-center";

    private static final String SECRET = "q3t6w9z$C&F)J@NcQfTjWnZr4u7x";

    private static final Long EXP_TIME = 1000L * 60 * 60 * 2;

    private JwtTokenUtils() {
        throw new RuntimeException("util class can not be init!");
    }

    /**
     * 生成令牌
     *
     * @param content 自定义内容
     * @return JWTToken
     */
    public static String createToken(String content) {
        ZonedDateTime now = ZonedDateTime.now();
        Date nowDate = Date.from(Instant.from(now));

        ZonedDateTime expTime = now.plus(2000, TimeUnit.MILLISECONDS.toChronoUnit());
        Date expDate = Date.from(Instant.from(expTime));
        return JWT.create()
                .withIssuedAt(nowDate)
                .withExpiresAt(expDate)
                .withSubject(content)
                .withIssuer(ISSUER)
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 校验令牌
     *
     * @param token JWTToken
     * @return 自定义内容
     * @throws com.auth0.jwt.exceptions.TokenExpiredException 校验失败抛出异常（RunTimeException）
     */
    public static String verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getSubject();
    }

    /**
     * jwt令牌header头默认带有如下信息：
     *  {
     *      "typ": "JWT",
     *      "alg": "HS256"  // 如果使用其他加密算法，会自动生成对应算法名称
     *  }
     *  value可以被覆写
     *  可以根据业务需要添加自定义header元素
     */
    private static Map<String, Object> defaultHeaderMap() {
        Map<String, Object> defaultHeaderMap = new HashMap<>();
        defaultHeaderMap.put("typ", "JWT");
        defaultHeaderMap.put("alg", "HS256");
        return defaultHeaderMap;
    }

    public static void main(String[] args) {
        String content = "{\"userId\": 1, \"username\": \"xujiajun\"}";
        String token = createToken(content);
        System.out.println(token);

        String[] split = token.split("\\.");
        String base64Header = split[0];
        String base64Payload = split[1];
        String signature = split[2];
        System.out.println(base64Header);
        System.out.println(base64Payload);
        System.out.println(signature);

        Base64.Decoder decoder = Base64.getDecoder();
        String header = new String(decoder.decode(base64Header));
        String payload = new String(decoder.decode(base64Payload));
        System.out.println(header);
        System.out.println(payload);

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String subject = verifyToken(token);
        System.out.println(subject);

//        String subject = verifyToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE3OTI2NTc2MTYsImV4cCI6MTc5MjY2NDgxNiwic3ViIjoie1wibmFtZVwiOlwi5b6Q5a625L-KXCIsXCJ1c2VySWRcIjoxfSIsImlzcyI6ImNsdXN0ZXItdXNlci1jZW50ZXIifQ.jVYZ-YTPbVAfLV1SSGnVGLx0tzHfSPABfIg6hSVTqUo");
//        System.out.println(subject);
    }
}
