package com.example.consumer.service.impl;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.example.consumer.service.DemoNacosConfigService;
import com.example.core.model.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author xujiajun
 * @since 2026/10/17
 */
@Slf4j
@Service
public class DemoNacosConfigServiceImpl implements DemoNacosConfigService {

    @NacosInjected
    private ConfigService configService;

    @com.alibaba.cloud.nacos.annotation.NacosConfig(dataId = "demo", group = "default", key = "name")
    private String defaultConfig;

    @Override
    public void publishConfig(NacosConfig nacosConfig) throws NacosException {
        String dataId = nacosConfig.getDataId();
        String group = nacosConfig.getGroup();
        String value = nacosConfig.getValue();
        configService.publishConfig(dataId, group, value, ConfigType.JSON.getType());
    }

    @Override
    public String getConfig(String dataId, String group) throws NacosException {
        String config = configService.getConfig(dataId, group, 300);
        log.info("annotation get config: {}", defaultConfig);
        return config;
    }
}
