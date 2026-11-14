package com.example.gateway.handler.L4;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

/**
 * 请求体修改工厂类
 *
 * @author xujiajun
 * @since 2026/11/14
 */
@Component
public class RequestBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestBodyGatewayFilterFactory.Config> {

    public RequestBodyGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public String name() {
        return "RequestBody";
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 原请求
            ServerHttpRequest req = exchange.getRequest();
            // 把 Flux<DataBuffer> 聚合成单个 DataBuffer（会先把整个 body 读进内存）
            return DataBufferUtils.join(req.getBody())
                    // body 为空时给个空 buffer，避免 NPE
                    .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                    .flatMap(buf -> {
                        // 按可读长度分配
                        byte[] bytes = new byte[buf.readableByteCount()];
                        // 拷贝出字节
                        buf.read(bytes);
                        // 必须释放，DataBuffer 是池化的，不释放会内存泄漏
                        DataBufferUtils.release(buf);
                        // 还原成字符串
                        String text = new String(bytes, StandardCharsets.UTF_8);
                        // 示例：篡改报文,回写字节
                        byte[] newBody = text.replace("\"debug\":true", "\"debug\":false")
                                .getBytes(StandardCharsets.UTF_8);
                        // 用装饰器包住原请求
                        ServerHttpRequestDecorator dec = new ServerHttpRequestDecorator(req) {

                            /**
                             * 重写 getBody，下游读到的就是新 body
                             * @return 新 body
                             */
                            @Override
                            public Flux<DataBuffer> getBody() {
                                // 用响应的 bufferFactory 分配，保证同一 pool 然后包装新字节
                                return Flux.just(exchange.getResponse()
                                        .bufferFactory().wrap(newBody));
                            }

                            /**
                             * 重写 getHeaders，下游读到的长度必须和 body 一致
                             * @return 新头
                             */
                            @Override
                            public HttpHeaders getHeaders() {
                                // 新建一份
                                HttpHeaders h = new HttpHeaders();
                                // 复制原头
                                h.putAll(super.getHeaders());
                                // 去掉 chunked，否则和 Content-Length 冲突
                                h.remove(HttpHeaders.TRANSFER_ENCODING);
                                // 设置正确的长度
                                h.setContentLength(newBody.length);
                                return h;
                            }
                        };
                        // 用装饰后的请求继续走链
                        return chain.filter(exchange.mutate().request(dec).build());
                    });
        };
    }

    @Setter
    @Getter
    public class Config {

    }
}
