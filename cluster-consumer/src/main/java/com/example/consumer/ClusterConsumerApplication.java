package com.example.consumer;

import com.example.consumer.configuration.ClusterFeignClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@EnableFeignClients(defaultConfiguration = ClusterFeignClientConfiguration.class)
@SpringBootApplication
public class ClusterConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusterConsumerApplication.class, args);
    }
}
