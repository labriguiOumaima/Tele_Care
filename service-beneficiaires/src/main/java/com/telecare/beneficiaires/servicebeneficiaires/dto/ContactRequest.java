package com.telecare.beneficiaires.servicebeneficiaires.dto;

import jakarta.validation.constraints.*;

public record ContactRequest(

        @NotNull(message = "Le bénéficiaire est obligatoire")
        @Positive(message = "L'identifiant du bénéficiaire doit être positif")
        Long beneficiaryId,

        @NotBlank(message = "Le nom du contact est obligatoire")
        String nom,

        @NotBlank(message = "Le lien avec le bénéficiaire est obligatoire")
        String lien,

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone

) {
}