package com.telecare.serviceinterventions.dto;

import jakarta.validation.constraints.NotNull;

public record InterventionCreationDto(
        @NotNull(message = "alertId est obligatoire")
        Long alertId,

        @NotNull(message = "assistantId est obligatoire")
        Long assistantId
) {
}