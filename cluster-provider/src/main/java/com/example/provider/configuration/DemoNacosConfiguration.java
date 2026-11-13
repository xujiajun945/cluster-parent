package com.example.provider.configuration;

import com.example.core.handler.clusterRandomLoadBalancerClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author xujiajun
 * @since 2026/10/19
 */
@Configuration
@LoadBalancerClients(
        @LoadBalancerClient(name = "${cluster.provider.cluster-consumer}", configuration = clusterRandomLoadBalancerClient.class)
)
@EnableDiscoveryClient
public class DemoNacosConfiguration {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}


