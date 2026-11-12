package com.example.consumer.client;

import com.example.consumer.configuration.ClusterFeignClientConfiguration;
import com.example.core.support.DemoProviderSupport;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@FeignClient(name = "${cluster.provider.cluster-provider}", path = "/demo/provider",
        contextId = "demoClient", configuration = ClusterFeignClientConfiguration.class)
public interface DemoClient extends DemoProviderSupport {
}
