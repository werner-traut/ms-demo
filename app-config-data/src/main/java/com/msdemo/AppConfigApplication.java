package com.msdemo;

import com.msdemo.config.KafkaConfigData;
import com.msdemo.config.KafkaProducerConfigData;
import com.msdemo.config.RetryConfigData;
import com.msdemo.config.TwitterToKafkaConfigProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        TwitterToKafkaConfigProperties.class,
        RetryConfigData.class,
        KafkaConfigData.class,
        KafkaProducerConfigData.class
})
public class AppConfigApplication {
}
