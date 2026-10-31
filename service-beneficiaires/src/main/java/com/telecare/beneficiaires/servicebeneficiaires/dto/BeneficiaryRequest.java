package com.telecare.beneficiaires.servicebeneficiaires.dto;

import jakarta.validation.constraints.*;

public record BeneficiaryRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotNull(message = "L'âge est obligatoire")
        @Min(value = 0, message = "L'âge doit être positif ou nul")
        @Max(value = 150, message = "L'âge ne doit pas dépasser 150 ans")
        Integer age,

        @NotBlank(message = "L'adresse est obligatoire")
        String adresse,

        String pathologie,

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone

) {
}