package com.stellar.crm.inventoryservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic cancellationResponseTopic(
            @Value("${stellar.kafka.topic.cancellation-response}") String name,
            @Value("${stellar.kafka.topic.partitions}") int partitions,
            @Value("${stellar.kafka.topic.replicas}") short replicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
