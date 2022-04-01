package com.msdemo.twitter.to.kafka.service.runner;

import com.msdemo.config.TwitterToKafkaConfigProperties;
import com.msdemo.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@Component
@Slf4j
public class TwitterKafkaStreamRunner implements StreamRunner {

    private final TwitterToKafkaConfigProperties twitterToKafkaConfigProperties;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    private TwitterStream twitterStream;

    public TwitterKafkaStreamRunner(TwitterToKafkaConfigProperties twitterToKafkaConfigProperties, TwitterKafkaStatusListener twitterKafkaStatusListener) {
        this.twitterToKafkaConfigProperties = twitterToKafkaConfigProperties;
        this.twitterKafkaStatusListener = twitterKafkaStatusListener;
    }

    @Override
    public void start() {
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(twitterKafkaStatusListener);
        addFilter();
    }

    @PreDestroy
    public void shutdown() {
        if (twitterStream != null) {
            log.info("Closing twitter stream");
            twitterStream.shutdown();
        }
    }

    private void addFilter() {
        String[] keywords = twitterToKafkaConfigProperties.twitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery  = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);
        log.info("Started filtering twitter stream for keywords {}", Arrays.toString(keywords));
    }
}
