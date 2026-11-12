package com.example.consumer.client;

import com.example.consumer.configuration.ClusterFeignClientConfiguration;
import com.example.core.dto.UserRememberMeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户客户端
 *
 * @author xujiajun
 * @since 2026/11/10
 */
@FeignClient(name = "${cluster.provider.cluster-provider}", path = "/provider/user", contextId = "userClient",
        configuration = ClusterFeignClientConfiguration.class)
public interface UserClient {

    /**
     * 添加rememberMe记录
     *
     * @return 影响行数
     */
    @PostMapping(value = "/user_remember_me")
    Integer addUserRememberMe(@RequestBody UserRememberMeDTO userRememberMeDTO);
}
