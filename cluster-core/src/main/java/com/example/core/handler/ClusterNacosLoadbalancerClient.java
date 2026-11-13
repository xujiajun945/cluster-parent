package com.example.core.handler;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.loadbalancer.LoadBalancerAlgorithm;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer;
import com.alibaba.cloud.nacos.loadbalancer.ServiceInstanceFilter;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * nacos权重负载均衡策略
 *
 * @author xujiajun
 * @since 2026/11/13
 */
public class ClusterNacosLoadbalancerClient {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment env,
                                                                   LoadBalancerClientFactory factory,
                                                                   NacosDiscoveryProperties nacosDiscoveryProperties,
                                                                   InetIPv6Utils inetIPv6Utils,
                                                                   List<ServiceInstanceFilter> serviceInstanceFilters,
                                                                   List<LoadBalancerAlgorithm> loadBalancerAlgorithms) {
        String name = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        Map<String, LoadBalancerAlgorithm> loadBalancerAlgorithmMap = new HashMap<>();
        loadBalancerAlgorithms.forEach(loadBalancerAlgorithm ->
                loadBalancerAlgorithmMap.putIfAbsent(loadBalancerAlgorithm.getServiceId(), loadBalancerAlgorithm));
        ObjectProvider<ServiceInstanceListSupplier> lazyProvider = factory.getLazyProvider(name, ServiceInstanceListSupplier.class);
        return new NacosLoadBalancer(lazyProvider, name, nacosDiscoveryProperties, inetIPv6Utils, serviceInstanceFilters, loadBalancerAlgorithmMap);
    }
}
