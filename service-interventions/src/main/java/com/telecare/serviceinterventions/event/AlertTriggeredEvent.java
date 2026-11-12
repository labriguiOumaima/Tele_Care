package com.telecare.serviceinterventions.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlertTriggeredEvent(
        Long alertId,
        Long beneficiaryId,
        String type,
        String gravite,
        String dateDeclenchement
) {
}