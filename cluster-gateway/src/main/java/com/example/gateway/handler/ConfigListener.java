package com.example.gateway.handler;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author xujiajun
 * @since 2026/11/13
 */
@Configuration
public class ConfigListener {

    @NacosConfigListener(dataId = "routeconfig", groupId = "DEFAULT_GROUP")
    public void rate(String rateConfig) {
        System.out.println("receiveRateConfig:"+rateConfig);
    }
}
