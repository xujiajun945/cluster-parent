package com.example.core.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * 全局traceId过滤器
 * 拦截所有请求，设置traceId，方便日志追踪
 *
 * @author xujiajun
 * @since 2026/11/14
 */
@Slf4j
public class LogFilter extends HttpFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final String MDC_TRACE_KEY = "traceId";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 跳过不需要追踪的路径，按需扩展
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/static/")) {
            chain.doFilter(request, response);
            return;
        }
        // 获取traceId
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (ObjectUtils.isEmpty(traceId)) {
            // 自己生成traceId
            log.warn("没有透传traceId，自己生成traceId...");
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        log.info("全局traceId: {}", traceId);
        // 放入MDC
        MDC.put(MDC_TRACE_KEY, traceId);
        // 继续透传traceId
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            // 清理资源
            MDC.remove(MDC_TRACE_KEY);
        }
    }

}
