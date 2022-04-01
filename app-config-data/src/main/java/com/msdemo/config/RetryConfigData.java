package com.msdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retry-config")
public record RetryConfigData(Long initialIntervalMs,
                              Long maxIntervalMs,
                              Double multiplier,
                              Integer maxAttempts,
                              Long sleepTimeMs) {}
