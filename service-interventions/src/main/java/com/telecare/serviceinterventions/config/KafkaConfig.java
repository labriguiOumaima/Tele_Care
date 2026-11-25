package com.telecare.serviceinterventions.config;

import com.telecare.serviceinterventions.exception.TransitionInterditeException;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public NewTopic alertTriggeredTopic() {
        return TopicBuilder.name("alert-triggered")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic interventionAssignedTopic() {
        return TopicBuilder.name("intervention-assigned")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emergencyResolvedTopic() {
        return TopicBuilder.name("emergency-resolved")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (record, ex) -> log.error(
                        "Message abandonne apres echec du traitement "
                                + "(topic {}, cle {}) : {}",
                        record.topic(),
                        record.key(),
                        record.value(),
                        ex),
                new FixedBackOff(2000L, 2L)
        );

        handler.addNotRetryableExceptions(
                TransitionInterditeException.class
        );

        return handler;
    }
}