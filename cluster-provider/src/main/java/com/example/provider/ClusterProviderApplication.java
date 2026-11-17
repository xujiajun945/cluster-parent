package com.example.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author xujiajun
 * @since 2026/10/15
 */
@EnableScheduling
@SpringBootApplication
public class ClusterProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClusterProviderApplication.class, args);
    }
}
