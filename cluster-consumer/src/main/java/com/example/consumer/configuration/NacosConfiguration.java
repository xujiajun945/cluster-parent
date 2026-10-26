package com.example.consumer.configuration;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${spring.cloud.nacos.config.server-addr}"))
//@NacosPropertySource(dataId = "application-${spring.profiles.active}.yml", groupId = "cluster-consumer-demo", autoRefreshed = true)
//@NacosConfigurationProperties(dataId = "application-${spring.profiles.active}.yml", groupId = "cluster-consumer-demo", autoRefreshed = true)
@EnableDiscoveryClient
//@EnableNacosDiscovery(globalProperties = @NacosProperties(serverAddr = "${spring.cloud.nacos.discovery.server-addr}"))
public class NacosConfiguration {
}
