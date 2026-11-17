package com.example.gateway.handler.L4;

import com.example.core.config.RememberMeConfig;
import com.example.core.dto.UserDTO;
import com.example.core.dto.UserRememberMeDTO;
import com.example.core.util.JsonUtils;
import com.example.core.util.JwtTokenUtils;
import com.example.core.util.TokenGeneratorUtil;
import com.example.gateway.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>L4 GlobalFilter / GatewayFilter 层：真正的 “拦截器”</h2>
 * 全局拦截器
 * 对所有路由进行拦截
 * GlobalFilter 只要求 filter 方法，Ordered 要求 getOrder
 *
 * @author xujiajun
 * @since 2026/11/14
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * todo xujiajun 白名单需要外置配置文件
     * 白名单路径
     */
    private static final List<String> WHITE = List
            .of("/auth/login", "/actuator/health", "/api/consumer/login/normal", "/api/consumer/login/jwt");

    @Autowired
    private RememberMeConfig rememberMeConfig;

    @Autowired
    private UserClient userClient;

    /**
     * 主拦截逻辑
     * 1. 校验jwt
     * 2. 校验spring session
     * 3. 校验remember me 并做刷新操作
     * 如果以上三种都通过则放行，否则返回错误
     *
     * @param exchange 请求上下文
     * @param chain 过滤器链
     * @return 响应结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 当前请求
        ServerHttpRequest req = exchange.getRequest();
        // 剥掉 host 后的路径，用于白名单匹配
        String path = req.getURI().getPath();
        // 命中白名单直接放行，不鉴权
        if (WHITE.contains(path)) {
            return chain.filter(exchange);
        }
        // 取 Authorization 头 校验jwt
        String auth = req.getHeaders().getFirst("auth");
        if (StringUtils.isNotBlank(auth)) {
            return validateJWT(exchange, chain, req, auth);
        }
        // 接入spring session 当前websession代理map session
        return exchange.getSession()
                .flatMap(session -> {
                    // 校验spring session
                    UserDTO user = JsonUtils.convertObject(session.getAttribute("user"), UserDTO.class);
                    if (user != null && user.getId() != null && user.getId() > 0) {
                        exchange.getAttributes().put("userId", user.getId());
                        return addUserIdAndChain(exchange, req, chain, user.getId().toString());
                    }
                    // 校验remember me
                    return validateRememberMe(exchange, chain, req, session);
                });
    }

    private Mono<Void> validateJWT(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest req, String jwt) {
        try {
            // 校验签名并解析载荷
            String subject = JwtTokenUtils.verifyToken(jwt);
            Map<String, Object> subjectInfo = JsonUtils.parseMap(subject, HashMap.class, String.class, Object.class);
            // exchange.getAttributes() 只在过滤器链内部可见，不会随转发带给下游微服务。要传给下游，必须写进 request header
            // 存进 attributes，供后续 filter 读取
            exchange.getAttributes().put("userId", subjectInfo.get("userId"));
            // 放行
            return addUserIdAndChain(exchange, req, chain, subjectInfo.get("userId").toString());
        } catch (Exception e) {
            // 发生异常（例如：过期/篡改/格式错误）统一 封装错误响应
            log.error("invalid token", e);
            return reject(exchange, "invalid token");
        }
    }

    private Mono<Void> validateRememberMe(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest req, WebSession session) {
        HttpCookie rememberMeCookie = req.getCookies().getFirst(rememberMeConfig.getCookieName());
        if (rememberMeCookie == null) {
            return reject(exchange, "未登录或登录失效！");
        }
        String rememberMeValue = rememberMeCookie.getValue();
        String[] parts = TokenGeneratorUtil.decodeCookieValue(rememberMeValue);
        if (parts == null) {
            return reject(exchange, "rememberMe token格式异常！");
        }
        String series = parts[0];
        String token = parts[1];
        return userClient.getUserRememberMeBySeries(series)
                .flatMap(userRememberMeDTO ->
                        setUserAndRefreshToken(exchange, req, session, chain, token, userRememberMeDTO, series))
                .onErrorResume(e -> {
                    log.error("获取用户 remember-me 失败, series={}", series, e);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }

    private Mono<Void> setUserAndRefreshToken(ServerWebExchange exchange, ServerHttpRequest req,
                                              WebSession session, GatewayFilterChain chain,
                                              String rememberMeTokenFromCookie,
                                              UserRememberMeDTO userRememberMeDTO, String series) {
        if (!rememberMeTokenFromCookie.equals(userRememberMeDTO.getToken())) {
            return reject(exchange, "rememberMe token校验失败！");
        }
        boolean result = TokenGeneratorUtil.validateCookieValue(rememberMeTokenFromCookie, userRememberMeDTO.getSecret());
        if (!result) {
            return reject(exchange, "rememberMe token解密失败！");
        }
        exchange.getAttributes().put("userId", userRememberMeDTO.getUserId());
        // 需要查询userDTO，创建session并重新下发会话cookie
        return userClient.getUserById(userRememberMeDTO.getUserId())
                // 校验原remember me通过后，需要更新remember me token
                .flatMap(userDTO -> refreshRememberMe(exchange, req, session, chain, userDTO, series))
                .onErrorResume(e -> {
                    log.error("获取用户信息失败, userId={}", userRememberMeDTO.getUserId(), e);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }

    private Mono<Void> refreshRememberMe(ServerWebExchange exchange, ServerHttpRequest req,
                                         WebSession session, GatewayFilterChain chain, UserDTO userDTO, String series) {
        // 1. 写 session（副作用，用 doOnNext 风格的 flatMap 内完成）
        return Mono.fromRunnable(() -> session.getAttributes().put("user", userDTO))
                // 2. 刷新 remember-me token
                .then(userClient.refreshUserRememberMe(series))
                // 3. 拿到新 token 后处理 cookie
                .flatMap(newToken -> {
                    String cookieValue = TokenGeneratorUtil.encodeCookieValue(series, newToken);
                    // 改请求头上的remember me cookie为最新值 + 写响应 cookie（副作用）
                    setRememberMeCookie(exchange, cookieValue);
                    return replaceCookieHeader(exchange, chain, req, cookieValue, userDTO.getId().toString());
                })
                .onErrorResume(e -> {
                    log.error("刷新 remember-me 失败, series={}", series, e);
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return exchange.getResponse().setComplete();
                });
    }

    private void setRememberMeCookie(ServerWebExchange exchange, String newValue) {
        // 新建一个 HttpCookie，复制旧值，覆盖要改的字段
        HttpCookie newCookie = ResponseCookie.from(rememberMeConfig.getCookieName(), newValue)
                .path(rememberMeConfig.getPath())
                .httpOnly(rememberMeConfig.isHttpOnly())
                .secure(rememberMeConfig.isSecure())
                .maxAge(rememberMeConfig.getMaxAge())
                .domain(rememberMeConfig.getDomain())
                .build();
        exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, newCookie.toString());
    }

    private Mono<Void> replaceCookieHeader(ServerWebExchange exchange, GatewayFilterChain chain,
                                           ServerHttpRequest request, String newValue, String userId) {
        String cookieString = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookieString == null || cookieString.isEmpty()) {
            return chain.filter(exchange);
        }
        String newCookieString = replaceRememberMeHeaderValue(rememberMeConfig, cookieString, newValue);
        // webFlux请求头只读，需要mutate替换
        ServerHttpRequest newRequest = request.mutate().header(HttpHeaders.COOKIE, newCookieString).build();
        // 构造新的 exchange，继续走过滤器链
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return addUserIdAndChain(newExchange, newRequest, chain, userId);
    }

    private String replaceRememberMeHeaderValue(RememberMeConfig config, String cookieString, String newValue) {
        String[] cookies = cookieString.split(";");
        StringBuilder newHeaderBuilder = new StringBuilder();
        for (String cookie : cookies) {
            String[] cookiePair = cookie.trim().split("=");
            if (cookiePair.length == 2) {
                String cookieName = cookiePair[0];
                String cookieValue = cookiePair[1];
                if (config.getCookieName().equals(cookieName)) {
                    cookieValue = newValue;
                }
                newHeaderBuilder.append(cookieName).append("=").append(cookieValue).append(";");
            }
        }
        int lastIndexOf = newHeaderBuilder.lastIndexOf(";");
        newHeaderBuilder.deleteCharAt(lastIndexOf);
        return newHeaderBuilder.toString();
    }

    /**
     * 添加用户ID到请求头并放行
     */
    private Mono<Void> addUserIdAndChain(ServerWebExchange exchange, ServerHttpRequest req, GatewayFilterChain chain, String userId) {
        ServerWebExchange build = exchange.mutate().request(req.mutate().header("X-User-Id", userId).build()).build();
        return chain.filter(build);
    }

    /**
     * 封装错误响应
     *
     * @param exchange 请求上下文
     * @param msg 错误信息
     * @return 响应结果
     */
    private Mono<Void> reject(ServerWebExchange exchange, String msg) {
        // 取响应对象
        ServerHttpResponse resp = exchange.getResponse();
        // 置 401
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 声明 JSON
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 构造错误体
        byte[] body = ("{\"code\":401,\"msg\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        // 用分配器包装成 DataBuffer
        DataBuffer buf = resp.bufferFactory().wrap(body);
        // 写入响应并结束，Mono 完成即请求结束
        return resp.writeWith(Mono.just(buf));
    }

    @Override
    public int getOrder() {
        // 负数保证排在路由 filter 之前（路由 filter 从 1 开始编号）
        return -100;
    }
}