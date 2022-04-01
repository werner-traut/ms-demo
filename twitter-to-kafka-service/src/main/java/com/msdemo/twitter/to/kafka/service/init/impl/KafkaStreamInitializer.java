package com.msdemo.twitter.to.kafka.service.init.impl;

import com.msdemo.config.KafkaConfigData;
import com.msdemo.kafka.admin.client.KafkaAdminClient;
import com.msdemo.twitter.to.kafka.service.init.StreamInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaStreamInitializer implements StreamInitializer {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    public KafkaStreamInitializer(KafkaConfigData kafkaConfigData, KafkaAdminClient adminClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaAdminClient = adminClient;
    }

    @Override
    public void init() {
        kafkaAdminClient.createTopics();
        kafkaAdminClient.checkSchemaRegistry();
        log.info("Topics with name {} is ready for operations", kafkaConfigData.topicNamesToCreate().toArray());
    }
}
