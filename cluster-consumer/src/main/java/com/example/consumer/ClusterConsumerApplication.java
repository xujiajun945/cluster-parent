package com.example.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@EnableScheduling
@ServletComponentScan
@EnableFeignClients
@SpringBootApplication
public class ClusterConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusterConsumerApplication.class, args);
    }
}
