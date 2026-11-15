package com.example.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author xujiajun
 * @since 2026/11/2
 */
@Setter
@Getter
public class ValkeySessionConfig {

    private String namespace;

    private String sessionPrefix;

    private String expirePrefix;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration maxInactiveInterval;
}
