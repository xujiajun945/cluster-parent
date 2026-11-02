package com.example.consumer.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author xujiajun
 * @since 2026/11/2
 */
@Component
@ConfigurationProperties(prefix = "cluster-consumer.valkey-session")
@Setter
@Getter
public class ValkeySessionConfig {

    private String namespace;

    private String sessionPrefix;

    private String expirePrefix;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration maxInactiveInterval;
}
