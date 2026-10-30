package com.example.consumer.configuration;

import com.example.core.handler.clusterRandomLoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author xujiajun
 * @since 2026/10/20
 */
@Configuration
@LoadBalancerClients(value =
        {
                @LoadBalancerClient(name = "${cluster.provider.cluster-provider}", configuration = clusterRandomLoadBalancerClient.class),
        }
)
public class LoadbalancerConfiguration {

    /**
     * restTemplate是同步阻塞的
     */
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
