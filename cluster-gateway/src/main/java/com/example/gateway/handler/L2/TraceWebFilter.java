package com.example.gateway.handler.L2;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * <h2>L2 WebFilter 层：所有请求的第一道</h2>
 * <p>
 *     `WebFilter` 是 WebFlux 版的 `javax.servlet.Filter`，作用于整个 `WebHandler` 链，
 *     先于路由匹配。CORS、traceId、访问日志和 Spring Security 的 `SecurityWebFilterChain` 都在这一层。
 * </p>
 * <p>
 *     每个请求来的时候，都生成一个 traceId，并打印请求方法、状态码和耗时
 * </p>
 *
 * @author xujiajun
 * @since 2026/11/14
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceWebFilter implements WebFilter {

    /**
     * 过滤器逻辑
     *
     * @param exchange 请求上下文
     * @param chain 过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 生成本次请求链路 traceId
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        // 取原始请求, 构建新的不可变 request
        ServerHttpRequest req = exchange.getRequest()
                // 拿到一个可修改的 Builder
                .mutate()
                // 注入traceId到请求头
                .header("X-Trace-Id", traceId)
                .build();
        // 写入日志上下文（日志框架才能打出来）
        MDC.put("traceId", traceId);
        // 记录起点用于计时
        long start = System.currentTimeMillis();
        // 放行到下一层，请求头已带 traceId
        return chain.filter(exchange.mutate().request(req).build())
                // doFinally 在整条链结束后执行（响应完成/取消/异常都会触发）
                .doFinally(sig -> {
                    // 计算耗时
                    long cost = System.currentTimeMillis() - start;
                    // 打印HTTP 方法, 响应状态码, 耗时
                    log.info("traceId:{} method:{} -> status:{} gatewayCost:{}ms", traceId,
                            exchange.getRequest().getMethod(),
                            exchange.getResponse().getStatusCode(),
                            cost);
                    // 清理 ThreadLocal，防止线程复用污染// 打印耗时
                    MDC.remove("traceId");
                });
    }
}