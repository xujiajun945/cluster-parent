package com.example.core.handler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 负载均衡策略
 *
 * @author xujiajun
 * @since 2026/10/28
 */
public class clusterRandomLoadBalancerClient {

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