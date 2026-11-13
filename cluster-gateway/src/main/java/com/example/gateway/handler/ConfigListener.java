package com.example.gateway.handler;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.annotation.NacosConfigListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author xujiajun
 * @since 2026/11/13
 */
@Configuration
public class ConfigListener {

    @Autowired
    private NacosConfigManager configManager;

    @NacosConfigListener(dataId = "application-dev.yml", group = "cluster-gateway")
    public void rate(String rateConfig) {
        System.out.println("receiveRateConfig:"+rateConfig);
    }
}
