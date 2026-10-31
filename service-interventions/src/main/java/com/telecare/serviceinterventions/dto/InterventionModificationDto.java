package com.telecare.serviceinterventions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterventionModificationDto(
        @NotBlank(message = "assistantNom est obligatoire")
        @Size(max = 255, message = "assistantNom : 255 caracteres maximum")
        String assistantNom
) {
}