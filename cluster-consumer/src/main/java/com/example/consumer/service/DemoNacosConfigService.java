package com.example.consumer.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.core.model.NacosConfig;

/**
 * @author xujiajun
 * @since 2026/10/17
 */
public interface DemoNacosConfigService {

    void publishConfig(NacosConfig nacosConfig) throws NacosException;

    String getConfig(String dataId, String group) throws NacosException;
}
