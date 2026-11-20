package com.telecare.serviceinterventions.dto;

import com.telecare.serviceinterventions.entite.StatutIntervention;

import java.time.LocalDateTime;

public record InterventionDetailDto(
        Long id,
        Long alertId,
        Long assistantId,
        LocalDateTime heureArrivee,
        String compteRendu,
        StatutIntervention statut,
        String alerteType,
        String alerteGravite,
        String alerteStatut,
        String protocoleEtapes,
        Integer protocoleNiveauPriorite
) {
}