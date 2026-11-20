package com.telecare.servicenotification.event;

import java.time.LocalDateTime;

public record EmergencyResolvedEvent(
        String eventId,
        Long interventionId,
        Long alertId,
        Long beneficiaryId,
        String compteRendu,
        LocalDateTime dateCloture
) {
}