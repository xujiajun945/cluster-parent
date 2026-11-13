package com.example.consumer.service.impl;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.annotation.NacosConfig;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.example.consumer.service.DemoNacosConfigService;
import com.example.core.model.DemoNacosConfig;
import com.example.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author xujiajun
 * @since 2026/10/17
 */
@Slf4j
@Service
public class DemoNacosConfigServiceImpl implements DemoNacosConfigService {

    @Autowired
    private NacosConfigManager configManager;

    @NacosConfig(dataId = "application-dev.yml", group = "cluster-consumer", key = "spring.data.valkey.lettuce.pool.max-active")
    private Integer maxActive;

    @Override
    public void publishConfig(DemoNacosConfig demoNacosConfig) throws NacosException {
        String dataId = demoNacosConfig.getDataId();
        String group = demoNacosConfig.getGroup();
        String value = demoNacosConfig.getValue();
        configManager.getConfigService().publishConfig(dataId, group, value, ConfigType.JSON.getType());
    }

    @Override
    public String getConfig(String dataId, String group) throws NacosException {
        String config = configManager.getConfigService().getConfig(dataId, group, 300);
        log.info("annotation get config: maxActive: {}, readConfig: {}", maxActive, config);
        return config;
    }

    @Override
    public void deleteConfig(String dataId, String group, String key) throws NacosException {
        String config = configManager.getConfigService().getConfig(dataId, group, 300);
        log.info("check origin config: {}", config);
        Map<String, String> nacosConfig = JsonUtils.parseMap(config, String.class, String.class);
        String value = nacosConfig.get(key);
        if (value != null) {
            nacosConfig.remove(key);
            String newConfig = JacksonUtils.toJson(nacosConfig);
            configManager.getConfigService().publishConfig(dataId, group, newConfig, ConfigType.JSON.getType());
            log.info("new config: {}, modified config success!", newConfig);
        } else {
            log.info("key not exists! delete operate failed!");
        }
    }
}
