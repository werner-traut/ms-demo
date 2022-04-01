package com.msdemo.twitter.to.kafka.service.listener;

import com.msdemo.config.KafkaConfigData;
import com.msdemo.kafka.avro.model.TwitterAvroModel;
import com.msdemo.kafka.producer.config.service.KafkaProducer;
import com.msdemo.twitter.to.kafka.service.transformer.TwitterStatusToAvroTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusAdapter;

@Slf4j
@Component
public class TwitterKafkaStatusListener extends StatusAdapter {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;
    private final TwitterStatusToAvroTransformer transformer;

    public TwitterKafkaStatusListener(KafkaConfigData kafkaConfigData, KafkaProducer<Long, TwitterAvroModel> kafkaProducer, TwitterStatusToAvroTransformer transformer) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducer = kafkaProducer;
        this.transformer = transformer;
    }

    @Override
    public void onStatus(Status status) {
        log.info("Received status text {} sending to kafka topic {}",
                status.getText(), kafkaConfigData.topicName());
        TwitterAvroModel twitterAvroModel = transformer.getTwitterAvroModelFromStatus(status);
        kafkaProducer.send(kafkaConfigData.topicName(), twitterAvroModel.getUserId(), twitterAvroModel);
    }
}
