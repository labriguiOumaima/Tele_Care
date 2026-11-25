package com.assistance.alertes.controller;

import com.assistance.alertes.dto.request.AlertCreateDTO;
import com.assistance.alertes.dto.request.AlertStatusUpdateDTO;
import com.assistance.alertes.dto.response.AlertResponseDTO;
import com.assistance.alertes.dto.response.EnrichedAlertDTO;
import com.assistance.alertes.entity.AlertStatus;
import com.assistance.alertes.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Déclencher une alerte pour un bénéficiaire
     */
    @PostMapping
    public ResponseEntity<AlertResponseDTO> triggerAlert(@Valid @RequestBody AlertCreateDTO request) {
        AlertResponseDTO created = alertService.triggerAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Consultation d'une alerte enrichie (Phase V4) :
     * Alerte + Protocole + Dossier Bénéficiaire + Contacts d'urgence (OpenFeign)
     */
    @GetMapping("/{id}/enriched")
    public ResponseEntity<EnrichedAlertDTO> getEnrichedAlertById(@PathVariable Long id) {
        EnrichedAlertDTO enriched = alertService.getEnrichedAlertById(id);
        return ResponseEntity.ok(enriched);
    }

    /**
     * Consulter les alertes et filtrer par statut (ex: /api/alerts?statut=DECLENCHEE)
     */
    @GetMapping
    public ResponseEntity<List<AlertResponseDTO>> getAlerts(
            @RequestParam(required = false) AlertStatus statut) {
        List<AlertResponseDTO> alerts = alertService.getAlerts(statut);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Consulter une alerte spécifique par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertResponseDTO> getAlertById(@PathVariable Long id) {
        AlertResponseDTO alert = alertService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }

    /**
     * Faire évoluer le statut d'une alerte (ex: vers PRISE_EN_CHARGE ou RESOLUE)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertResponseDTO> updateAlertStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlertStatusUpdateDTO request) {
        AlertResponseDTO updated = alertService.updateAlertStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprimer une alerte
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}
