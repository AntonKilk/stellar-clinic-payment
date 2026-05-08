package com.stellar.crm.paymentservice.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentStatusUpdatedTopic(
            @Value("${stellar.kafka.topic.payment-status-updated}") String name,
            @Value("${stellar.kafka.topic.partitions}") int partitions,
            @Value("${stellar.kafka.topic.replicas}") short replicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
