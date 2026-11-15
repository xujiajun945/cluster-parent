package com.example.provider.configuration;

import com.example.core.config.RememberMeConfig;
import com.example.core.config.ValkeySessionConfig;
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
    @ConfigurationProperties(prefix = "cluster-provider.remember-me")
    public RememberMeConfig rememberMeConfig() {
        return new RememberMeConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "cluster-provider.valkey-session")
    public ValkeySessionConfig valkeySessionConfig() {
        return new ValkeySessionConfig();
    }

}
