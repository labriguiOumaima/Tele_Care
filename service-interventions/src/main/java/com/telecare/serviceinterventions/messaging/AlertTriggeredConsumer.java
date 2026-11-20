package com.telecare.serviceinterventions.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecare.serviceinterventions.entite.Intervention;
import com.telecare.serviceinterventions.event.AlertTriggeredEvent;
import com.telecare.serviceinterventions.exception.InterventionExistanteException;
import com.telecare.serviceinterventions.service.AssistantAstreinteService;
import com.telecare.serviceinterventions.service.InterventionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertTriggeredConsumer {

    public static final String TOPIC_ALERT_TRIGGERED = "alert-triggered";

    private static final Logger log = LoggerFactory.getLogger(AlertTriggeredConsumer.class);

    private final ObjectMapper objectMapper;
    private final InterventionService interventionService;
    private final AssistantAstreinteService assistantService;

    public AlertTriggeredConsumer(ObjectMapper objectMapper,
                                  InterventionService interventionService,
                                  AssistantAstreinteService assistantService) {
        this.objectMapper = objectMapper;
        this.interventionService = interventionService;
        this.assistantService = assistantService;
    }

    @KafkaListener(topics = TOPIC_ALERT_TRIGGERED)
    public void recevoir(String message) {
        // 1. Lire le message JSON (un message illisible est ignore : le reessayer ne changerait rien)
        AlertTriggeredEvent event;
        try {
            event = objectMapper.readValue(message, AlertTriggeredEvent.class);
        } catch (JsonProcessingException ex) {
            log.error("Message alert-triggered illisible, ignore : {}", message, ex);
            return;
        }

        // 2. Seul alertId est indispensable
        if (event.alertId() == null) {
            log.error("Message alert-triggered sans alertId, ignore : {}", message);
            return;
        }

        // 3. Creer l'intervention avec un assistant de la liste fictive
        Intervention intervention = new Intervention();
        intervention.setAlertId(event.alertId());
        intervention.setAssistantId(assistantService.prochainAssistant());

        try {
            interventionService.creerIntervention(intervention);
            log.info("Intervention creee pour l'alerte {}", event.alertId());
        } catch (InterventionExistanteException ex) {
            // Idempotence : message deja traite, on ne cree rien de plus
            log.info("Alerte {} deja traitee, message ignore", event.alertId());
        }
    }
}