package com.example.gateway.configuration;

import com.example.core.handler.ClusterNacosLoadbalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

/**
 * 负载均衡配置
 *
 * @author xujiajun
 * @since 2026/11/13
 */
@LoadBalancerClients(
        {
                @LoadBalancerClient(name = "cluster-consumer-dev", configuration = ClusterNacosLoadbalancerClient.class)
        }
)
@Configuration
public class LoadbalancerConfiguration {
}
