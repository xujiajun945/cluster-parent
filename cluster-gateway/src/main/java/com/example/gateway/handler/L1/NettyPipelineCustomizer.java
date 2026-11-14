package com.example.gateway.handler.L1;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.reactor.netty.NettyServerCustomizer;
import org.springframework.stereotype.Component;
import reactor.netty.http.server.HttpServer;

import java.net.SocketAddress;

/**
 * <h2>L1 Netty 层：连接级拦截</h2>
 * <p>
 * Spring Boot 提供的 Netty 定制扩展点
 * 自定义 Netty 服务端 pipeline，用于统计连接数
 * <p>
 * 运行位置: Reactor Netty pipeline
 * 典型用途: 统计连接数 协议级、连接级、TCP 层埋点
 * <p>
 * 这里的 handler 在 HTTP 解析之前执行，不能修改 `ServerWebExchange`，只能处理 `ByteBuf`。大多数业务不需要写到这一层。
 *
 * @author xujiajun
 * @since 2026/11/14
 */
@Slf4j
@Component
public class NettyPipelineCustomizer implements NettyServerCustomizer {


    /**
     * 自定义 Netty 服务端 pipeline
     *
     * @param server Reactor Netty 的 HttpServer 实例
     * @return 自定义的 HttpServer 实例
     */
    @Override
    public HttpServer apply(HttpServer server) {
        // 每建立一条 TCP 连接时回调
        return server.doOnConnection(conn ->
                // 在 pipeline 末尾追加 handler
                conn.addHandlerLast("connCounter", new ChannelInboundHandlerAdapter() {

                    /**
                     * 连接建立完成时回调
                     *
                     * @param ctx ChannelHandlerContext
                     * @throws Exception 可能的异常
                     */
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        // 拿到真实对端 IP:PORT
                        SocketAddress socketAddress = ctx.channel().remoteAddress();
                        log.info("new conn from {}", socketAddress);
                        // 交给后续 handler，不能吞掉事件
                        super.channelActive(ctx);
                    }

                }));
    }
}