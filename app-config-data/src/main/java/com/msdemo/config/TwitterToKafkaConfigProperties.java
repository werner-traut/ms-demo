package com.msdemo.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "twitter-to-kafka-service")
public record TwitterToKafkaConfigProperties(List<String> twitterKeywords){}
