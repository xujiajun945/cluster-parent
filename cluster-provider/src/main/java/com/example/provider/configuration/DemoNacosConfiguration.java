package com.example.provider.configuration;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author xujiajun
 * @since 2026/10/19
 */
@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${spring.cloud.nacos.config.server-addr}"))
@EnableDiscoveryClient
public class DemoNacosConfiguration {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}


