package com.telecare.serviceinterventions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InterventionCreationDto(
        @NotNull(message = "alertId est obligatoire")
        Long alertId,

        @NotBlank(message = "assistantNom est obligatoire")
        @Size(max = 255, message = "assistantNom : 255 caracteres maximum")
        String assistantNom
) {
}