package com.telecare.serviceinterventions.dto;

import jakarta.validation.constraints.NotNull;

public record InterventionModificationDto(
        @NotNull(message = "assistantId est obligatoire")
        Long assistantId
) {
}