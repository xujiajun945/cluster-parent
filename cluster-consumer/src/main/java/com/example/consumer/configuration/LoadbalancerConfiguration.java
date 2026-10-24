package com.example.consumer.configuration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

/**
 * @author xujiajun
 * @since 2026/10/20
 */
@Configuration
@LoadBalancerClients(value =
        {
                @LoadBalancerClient(name = "${cluster.provider.cluster-provider}", configuration = LoadbalancerConfiguration.ClusterProviderLoadBalancerClient.class),
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


    public static class ClusterProviderLoadBalancerClient {

        /**
         * 负载均衡策略：随机
         */
        @Bean
        public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment env, LoadBalancerClientFactory factory) {
            String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
            ObjectProvider<ServiceInstanceListSupplier> lazyProvider = factory.getLazyProvider(name, ServiceInstanceListSupplier.class);
            return new RandomLoadBalancer(lazyProvider, name);
        }
    }
}
