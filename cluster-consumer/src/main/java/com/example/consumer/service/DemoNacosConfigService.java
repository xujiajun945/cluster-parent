package com.example.consumer.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.core.model.DemoNacosConfig;

/**
 * @author xujiajun
 * @since 2026/10/17
 */
public interface DemoNacosConfigService {

    void publishConfig(DemoNacosConfig demoNacosConfig) throws NacosException;

    String getConfig(String dataId, String group) throws NacosException;

    void deleteConfig(String dataId, String group, String key) throws NacosException;
}
