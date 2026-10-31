package com.telecare.beneficiaires.servicebeneficiaires.dto;

public record ContactResponse(
        Long id,
        Long beneficiaryId,
        String nom,
        String lien,
        String telephone
) {
}
