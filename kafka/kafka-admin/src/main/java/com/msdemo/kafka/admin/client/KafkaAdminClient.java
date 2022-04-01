package com.msdemo.kafka.admin.client;

import com.msdemo.config.KafkaConfigData;
import com.msdemo.config.RetryConfigData;
import com.msdemo.kafka.admin.exception.KafkaClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KafkaAdminClient {

    private final KafkaConfigData kafkaConfigData;
    private final RetryConfigData retryConfigData;
    private final AdminClient adminClient;
    private final RetryTemplate retryTemplate;
    private final WebClient webClient;

    public KafkaAdminClient(KafkaConfigData kafkaConfigData, RetryConfigData retryConfigData, AdminClient adminClient, RetryTemplate retryTemplate, WebClient webClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.retryConfigData = retryConfigData;
        this.adminClient = adminClient;
        this.retryTemplate = retryTemplate;
        this.webClient = webClient;
    }

    public void createTopics() {
        CreateTopicsResult createTopicsResult;
        try {
            createTopicsResult = retryTemplate.execute(this::doCreateTopics);
        } catch (Throwable e) {
            throw new KafkaClientException("Reached max number of retry for creating kafka topics", e);
        }
        checkTopicsCreated();
    }

    public void checkSchemaRegistry() {
        int retryCount = 1;
        Integer maxAttempts = retryConfigData.maxAttempts();
        int multiplier = retryConfigData.multiplier().intValue();
        Long sleepTimeMs = retryConfigData.sleepTimeMs();
        while (!getSchemaRegistryStatus().is2xxSuccessful()) {
            checkMaxRetry(retryCount++, maxAttempts);
            sleep(sleepTimeMs);
            sleepTimeMs*= multiplier;
        }
    }

    private HttpStatus getSchemaRegistryStatus() {
        try {
            var response =  webClient
                    .get()
                    .uri(kafkaConfigData.schemaRegistryUrl())
                    .retrieve()
                    .toEntity(Void.class)
                    .block()
                    .getStatusCode();
            return response;
        } catch (Exception e) {
            log.error("Error checking status of schema resistry", e);
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

    private void checkTopicsCreated() {
        Collection<TopicListing> topics = getTopics();
        int retryCount = 1;
        Integer maxAttempts = retryConfigData.maxAttempts();
        int multiplier = retryConfigData.multiplier().intValue();
        Long sleepTimeMs = retryConfigData.sleepTimeMs();

        for (String topic : kafkaConfigData.topicNamesToCreate()) {
            while (!isTopicCreated(topics, topic)) {
                checkMaxRetry(retryCount++, maxAttempts);
                sleep(sleepTimeMs);
                sleepTimeMs *= multiplier;
                topics = getTopics();
            }
        }
    }

    private void sleep(Long sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {
            throw new KafkaClientException("Error while sleeping for waiting new created topics");
        }
    }

    private void checkMaxRetry(int retry, Integer maxAttempts) {
        if (retry > maxAttempts) {
            throw new KafkaClientException("Reached max number of retry for reading kafka topics");
        }
    }

    private boolean isTopicCreated(Collection<TopicListing> topics, String topicName) {
        if (topics == null) {
            return false;
        }

        return topics.stream().anyMatch(topic -> topic.name().equals(topicName));
    }

    private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
        List<String> topicNames = kafkaConfigData.topicNamesToCreate();
        log.info("Creating {} topics, attempt {}", topicNames.size(), retryContext.getRetryCount());
        List<NewTopic> topics = topicNames.stream().map(topic -> new NewTopic(
                topic.trim(),
                kafkaConfigData.numOfPartitions(),
                kafkaConfigData.replicationFactor()
        )).collect(Collectors.toList());

        return adminClient.createTopics(topics);
    }

    private Collection<TopicListing> getTopics() {
        Collection<TopicListing> topics;
        try {
            topics = retryTemplate.execute(this::doGetTopics);
        } catch (Throwable e) {
            throw new KafkaClientException("Reached max number of retry for reading kafka topics", e);
        }
        return topics;
    }

    private Collection<TopicListing> doGetTopics(RetryContext retryContext) throws ExecutionException, InterruptedException {
        log.info("Reading kafka topic {}, attempt {}", kafkaConfigData.topicNamesToCreate().toArray(), retryContext.getRetryCount());
        Collection<TopicListing> topicListings = adminClient.listTopics().listings().get();
        if (topicListings != null) {
            topicListings.forEach(topic -> log.info("Topic with name {}", topic.name()));
        }
        return topicListings;
    }
}
