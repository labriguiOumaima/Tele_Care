package com.assistance.alertes.event;

import com.assistance.alertes.dto.event.EmergencyResolvedEvent;
import com.assistance.alertes.dto.event.InterventionAssignedEvent;
import com.assistance.alertes.dto.request.AlertStatusUpdateDTO;
import com.assistance.alertes.dto.response.AlertResponseDTO;
import com.assistance.alertes.entity.AlertStatus;
import com.assistance.alertes.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AlertEventConsumer.class);

    private final AlertService alertService;

    public AlertEventConsumer(AlertService alertService) {
        this.alertService = alertService;
    }

    @KafkaListener(
            topics = "intervention-assigned",
            groupId = "alertes-group",
            autoStartup = "${spring.kafka.listener.auto-startup:true}",
            properties = {
                    "spring.json.value.default.type=com.assistance.alertes.dto.event.InterventionAssignedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public AlertResponseDTO onInterventionAssigned(
            InterventionAssignedEvent event) {

        log.info(
                "[KAFKA RECU] intervention-assigned : alertId={}, interventionId={}, assistantId={}",
                event.getAlertId(),
                event.getInterventionId(),
                event.getAssistantId()
        );

        AlertResponseDTO current = alertService.getAlertById(event.getAlertId());
        if (current.getStatut() == AlertStatus.RESOLUE) return current;
        AlertStatusUpdateDTO updateDTO = AlertStatusUpdateDTO.builder()
                .statut(AlertStatus.PRISE_EN_CHARGE)
                .build();

        AlertResponseDTO updated =
                alertService.updateAlertStatus(event.getAlertId(), updateDTO);

        log.info(
                "[CYCLE DE VIE] Alerte id={} : {}",
                updated.getId(),
                updated.getStatut()
        );

        return updated;
    }

    @KafkaListener(
            topics = "emergency-resolved",
            groupId = "alertes-group",
            autoStartup = "${spring.kafka.listener.auto-startup:true}",
            properties = {
                    "spring.json.value.default.type=com.assistance.alertes.dto.event.EmergencyResolvedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public AlertResponseDTO onEmergencyResolved(
            EmergencyResolvedEvent event) {

        log.info(
                "[KAFKA RECU] emergency-resolved : alertId={}, interventionId={}",
                event.getAlertId(),
                event.getInterventionId()
        );

        AlertStatusUpdateDTO updateDTO = AlertStatusUpdateDTO.builder()
                .statut(AlertStatus.RESOLUE)
                .compteRendu(event.getCompteRendu())
                .build();

        AlertResponseDTO updated =
                alertService.updateAlertStatus(event.getAlertId(), updateDTO);

        log.info(
                "[CYCLE DE VIE] Alerte id={} : {}, compte-rendu={}",
                updated.getId(),
                updated.getStatut(),
                updated.getCompteRendu()
        );

        return updated;
    }
}