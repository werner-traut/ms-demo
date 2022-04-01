package com.msdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka-producer-config")
public record KafkaProducerConfigData(String keySerializerClass,
                                      String valueSerializerClass,
                                      String compressionType,
                                      String acks,
                                      Integer batchSize,
                                      Integer batchSizeBoostFactor,
                                      Integer lingerMs,
                                      Integer requestTimeoutMs,
                                      Integer retryCount
) {
}
