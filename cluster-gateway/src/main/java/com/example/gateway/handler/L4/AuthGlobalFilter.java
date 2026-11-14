package com.example.gateway.handler.L4;

import com.example.core.util.JsonUtils;
import com.example.core.util.JwtTokenUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
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
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 白名单路径
     */
    private static final List<String> WHITE = List.of("/auth/login", "/actuator/health");

    /**
     * 主拦截逻辑
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
        // 取 Authorization 头
        String token = req.getHeaders().getFirst("Authorization");
        // 没带或格式不对直接返回 401，不调用 chain（链在此中断）
        if (token == null || !token.startsWith("Bearer ")) {
            return reject(exchange, "missing token");
        }
        try {
            // 校验签名并解析载荷
            String subject = JwtTokenUtils.verifyToken(token);
            Map<String, Object> subjectInfo = JsonUtils.parseMap(subject, HashMap.class, String.class, Object.class);
            // exchange.getAttributes() 只在过滤器链内部可见，不会随转发带给下游微服务。要传给下游，必须写进 request header
            // 存进 attributes，供后续 filter 读取
            exchange.getAttributes().put("userId", subjectInfo.get("userId"));
            // 放行
            return chain.filter(exchange);
        } catch (Exception e) {
            // 发生异常（例如：过期/篡改/格式错误）统一 封装错误响应
            return reject(exchange, "invalid token");
        }
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