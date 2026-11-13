package com.example.core.handler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
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
public class ClusterRoundRobinLoadBalancerClient {

        /**
         * 负载均衡策略：轮询（Round Robin）
         */
        @Bean
        public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(Environment env, LoadBalancerClientFactory factory) {
            String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
            ObjectProvider<ServiceInstanceListSupplier> lazyProvider = factory.getLazyProvider(name, ServiceInstanceListSupplier.class);
            return new RoundRobinLoadBalancer(lazyProvider, name);
        }
}