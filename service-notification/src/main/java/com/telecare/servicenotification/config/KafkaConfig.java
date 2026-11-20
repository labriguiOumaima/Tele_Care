package com.telecare.servicenotification.config;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        // 3 tentatives au total (1 + 2 reessais), 2 secondes entre chaque
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (record, ex) -> log.error(
                        "Message abandonne apres echec du traitement (topic {}, cle {}) : {}",
                        record.topic(), record.key(), record.value(), ex),
                new FixedBackOff(2000L, 2L));

        // Beneficiaire introuvable chez service-beneficiaires : reessayer ne changerait rien
        handler.addNotRetryableExceptions(FeignException.NotFound.class);
        return handler;
    }
}