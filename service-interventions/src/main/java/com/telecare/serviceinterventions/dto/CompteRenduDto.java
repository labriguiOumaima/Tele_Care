package com.telecare.serviceinterventions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompteRenduDto(
        @NotBlank(message = "compteRendu est obligatoire")
        @Size(max = 2000, message = "compteRendu : 2000 caracteres maximum")
        String compteRendu
) {
}