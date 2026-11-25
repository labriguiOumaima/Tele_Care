package com.assistance.alertes.controller;

import com.assistance.alertes.dto.event.EmergencyResolvedEvent;
import com.assistance.alertes.dto.event.InterventionAssignedEvent;
import com.assistance.alertes.dto.response.AlertResponseDTO;
import com.assistance.alertes.event.AlertEventConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Contrôleur utilitaire de simulation pour Personne 2 ("Pour avancer seule").
 * Permet d'injecter des événements fictifs simulant le service service-interventions
 * afin de tester et valider les transitions automatiques de cycle de vie.
 */
@RestController
@RequestMapping("/api/alerts/simulation")
@CrossOrigin(origins = "*")
public class EventSimulationController {

    private final AlertEventConsumer alertEventConsumer;

    public EventSimulationController(AlertEventConsumer alertEventConsumer) {
        this.alertEventConsumer = alertEventConsumer;
    }

    /**
     * Simule l'arrivée de l'événement Kafka intervention-assigned -> statut passe à PRISE_EN_CHARGE
     */
    @PostMapping("/inject-intervention-assigned")
    public ResponseEntity<AlertResponseDTO> injectInterventionAssigned(
            @RequestBody InterventionAssignedEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        AlertResponseDTO updated = alertEventConsumer.onInterventionAssigned(event);
        return ResponseEntity.ok(updated);
    }

    /**
     * Simule l'arrivée de l'événement Kafka emergency-resolved -> statut passe à RESOLUE
     */
    @PostMapping("/inject-emergency-resolved")
    public ResponseEntity<AlertResponseDTO> injectEmergencyResolved(
            @RequestBody EmergencyResolvedEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        AlertResponseDTO updated = alertEventConsumer.onEmergencyResolved(event);
        return ResponseEntity.ok(updated);
    }
}
