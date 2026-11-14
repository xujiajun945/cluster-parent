package com.example.gateway.handler.L4;

import com.example.core.util.TaskUtils;
import io.valkey.springframework.data.valkey.core.ValkeyTemplate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * <h2>L4 自定义 GatewayFilterFactory（路由级拦截）</h2>
 * <p>
 *     当前示例：拦截请求，实现幂等性
 * </p>
 * 必须成为 Bean，才能被 RouteDefinitionRouteLocator 通过 name 找到
 * <p>
 *     GlobalFilter:         全局拦截器
 *     GatewayFilterFactory: 路由级拦截器
 * </p>
 * <p>
 *     AbstractGatewayFilterFactory: 路由级拦截器的模板父类, 处理配置绑定
 * </p>
 * @author xujiajun
 * @since 2026/11/14
 */
@Component
public class IdempotentGatewayFilterFactory extends AbstractGatewayFilterFactory<IdempotentGatewayFilterFactory.Config> {

    @Autowired
    private ValkeyTemplate<String, Object> valkeyTemplate;

    /**
     * 必须有构造器
     */
    public IdempotentGatewayFilterFactory() {
        // 把配置类交给父类做反射绑定
        super(Config.class);
    }

    /**
     * 必须与 yml 中配置的名称一致
     *
     * @return 过滤器名称
     */
    @Override
    public String name() {
        // 此例即 yml 写 - name: Idempotent
        return "Idempotent";
    }

    /**
     * 支持的简写参数
     * 配置后，在yaml中只需要写 Idempotent=30 （[name]=[value]）
     *
     * @return 简写对应的config里的参数名，只能有一个，且必须是第一个
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("ttlSeconds");
    }

    /**
     * 过滤器应用方法
     * 每个路由构建时调用一次，返回真正的 filter
     *
     * @param config 配置类
     * @return 过滤器
     */
    @Override
    public GatewayFilter apply(Config config) {
        // OrderedGatewayFilter: 对 GatewayFilter 进行包装以便自定义顺序
        return new OrderedGatewayFilter(
                // 这里是每个请求都会执行的 lambda
                (exchange, chain) -> {
                    // 幂等键
                    String key = buildKey(exchange);
                    // 构造 Redis key 使用分布式锁
                    String cacheKey = "idem:" + key;
                    Boolean locked = valkeyTemplate.opsForValue()
                            .setIfAbsent(cacheKey, "PROCESSING", Duration.ofSeconds(config.getTtlSeconds()));
                    // 拿到锁才可以放行
                    if (locked == null || !locked) {
                        // 重复请求 返回 409
                        exchange.getResponse().setStatusCode(HttpStatus.CONFLICT);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange)
                            .then(Mono.fromRunnable(TaskUtils.wrap(() -> {
                                HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                                int status = statusCode.value();
                                if (status >= 200 && status < 300) {
                                    // 成功 → 标记为已完成
                                    valkeyTemplate.opsForValue().set(key, "SUCCESS", config.getTtlSeconds());
                                } else {
                                    // 失败 → 释放，允许重试
                                    valkeyTemplate.delete(key);
                                }
                            }, exception -> valkeyTemplate.delete(cacheKey))));
                },
                // 顺序取自配置
                config.getOrder());
    }

    private String buildKey(ServerWebExchange exchange) {
        String clientKey = exchange.getRequest().getHeaders().getFirst("X-Idem-Key");
        // 客户端主动传
        if (clientKey != null) {
            return clientKey;
        }
        // 兜底：用「用户 + 方法 + 路径 + 关键参数」拼，别用全量 body（要读 body，成本高）
        return exchange.getAttribute("userId")
                + ":" + exchange.getRequest().getMethod()
                + ":" + exchange.getRequest().getPath();
//                + ":" + exchange.getRequest().getQueryParams().getFirst("orderNo");
    }

    /**
     * <h2>配置类</h2>
     * 配置类的字段必须与 yml 中的参数名一致
     */
    @Setter
    @Getter
    public static class Config {

        // 幂等窗口，默认 60 秒
        private long ttlSeconds = 60;

        // 过滤链顺序
        private int order = 0;
    }
}