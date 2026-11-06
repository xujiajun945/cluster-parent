package com.example.consumer.handler;

import com.example.consumer.util.LoginUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author xujiajun
 * @since 2026/11/3
 */
public class ClusterFeignIntercetor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            HttpServletRequest httpServletRequest = LoginUtils.getRequest();
            String cookie = httpServletRequest.getHeader("Cookie");
            String auth = httpServletRequest.getHeader("auth");
            template.header("Cookie", cookie);
            template.header("auth", auth);
        }
    }