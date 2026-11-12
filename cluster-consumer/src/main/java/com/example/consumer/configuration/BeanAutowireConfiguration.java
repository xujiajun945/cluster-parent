package com.example.consumer.configuration;

import com.example.core.config.RememberMeConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author xujiajun
 * @since 2026/11/12
 */
@Configuration
public class BeanAutowireConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "cluster-consumer.remember-me")
    public RememberMeConfig rememberMeConfig() {
        return new RememberMeConfig();
    }

}
