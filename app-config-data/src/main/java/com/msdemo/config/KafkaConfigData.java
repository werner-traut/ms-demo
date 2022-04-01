package com.msdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "kafka-config")
public record KafkaConfigData(String bootstrapServers,
                              String schemaRegistryUrlKey,
                              String schemaRegistryUrl,
                              String topicName,
                              List<String> topicNamesToCreate,
                              Integer numOfPartitions,
                              Short replicationFactor) {}
