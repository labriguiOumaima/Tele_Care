package com.telecare.serviceinterventions.event;

import java.time.LocalDateTime;

public record InterventionAssignedEvent(
        String eventId,
        Long interventionId,
        Long alertId,
        Long beneficiaryId,
        String assistantNom,
        LocalDateTime dateAffectation
) {
}