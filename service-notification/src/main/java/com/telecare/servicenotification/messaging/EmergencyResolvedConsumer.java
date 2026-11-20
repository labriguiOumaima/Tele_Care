package com.telecare.servicenotification.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecare.servicenotification.event.EmergencyResolvedEvent;
import com.telecare.servicenotification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmergencyResolvedConsumer {

    public static final String TOPIC = "emergency-resolved";

    private static final Logger log = LoggerFactory.getLogger(EmergencyResolvedConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public EmergencyResolvedConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = TOPIC)
    public void recevoir(String message) {
        // Un message illisible est ignore : le reessayer ne changerait rien
        EmergencyResolvedEvent event;
        try {
            event = objectMapper.readValue(message, EmergencyResolvedEvent.class);
        } catch (JsonProcessingException ex) {
            log.error("Message emergency-resolved illisible, ignore : {}", message, ex);
            return;
        }

        try {
            notificationService.notifierResolution(event);
        } catch (DataIntegrityViolationException ex) {
            // Idempotence : le meme evenement a ete traite en parallele
            log.info("Evenement {} deja traite, message ignore", event.eventId());
        }
    }
}