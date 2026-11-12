package com.telecare.serviceinterventions.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecare.serviceinterventions.event.EmergencyResolvedEvent;
import com.telecare.serviceinterventions.event.InterventionAssignedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InterventionEventPublisher {

    public static final String TOPIC_INTERVENTION_ASSIGNED = "intervention-assigned";
    public static final String TOPIC_EMERGENCY_RESOLVED = "emergency-resolved";

    private static final Logger log = LoggerFactory.getLogger(InterventionEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public InterventionEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                      ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publierAffectation(InterventionAssignedEvent event) {
        envoyer(TOPIC_INTERVENTION_ASSIGNED, event.alertId(), event);
    }

    public void publierCloture(EmergencyResolvedEvent event) {
        envoyer(TOPIC_EMERGENCY_RESOLVED, event.alertId(), event);
    }

    private void envoyer(String topic, Long alertId, Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(alertId), json)
                    .whenComplete((resultat, erreur) -> {
                        if (erreur != null) {
                            log.error("Intervention enregistree, mais publication echouee sur le topic {} (alertId {})",
                                    topic, alertId, erreur);
                        }
                    });
        } catch (Exception ex) {
            log.error("Intervention enregistree, mais publication impossible sur le topic {} (alertId {})",
                    topic, alertId, ex);
        }
    }
}