package com.example.gateway.handler.L6;

import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * <h2>L6 异常处理层：统一错误返回</h2>
 * WebFlux 全局异常处理器
 * 处理所有未被捕获的异常，返回统一的错误格式
 * `WebFilter` / `GlobalFilter` 抛出的异常不会自动变成友好响应，需要 `ErrorWebExceptionHandler`。
 * 要点：路由不命中时框架抛 `ResponseStatusException(NOT_FOUND)`，如果你也想返回统一 JSON，就要覆盖它，否则是默认的 404 空体。
 * Order=-1: 必须小于 DefaultErrorWebExceptionHandler（是 -1 的同级比较，靠 Bean 顺序），确保优先接管
 *
 * @author xujiajun
 * @since 2026/11/14
 */
@Component
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    /**
     * 任意环节抛出的异常都汇到这里
     *
     * @param exchange 请求上下文
     * @param ex 异常对象
     * @return 返回值
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 取响应
        ServerHttpResponse resp = exchange.getResponse();
        // 响应已经开始写出（例如流式响应中途炸） 只能向上抛，无法再改状态码
        if (resp.isCommitted()) {
            return Mono.error(ex);
        }
        // 默认 500
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        // 默认文案
        String msg = "internal error";
        // 框架自身的状态异常（如 404 路由未命中） 沿用它的状态码 和 reason
        if (ex instanceof ResponseStatusException rse) {
            status = (HttpStatus) rse.getStatusCode();
            msg = rse.getReason();
        } else if (ex instanceof TimeoutException) {
            // 下游超时 504
            status = HttpStatus.GATEWAY_TIMEOUT;
            msg = "upstream timeout";
        }
        // 写入最终状态码
        resp.setStatusCode(status);
        // JSON 返回
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 拼错误体
        byte[] body = String.format("{\"code\":%d,\"msg\":\"%s\"}", status.value(), msg)
                .getBytes(StandardCharsets.UTF_8);
        // 写回并结束
        return resp.writeWith(Mono.just(resp.bufferFactory().wrap(body)));
    }
}