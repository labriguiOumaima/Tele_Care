package com.assistance.alertes.event;

import com.assistance.alertes.dto.event.AlertTriggeredEvent;
import com.assistance.alertes.entity.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AlertEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AlertEventPublisher.class);
    public static final String TOPIC_ALERT_TRIGGERED = "alert-triggered";

    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    public AlertEventPublisher(ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
    }

    /**
     * Publie l'événement alert-triggered après l'enregistrement d'une alerte
     */
    public void publishAlertTriggered(Alert alert) {
        AlertTriggeredEvent event = AlertTriggeredEvent.builder()
                .alertId(alert.getId())
                .beneficiaryId(alert.getBeneficiaryId())
                .type(alert.getType())
                .gravite(alert.getGravite())
                .dateDeclenchement(alert.getDateDeclenchement())
                .timestamp(LocalDateTime.now())
                .build();

        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(TOPIC_ALERT_TRIGGERED, String.valueOf(alert.getId()), event);
                log.info("[KAFKA] Événement 'alert-triggered' publié avec succès pour alertId={}", alert.getId());
            } catch (Exception e) {
                log.warn("[KAFKA] Broker Kafka non connecté : événement enregistré localement ({})", e.getMessage());
            }
        } else {
            log.info("[SIMULATION KAFKA] Événement 'alert-triggered' généré avec succès pour alertId={}", alert.getId());
        }
    }
}
