package com.example.consumer.handler;

import com.example.core.util.LoginUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 *
 * @author xujiajun
 * @since 2026/11/3
 */
public class ClusterFeignIntercetor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            String headerCookies = LoginUtils.getHeaderCookies();
            String auth = LoginUtils.getAuth();
            template.header("Cookie", headerCookies);
            template.header("auth", auth);
            String traceId = MDC.get("traceId");
            if (StringUtils.isNotBlank(traceId)) {
                template.header("X-Trace-Id", traceId);
            }
        }
    }