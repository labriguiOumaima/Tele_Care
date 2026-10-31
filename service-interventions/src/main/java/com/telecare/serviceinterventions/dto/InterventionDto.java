package com.telecare.serviceinterventions.dto;

import com.telecare.serviceinterventions.entite.StatutIntervention;

import java.time.LocalDateTime;

public record InterventionDto(
        Long id,
        Long alertId,
        String assistantNom,
        LocalDateTime heureArrivee,
        String compteRendu,
        StatutIntervention statut
) {
}