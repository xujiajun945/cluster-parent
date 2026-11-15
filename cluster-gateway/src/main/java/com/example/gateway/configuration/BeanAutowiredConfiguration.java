package com.example.gateway.configuration;

import com.example.core.config.RememberMeConfig;
import com.example.core.config.SessionCookieConfig;
import com.example.core.config.ValkeySessionConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xujiajun
 * @since 2026/11/15
 */
@Configuration
public class BeanAutowiredConfiguration {


    @ConfigurationProperties(prefix = "cluster-gateway.valkey-session")
    @Bean
    public ValkeySessionConfig valkeySessionConfig() {
        return new ValkeySessionConfig();
    }

    @ConfigurationProperties(prefix = "cluster-gateway.remember-me")
    @Bean
    public RememberMeConfig rememberMeConfig() {
        return new RememberMeConfig();
    }

    @ConfigurationProperties(prefix = "cluster-gateway.valkey-session-cookie")
    @Bean
    public SessionCookieConfig sessionCookieConfig() {
        return new SessionCookieConfig();
    }

}
