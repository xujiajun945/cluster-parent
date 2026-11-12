package com.example.consumer.handler;

import com.example.core.util.LoginUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 *
 * @author xujiajun
 * @since 2026/11/3
 */
public class ClusterFeignIntercetor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            String headerCookies = LoginUtils.getHeaderCookies();
            String auth = LoginUtils.getContext().getAuth();
            template.header("Cookie", headerCookies);
            template.header("auth", auth);
        }
    }